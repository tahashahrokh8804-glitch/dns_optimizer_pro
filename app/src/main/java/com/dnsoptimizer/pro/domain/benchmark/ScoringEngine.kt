package com.dnsoptimizer.pro.domain.benchmark

import com.dnsoptimizer.pro.data.model.BenchmarkResult
import com.dnsoptimizer.pro.data.model.DnsProvider

/**
 * Scoring engine for ranking DNS providers based on benchmark results.
 * 
 * The scoring considers multiple factors:
 * - Median latency (most important)
 * - Stability (jitter)
 * - Reliability (success rate)
 * - Timeout rate
 * - Protocol support
 * - Current network conditions
 * 
 * This engine generates recommendations based on actual test results,
 * not static rankings.
 */
class ScoringEngine {
    
    companion object {
        // Weight factors for scoring (must sum to 1.0)
        private const val WEIGHT_LATENCY = 0.40
        private const val WEIGHT_STABILITY = 0.20
        private const val WEIGHT_RELIABILITY = 0.25
        private const val WEIGHT_TIMEOUT = 0.10
        private const val WEIGHT_PROTOCOL = 0.05
        
        // Thresholds for scoring
        private const val EXCELLENT_LATENCY_MS = 20.0
        private const val GOOD_LATENCY_MS = 50.0
        private const val FAIR_LATENCY_MS = 100.0
        private const val POOR_LATENCY_MS = 200.0
        
        private const val EXCELLENT_JITTER_MS = 5.0
        private const val GOOD_JITTER_MS = 15.0
        private const val FAIR_JITTER_MS = 30.0
    }
    
    /**
     * Calculate comprehensive score for a benchmark result.
     * Returns a score between 0 and 100.
     */
    fun calculateScore(result: BenchmarkResult): Double {
        val latencyScore = calculateLatencyScore(result.medianLatencyMs)
        val stabilityScore = calculateStabilityScore(result.jitterMs)
        val reliabilityScore = calculateReliabilityScore(result.successRate)
        val timeoutScore = calculateTimeoutScore(result.timeoutCount, result.totalTests)
        val protocolScore = calculateProtocolScore(result.protocol)
        
        val totalScore = (latencyScore * WEIGHT_LATENCY) +
                (stabilityScore * WEIGHT_STABILITY) +
                (reliabilityScore * WEIGHT_RELIABILITY) +
                (timeoutScore * WEIGHT_TIMEOUT) +
                (protocolScore * WEIGHT_PROTOCOL)
        
        return totalScore.coerceIn(0.0, 100.0)
    }
    
    /**
     * Calculate latency score (0-100).
     */
    private fun calculateLatencyScore(latencyMs: Double): Double {
        return when {
            latencyMs <= EXCELLENT_LATENCY_MS -> 100.0
            latencyMs <= GOOD_LATENCY_MS -> 80.0 + (100.0 - latencyMs) / (GOOD_LATENCY_MS - EXCELLENT_LATENCY_MS) * 20.0
            latencyMs <= FAIR_LATENCY_MS -> 60.0 + (GOOD_LATENCY_MS - latencyMs) / (FAIR_LATENCY_MS - GOOD_LATENCY_MS) * 20.0
            latencyMs <= POOR_LATENCY_MS -> 40.0 + (FAIR_LATENCY_MS - latencyMs) / (POOR_LATENCY_MS - FAIR_LATENCY_MS) * 20.0
            else -> (200.0 - latencyMs) / 100.0 * 40.0
        }.coerceIn(0.0, 100.0)
    }
    
    /**
     * Calculate stability score based on jitter (0-100).
     */
    private fun calculateStabilityScore(jitterMs: Double): Double {
        return when {
            jitterMs <= EXCELLENT_JITTER_MS -> 100.0
            jitterMs <= GOOD_JITTER_MS -> 80.0 + (EXCELLENT_JITTER_MS - jitterMs) / (GOOD_JITTER_MS - EXCELLENT_JITTER_MS) * 20.0
            jitterMs <= FAIR_JITTER_MS -> 60.0 + (GOOD_JITTER_MS - jitterMs) / (FAIR_JITTER_MS - GOOD_JITTER_MS) * 20.0
            else -> (30.0 - jitterMs) / 30.0 * 60.0
        }.coerceIn(0.0, 100.0)
    }
    
