package com.dnsoptimizer.pro.domain.benchmark

import android.util.Log
import com.dnsoptimizer.pro.data.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

/**
 * Core DNS benchmark engine.
 * Performs real DNS queries to measure latency, reliability, and stability.
 * 
 * This engine does NOT fake measurements. Every result comes from actual DNS queries.
 */
class DnsBenchmarkEngine {
    
    companion object {
        private const val TAG = "DnsBenchmarkEngine"
        private const val DEFAULT_TEST_COUNT = 10
        private const val DEFAULT_TIMEOUT_MS = 5000L
        private const val CONCURRENT_LIMIT = 3
        
        // Well-known test domains for DNS resolution verification
        val TEST_DOMAINS = listOf(
            "google.com",
            "cloudflare.com",
            "microsoft.com",
            "amazon.com",
            "steamcdn-a.akamaihd.net",
            "epicgames.com",
            "minecraft.net"
        )
    }
    
    private val _progress = MutableStateFlow<BenchmarkProgress>(BenchmarkProgress.Idle)
    val progress: StateFlow<BenchmarkProgress> = _progress.asStateFlow()
    
    private val _currentResult = MutableStateFlow<BenchmarkResult?>(null)
    val currentResult: StateFlow<BenchmarkResult?> = _currentResult.asStateFlow()
    
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val activeJobs = mutableListOf<Job>()
    
    /**
     * Run a complete benchmark for a single DNS provider.
     * Tests multiple protocols and collects comprehensive metrics.
     */
    suspend fun benchmarkProvider(
        provider: DnsProvider,
        testCount: Int = DEFAULT_TEST_COUNT,
        timeoutMs: Long = DEFAULT_TIMEOUT_MS,
        testDomains: List<String> = TEST_DOMAINS
    ): BenchmarkResult = withContext(Dispatchers.IO) {
        
        Log.i(TAG, "Starting benchmark for ${provider.name}")
        
        val result = BenchmarkResult(
            providerId = provider.id,
            providerName = provider.name,
            protocol = DnsProtocol.UDP, // Default, will be updated per protocol
            testDomain = testDomains.first(),
            testCount = testCount,
            timeoutMs = timeoutMs,
            status = BenchmarkStatus.RUNNING
        )
        
        _currentResult.value = result
        _progress.value = BenchmarkProgress.Testing(provider.name, "UDP", 0, testCount)
        
        // Test UDP DNS first (most common)
        val udpResult = testUdpDns(provider, testCount, timeoutMs, testDomains)
        
        // Update progress
        _progress.value = BenchmarkProgress.Testing(provider.name, "TCP", 0, testCount)
        
        // Test TCP DNS
        val tcpResult = testTcpDns(provider, testCount, timeoutMs, testDomains)
        
        // Test DoH if available
        val dohResult = if (provider.dohEndpoint != null) {
            _progress.value = BenchmarkProgress.Testing(provider.name, "DoH", 0, testCount)
            testDohDns(provider, testCount, timeoutMs, testDomains)
        } else null
        
        // Test DoT if available
        val dotResult = if (provider.dotHostname != null) {
            _progress.value = BenchmarkProgress.Testing(provider.name, "DoT", 0, testCount)
            testDotDns(provider, testCount, timeoutMs, testDomains)
        } else null
        
        // Combine results - use the best performing protocol
        val allResults = listOfNotNull(udpResult, tcpResult, dohResult, dotResult)
        
        val bestResult = allResults.minByOrNull { it.medianLatencyMs } ?: udpResult
        
        // Calculate final score
        val scoredResult = calculateScore(bestResult)
        
        _currentResult.value = scoredResult
        _progress.value = BenchmarkProgress.Completed(provider.name, scoredResult)
        
        scoredResult
    }
    
