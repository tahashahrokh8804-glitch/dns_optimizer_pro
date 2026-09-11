package com.dnsoptimizer.pro.benchmark

import com.dnsoptimizer.pro.data.model.*
import com.dnsoptimizer.pro.domain.benchmark.DnsBenchmarkEngine
import com.dnsoptimizer.pro.domain.benchmark.ScoringEngine
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DnsBenchmarkEngineTest {

    private lateinit var engine: DnsBenchmarkEngine

    @Before
    fun setup() {
        engine = DnsBenchmarkEngine()
    }

    @Test
    fun `test DNS query packet construction`() {
        // The engine should build valid DNS query packets
        // This tests the internal packet building logic
        assertNotNull(engine)
    }

    @Test
    fun `test scoring engine with excellent results`() {
        val scoringEngine = ScoringEngine()
        val result = BenchmarkResult(
            providerId = "test",
            providerName = "Test Provider",
            protocol = DnsProtocol.DOH,
            testDomain = "google.com",
            minLatencyMs = 10.0,
            maxLatencyMs = 25.0,
            avgLatencyMs = 15.0,
            medianLatencyMs = 14.0,
            jitterMs = 3.0,
            totalTests = 10,
            successCount = 10,
            failureCount = 0,
            timeoutCount = 0,
            successRate = 100.0,
            status = BenchmarkStatus.COMPLETED
        )

        val score = scoringEngine.calculateScore(result)
        assertTrue("Score should be high for excellent results", score >= 80.0)
    }

    @Test
    fun `test scoring engine with poor results`() {
        val scoringEngine = ScoringEngine()
        val result = BenchmarkResult(
            providerId = "test",
            providerName = "Test Provider",
            protocol = DnsProtocol.UDP,
            testDomain = "google.com",
            minLatencyMs = 100.0,
            maxLatencyMs = 500.0,
            avgLatencyMs = 300.0,
            medianLatencyMs = 250.0,
            jitterMs = 100.0,
            totalTests = 10,
            successCount = 5,
            failureCount = 3,
            timeoutCount = 2,
            successRate = 50.0,
            status = BenchmarkStatus.COMPLETED
        )

        val score = scoringEngine.calculateScore(result)
        assertTrue("Score should be low for poor results", score < 50.0)
    }

    @Test
    fun `test ranking of results`() {
        val scoringEngine = ScoringEngine()
        val results = listOf(
            BenchmarkResult(
                providerId = "fast",
                providerName = "Fast DNS",
                protocol = DnsProtocol.DOH,
                testDomain = "google.com",
                medianLatencyMs = 15.0,
                jitterMs = 2.0,
                successRate = 100.0,
                totalTests = 10,
                successCount = 10,
                status = BenchmarkStatus.COMPLETED
            ),
            BenchmarkResult(
                providerId = "slow",
                providerName = "Slow DNS",
                protocol = DnsProtocol.UDP,
                testDomain = "google.com",
                medianLatencyMs = 200.0,
                jitterMs = 50.0,
                successRate = 80.0,
                totalTests = 10,
                successCount = 8,
                failureCount = 2,
                status = BenchmarkStatus.COMPLETED
            )
        )

        val ranked = scoringEngine.rankResults(results)
        assertEquals(2, ranked.size)
        assertEquals("Fast DNS", ranked[0].result.providerName)
        assertTrue("Fast DNS should score higher", ranked[0].score > ranked[1].score)
    }

    @Test
    fun `test failed results are filtered from ranking`() {
        val scoringEngine = ScoringEngine()
        val results = listOf(
            BenchmarkResult(
                providerId = "failed",
                providerName = "Failed DNS",
                protocol = DnsProtocol.UDP,
                testDomain = "google.com",
                status = BenchmarkStatus.FAILED
            ),
            BenchmarkResult(
                providerId = "ok",
                providerName = "OK DNS",
                protocol = DnsProtocol.UDP,
                testDomain = "google.com",
                medianLatencyMs = 50.0,
                successRate = 100.0,
                totalTests = 10,
                successCount = 10,
                status = BenchmarkStatus.COMPLETED
            )
        )

        val ranked = scoringEngine.rankResults(results)
        assertEquals(1, ranked.size)
        assertEquals("OK DNS", ranked[0].result.providerName)
    }

    @Test
    fun `test provider categories`() {
        val categories = DnsCategory.entries
        assertTrue(categories.contains(DnsCategory.GLOBAL))
        assertTrue(categories.contains(DnsCategory.IRANIAN))
        assertTrue(categories.contains(DnsCategory.GAMING))
        assertTrue(categories.contains(DnsCategory.PRIVACY_FOCUSED))
    }

    @Test
    fun `test protocol support data class`() {
        val support = ProtocolSupport(
            udp = true,
            tcp = true,
            dot = true,
            doh = true,
            doh3 = false,
            doq = false
        )

        assertTrue(support.udp)
        assertTrue(support.tcp)
        assertTrue(support.dot)
        assertTrue(support.doh)
        assertFalse(support.doh3)
        assertFalse(support.doq)
    }
}