    /**
     * Calculate reliability score based on success rate (0-100).
     */
    private fun calculateReliabilityScore(successRate: Double): Double {
        return when {
            successRate >= 100.0 -> 100.0
            successRate >= 90.0 -> 80.0 + (successRate - 90.0) / 10.0 * 20.0
            successRate >= 70.0 -> 60.0 + (successRate - 70.0) / 20.0 * 20.0
            successRate >= 50.0 -> 40.0 + (successRate - 50.0) / 20.0 * 20.0
            else -> successRate / 50.0 * 40.0
        }.coerceIn(0.0, 100.0)
    }
    
    /**
     * Calculate timeout penalty score (0-100).
     */
    private fun calculateTimeoutScore(timeoutCount: Int, totalTests: Int): Double {
        if (totalTests == 0) return 0.0
        val timeoutRate = (timeoutCount.toDouble() / totalTests) * 100
        
        return when {
            timeoutRate == 0.0 -> 100.0
            timeoutRate < 10.0 -> 80.0
            timeoutRate < 20.0 -> 60.0
            timeoutRate < 30.0 -> 40.0
            else -> 20.0
        }
    }
    
    /**
     * Calculate protocol bonus score (0-100).
     */
    private fun calculateProtocolScore(protocol: com.dnsoptimizer.pro.data.model.DnsProtocol): Double {
        return when (protocol) {
            com.dnsoptimizer.pro.data.model.DnsProtocol.DOQ -> 100.0 // QUIC is fastest
            com.dnsoptimizer.pro.data.model.DnsProtocol.DOH3 -> 95.0  // HTTP/3
            com.dnsoptimizer.pro.data.model.DnsProtocol.DOH -> 85.0   // HTTPS
            com.dnsoptimizer.pro.data.model.DnsProtocol.DOT -> 80.0   // TLS
            com.dnsoptimizer.pro.data.model.DnsProtocol.TCP -> 70.0   // TCP
            com.dnsoptimizer.pro.data.model.DnsProtocol.UDP -> 60.0   // UDP (baseline)
        }
    }
    
    /**
     * Rank multiple benchmark results and return recommendations.
     */
    fun rankResults(results: List<BenchmarkResult>): List<RankedResult> {
        return results
            .filter { it.status == com.dnsoptimizer.pro.data.model.BenchmarkStatus.COMPLETED }
            .map { result ->
                RankedResult(
                    result = result,
                    score = calculateScore(result),
                    recommendation = generateRecommendation(result)
                )
            }
            .sortedByDescending { it.score }
    }
    
    /**
     * Generate a human-readable recommendation reason.
     */
    private fun generateRecommendation(result: BenchmarkResult): String {
        val reasons = mutableListOf<String>()
        
        // Latency
        when {
            result.medianLatencyMs < EXCELLENT_LATENCY_MS -> reasons.add("Excellent latency")
            result.medianLatencyMs < GOOD_LATENCY_MS -> reasons.add("Good latency")
            result.medianLatencyMs < FAIR_LATENCY_MS -> reasons.add("Moderate latency")
            else -> reasons.add("High latency")
        }
        
        // Stability
        when {
            result.jitterMs < EXCELLENT_JITTER_MS -> reasons.add("Very stable")
            result.jitterMs < GOOD_JITTER_MS -> reasons.add("Stable connection")
            else -> reasons.add("Variable performance")
        }
        
        // Reliability
        when {
            result.successRate >= 100.0 -> reasons.add("100% success rate")
            result.successRate >= 90.0 -> reasons.add("High reliability")
            result.successRate >= 70.0 -> reasons.add("Moderate reliability")
            else -> reasons.add("Low reliability")
        }
        
        // Protocol
        reasons.add("Using ${result.protocol.displayName}")
        
        return reasons.joinToString(" • ")
    }
}

/**
 * Represents a ranked benchmark result with score and recommendation.
 */
data class RankedResult(
    val result: BenchmarkResult,
    val score: Double,
    val recommendation: String
) {
    val qualityRating: String
        get() = when {
            score >= 90 -> "Excellent"
            score >= 75 -> "Good"
            score >= 60 -> "Average"
            score >= 40 -> "Poor"
            else -> "Failed"
        }
    
    val qualityColor: QualityColor
        get() = when {
            score >= 90 -> QualityColor.EXCELLENT
            score >= 75 -> QualityColor.GOOD
            score >= 60 -> QualityColor.AVERAGE
            score >= 40 -> QualityColor.POOR
            else -> QualityColor.FAILED
        }
}

enum class QualityColor {
    EXCELLENT,
    GOOD,
    AVERAGE,
    POOR,
    FAILED
}