    /**
     * Benchmark all providers concurrently.
     */
    suspend fun benchmarkAllProviders(
        providers: List<DnsProvider>,
        testCount: Int = DEFAULT_TEST_COUNT,
        timeoutMs: Long = DEFAULT_TIMEOUT_MS
    ): List<BenchmarkResult> = withContext(Dispatchers.IO) {
        
        val results = ConcurrentLinkedQueue<BenchmarkResult>()
        val completedCount = AtomicInteger(0)
        
        _progress.value = BenchmarkProgress.MultiTesting(
            providers.map { it.name },
            0,
            providers.size
        )
        
        // Test providers in batches to avoid overwhelming the network
        providers.chunked(CONCURRENT_LIMIT).forEach { batch ->
            val batchJobs = batch.map { provider ->
                async {
                    try {
                        val result = benchmarkProvider(provider, testCount, timeoutMs)
                        results.add(result)
                        completedCount.incrementAndGet()
                        _progress.value = BenchmarkProgress.MultiTesting(
                            providers.map { it.name },
                            completedCount.get(),
                            providers.size
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Benchmark failed for ${provider.name}", e)
                        results.add(
                            BenchmarkResult(
                                providerId = provider.id,
                                providerName = provider.name,
                                protocol = DnsProtocol.UDP,
                                testDomain = TEST_DOMAINS.first(),
                                status = BenchmarkStatus.FAILED,
                                errorMessage = e.message
                            )
                        )
                        completedCount.incrementAndGet()
                    }
                }
            }
            activeJobs.addAll(batchJobs)
            batchJobs.awaitAll()
        }
        
        results.toList().sortedBy { it.medianLatencyMs }
    }
    
    /**
     * Test UDP DNS resolution.
     */
    private suspend fun testUdpDns(
        provider: DnsProvider,
        testCount: Int,
        timeoutMs: Long,
        testDomains: List<String>
    ): BenchmarkResult = withContext(Dispatchers.IO) {
        
        val latencies = mutableListOf<Double>()
        var successCount = 0
        var failureCount = 0
        var timeoutCount = 0
        
        for (i in 0 until testCount) {
            val domain = testDomains[i % testDomains.size]
            
            try {
                val startTime = System.nanoTime()
                
                val socket = DatagramSocket()
                socket.soTimeout = timeoutMs.toInt()
                
                // Build DNS query packet
                val query = buildDnsQuery(domain)
                val packet = DatagramPacket(
                    query,
                    query.size,
                    InetAddress.getByName(provider.ipv4Primary),
                    53
                )
                
                socket.send(packet)
                
                // Receive response
                val buffer = ByteArray(512)
                val responsePacket = DatagramPacket(buffer, buffer.size)
                socket.receive(responsePacket)
                
                val endTime = System.nanoTime()
                val latencyMs = (endTime - startTime) / 1_000_000.0
                
                latencies.add(latencyMs)
                successCount++
                
                socket.close()
                
                // Update progress
                _progress.value = BenchmarkProgress.Testing(
                    provider.name,
                    "UDP",
                    i + 1,
                    testCount
                )
                
            } catch (e: SocketTimeoutException) {
                timeoutCount++
                failureCount++
                Log.w(TAG, "UDP timeout for ${provider.name} on $domain")
            } catch (e: Exception) {
                failureCount++
                Log.w(TAG, "UDP failure for ${provider.name}: ${e.message}")
            }
            
            // Small delay between tests
            delay(50)
        }
        
        calculateMetrics(
            providerId = provider.id,
            providerName = provider.name,
            protocol = DnsProtocol.UDP,
            latencies = latencies,
            successCount = successCount,
            failureCount = failureCount,
            timeoutCount = timeoutCount,
            testCount = testCount
        )
    }
    
    /**
     * Test TCP DNS resolution.
     */
    private suspend fun testTcpDns(
        provider: DnsProvider,
        testCount: Int,
        timeoutMs: Long,
        testDomains: List<String>
    ): BenchmarkResult = withContext(Dispatchers.IO) {
        
        val latencies = mutableListOf<Double>()
        var successCount = 0
        var failureCount = 0
        var timeoutCount = 0
        
        for (i in 0 until testCount) {
            val domain = testDomains[i % testDomains.size]
            
            try {
                val startTime = System.nanoTime()
                
                val socket = Socket()
                socket.connect(
                    InetSocketAddress(provider.ipv4Primary, 53),
                    timeoutMs.toInt()
                )
                socket.soTimeout = timeoutMs.toInt()
                
                // Build DNS query
                val query = buildDnsQuery(domain)
                
                // TCP DNS uses length prefix
                val lengthPrefix = byteArrayOf(
                    (query.size shr 8).toByte(),
                    (query.size and 0xFF).toByte()
                )
                
                socket.getOutputStream().write(lengthPrefix + query)
                
                // Read response
                val lengthBuffer = ByteArray(2)
                socket.getInputStream().read(lengthBuffer)
                val responseLength = (lengthBuffer[0].toInt() shl 8) or (lengthBuffer[1].toInt() and 0xFF)
                
                val responseBuffer = ByteArray(responseLength)
                socket.getInputStream().read(responseBuffer)
                
                val endTime = System.nanoTime()
                val latencyMs = (endTime - startTime) / 1_000_000.0
                
                latencies.add(latencyMs)
                successCount++
                
                socket.close()
                
                _progress.value = BenchmarkProgress.Testing(
                    provider.name,
                    "TCP",
                    i + 1,
                    testCount
                )
                
            } catch (e: SocketTimeoutException) {
                timeoutCount++
                failureCount++
            } catch (e: Exception) {
                failureCount++
            }
            
            delay(50)
        }
        
        calculateMetrics(
            providerId = provider.id,
            providerName = provider.name,
            protocol = DnsProtocol.TCP,
            latencies = latencies,
            successCount = successCount,
            failureCount = failureCount,
            timeoutCount = timeoutCount,
            testCount = testCount
        )
    }
    
    /**
     * Test DNS over HTTPS.
     */
    private suspend fun testDohDns(
        provider: DnsProvider,
        testCount: Int,
        timeoutMs: Long,
        testDomains: List<String>
    ): BenchmarkResult? {
        val endpoint = provider.dohEndpoint ?: return null
        
        return withContext(Dispatchers.IO) {
            val latencies = mutableListOf<Double>()
            var successCount = 0
            var failureCount = 0
            var timeoutCount = 0
            
            for (i in 0 until testCount) {
                val domain = testDomains[i % testDomains.size]
                
                try {
                    val startTime = System.nanoTime()
                    
                    val query = buildDnsQuery(domain)
                    val base64Query = android.util.Base64.encodeToString(
                        query,
                        android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING
                    )
                    
                    val url = "$endpoint?dns=$base64Query"
                    val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.setRequestProperty("Accept", "application/dns-message")
                    connection.connectTimeout = timeoutMs.toInt()
                    connection.readTimeout = timeoutMs.toInt()
                    
                    val responseCode = connection.responseCode
                    if (responseCode == 200) {
                        connection.inputStream.readBytes()
                        val endTime = System.nanoTime()
                        val latencyMs = (endTime - startTime) / 1_000_000.0
                        
                        latencies.add(latencyMs)
                        successCount++
                    } else {
                        failureCount++
                    }
                    
                    connection.disconnect()
                    
                    _progress.value = BenchmarkProgress.Testing(
                        provider.name,
                        "DoH",
                        i + 1,
                        testCount
                    )
                    
                } catch (e: SocketTimeoutException) {
                    timeoutCount++
                    failureCount++
                } catch (e: Exception) {
                    failureCount++
                }
                
                delay(100) // DoH needs more delay
            }
            
            calculateMetrics(
                providerId = provider.id,
                providerName = provider.name,
                protocol = DnsProtocol.DOH,
                latencies = latencies,
                successCount = successCount,
                failureCount = failureCount,
                timeoutCount = timeoutCount,
                testCount = testCount
            )
        }
    }
    
    /**
     * Test DNS over TLS.
     */
    private suspend fun testDotDns(
        provider: DnsProvider,
        testCount: Int,
        timeoutMs: Long,
        testDomains: List<String>
    ): BenchmarkResult? {
        val hostname = provider.dotHostname ?: return null
        
        return withContext(Dispatchers.IO) {
            val latencies = mutableListOf<Double>()
            var successCount = 0
            var failureCount = 0
            var timeoutCount = 0
            
            for (i in 0 until testCount) {
                val domain = testDomains[i % testDomains.size]
                
                try {
                    val startTime = System.nanoTime()
                    
                    // Create TLS socket
                    val sslContext = javax.net.ssl.SSLContext.getInstance("TLS")
                    sslContext.init(null, null, null)
                    val factory = sslContext.socketFactory
                    
                    val socket = factory.createSocket() as javax.net.ssl.SSLSocket
                    socket.connect(
                        InetSocketAddress(hostname, 853),
                        timeoutMs.toInt()
                    )
                    socket.soTimeout = timeoutMs.toInt()
                    socket.startHandshake()
                    
                    // Build and send DNS query
                    val query = buildDnsQuery(domain)
                    val lengthPrefix = byteArrayOf(
                        (query.size shr 8).toByte(),
                        (query.size and 0xFF).toByte()
                    )
                    
                    socket.outputStream.write(lengthPrefix + query)
                    
                    // Read response
                    val lengthBuffer = ByteArray(2)
                    socket.inputStream.read(lengthBuffer)
                    val responseLength = (lengthBuffer[0].toInt() shl 8) or (lengthBuffer[1].toInt() and 0xFF)
                    
                    val responseBuffer = ByteArray(responseLength)
                    socket.inputStream.read(responseBuffer)
                    
                    val endTime = System.nanoTime()
                    val latencyMs = (endTime - startTime) / 1_000_000.0
                    
                    latencies.add(latencyMs)
                    successCount++
                    
                    socket.close()
                    
                    _progress.value = BenchmarkProgress.Testing(
                        provider.name,
                        "DoT",
                        i + 1,
                        testCount
                    )
                    
                } catch (e: SocketTimeoutException) {
                    timeoutCount++
                    failureCount++
                } catch (e: Exception) {
                    failureCount++
                }
                
                delay(100)
            }
            
            calculateMetrics(
                providerId = provider.id,
                providerName = provider.name,
                protocol = DnsProtocol.DOT,
                latencies = latencies,
                successCount = successCount,
                failureCount = failureCount,
                timeoutCount = timeoutCount,
                testCount = testCount
            )
        }
    }
    
    /**
     * Build a simple DNS query packet for A record.
     */
    private fun buildDnsQuery(domain: String): ByteArray {
        val parts = domain.split(".")
        val question = mutableListOf<Byte>()
        
        // Transaction ID
        val transactionId = (Math.random() * 65535).toInt().toShort()
        question.addAll(listOf(
            (transactionId shr 8).toByte(),
            (transactionId and 0xFF).toByte()
        ))
        
        // Flags: standard query
        question.addAll(listOf(0x01, 0x00))
        
        // Questions: 1
        question.addAll(listOf(0x00, 0x01))
        
        // Answer RRs: 0
        question.addAll(listOf(0x00, 0x00))
        
        // Authority RRs: 0
        question.addAll(listOf(0x00, 0x00))
        
        // Additional RRs: 0
        question.addAll(listOf(0x00, 0x00))
        
        // Question: domain name
        for (part in parts) {
            question.add(part.length.toByte())
            question.addAll(part.toByteArray().toList())
        }
        question.add(0) // Root label
        
        // Type: A (1)
        question.addAll(listOf(0x00, 0x01))
        
        // Class: IN (1)
        question.addAll(listOf(0x00, 0x01))
        
        return question.toByteArray()
    }
    
    /**
     * Calculate comprehensive metrics from raw latency data.
     */
    private fun calculateMetrics(
        providerId: String,
        providerName: String,
        protocol: DnsProtocol,
        latencies: MutableList<Double>,
        successCount: Int,
        failureCount: Int,
        timeoutCount: Int,
        testCount: Int
    ): BenchmarkResult {
        
        if (latencies.isEmpty()) {
            return BenchmarkResult(
                providerId = providerId,
                providerName = providerName,
                protocol = protocol,
                testDomain = TEST_DOMAINS.first(),
                totalTests = testCount,
                successCount = 0,
                failureCount = failureCount,
                timeoutCount = timeoutCount,
                successRate = 0.0,
                status = BenchmarkStatus.FAILED,
                errorMessage = "All tests failed"
            )
        }
        
        val sorted = latencies.sorted()
        val min = sorted.first()
        val max = sorted.last()
        val avg = sorted.average()
        val median = if (sorted.size % 2 == 0) {
            (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2
        } else {
            sorted[sorted.size / 2]
        }
        
        // P95 latency
        val p95Index = (sorted.size * 0.95).toInt().coerceAtMost(sorted.size - 1)
        val p95 = sorted[p95Index]
        
        // Jitter (standard deviation)
        val mean = sorted.average()
        val variance = sorted.map { (it - mean) * (it - mean) }.average()
        val jitter = Math.sqrt(variance)
        
        val successRate = if (testCount > 0) (successCount.toDouble() / testCount) * 100 else 0.0
        
        return BenchmarkResult(
            providerId = providerId,
            providerName = providerName,
            protocol = protocol,
            testDomain = TEST_DOMAINS.first(),
            totalTests = testCount,
            successCount = successCount,
            failureCount = failureCount,
            timeoutCount = timeoutCount,
            successRate = successRate,
            minLatencyMs = min,
            maxLatencyMs = max,
            avgLatencyMs = avg,
            medianLatencyMs = median,
            p95LatencyMs = p95,
            jitterMs = jitter,
            status = BenchmarkStatus.COMPLETED,
            resolutionCorrectness = successCount > 0
        )
    }
    
    /**
     * Calculate overall score (0-100) based on multiple factors.
     */
    private fun calculateScore(result: BenchmarkResult): BenchmarkResult {
        var score = 100.0
        
        // Penalize based on median latency (0-40 points)
        when {
            result.medianLatencyMs < 20 -> score -= 0
            result.medianLatencyMs < 50 -> score -= 10
            result.medianLatencyMs < 100 -> score -= 20
            result.medianLatencyMs < 200 -> score -= 30
            else -> score -= 40
        }
        
        // Penalize based on jitter (0-20 points)
        when {
            result.jitterMs < 5 -> score -= 0
            result.jitterMs < 15 -> score -= 5
            result.jitterMs < 30 -> score -= 10
            result.jitterMs < 50 -> score -= 15
            else -> score -= 20
        }
        
        // Penalize based on failure rate (0-30 points)
        val failureRate = 100 - result.successRate
        when {
            failureRate == 0.0 -> score -= 0
            failureRate < 10 -> score -= 10
            failureRate < 20 -> score -= 20
            else -> score -= 30
        }
        
        // Penalize based on timeout rate (0-10 points)
        val timeoutRate = if (result.totalTests > 0) {
            (result.timeoutCount.toDouble() / result.totalTests) * 100
        } else 0.0
        when {
            timeoutRate == 0.0 -> score -= 0
            timeoutRate < 10 -> score -= 5
            else -> score -= 10
        }
        
        return result.copy(score = score.coerceIn(0.0, 100.0))
    }
    
    /**
     * Cancel all running benchmarks.
     */
    fun cancelAll() {
        activeJobs.forEach { it.cancel() }
        activeJobs.clear()
        _progress.value = BenchmarkProgress.Idle
    }
    
    /**
     * Clean up resources.
     */
    fun cleanup() {
        cancelAll()
        coroutineScope.cancel()
    }
}

/**
 * Represents the current state of benchmark progress.
 */
sealed class BenchmarkProgress {
    data object Idle : BenchmarkProgress()
    
    data class Testing(
        val providerName: String,
        val protocol: String,
        val currentTest: Int,
        val totalTests: Int
    ) : BenchmarkProgress()
    
    data class MultiTesting(
        val providerNames: List<String>,
        val completedCount: Int,
        val totalCount: Int
    ) : BenchmarkProgress()
    
    data class Completed(
        val providerName: String,
        val result: BenchmarkResult
    ) : BenchmarkProgress()
    
    data class Error(
        val message: String
    ) : BenchmarkProgress()
}
