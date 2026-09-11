package com.dnsoptimizer.pro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents the result of a DNS benchmark test.
 */
@Entity(tableName = "benchmark_results")
data class BenchmarkResult(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val providerId: String,
    
    val providerName: String,
    
    val protocol: DnsProtocol,
    
    val testDomain: String,
    
    val timestamp: Long = System.currentTimeMillis(),
    
    // Latency metrics (in milliseconds)
    val minLatencyMs: Double = 0.0,
    val maxLatencyMs: Double = 0.0,
    val avgLatencyMs: Double = 0.0,
    val medianLatencyMs: Double = 0.0,
    val p95LatencyMs: Double = 0.0,
    val jitterMs: Double = 0.0,
    
    // Reliability metrics
    val totalTests: Int = 0,
    val successCount: Int = 0,
    val failureCount: Int = 0,
    val timeoutCount: Int = 0,
    val successRate: Double = 0.0,
    
    // Test configuration
    val testCount: Int = 10,
    val timeoutMs: Long = 5000,
    
    // Connection info
    val networkType: NetworkType = NetworkType.WIFI,
    val networkName: String = "",
    val ipAddress: String = "",
    
    // Overall score (0-100)
    val score: Double = 0.0,
    
    // Status
    val status: BenchmarkStatus = BenchmarkStatus.PENDING,
    
    // Error info
    val errorMessage: String? = null,
    
    // DNSSEC validation
    val dnssecSupported: Boolean = false,
    
    // Bootstrap latency (time to establish connection)
    val bootstrapLatencyMs: Double = 0.0,
    
    // Specific protocol metrics
    val handshakeLatencyMs: Double = 0.0,
    val resolutionCorrectness: Boolean = true
)

enum class DnsProtocol(val displayName: String) {
    UDP("DNS over UDP"),
    TCP("DNS over TCP"),
    DOT("DNS over TLS"),
    DOH("DNS over HTTPS"),
    DOH3("DNS over HTTPS/3"),
    DOQ("DNS over QUIC")
}

enum class NetworkType {
    WIFI,
    MOBILE,
    ETHERNET,
    VPN,
    UNKNOWN
}

enum class BenchmarkStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED
}
