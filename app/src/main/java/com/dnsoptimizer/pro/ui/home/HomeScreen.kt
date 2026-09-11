package com.dnsoptimizer.pro.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnsoptimizer.pro.data.model.DnsProvider
import com.dnsoptimizer.pro.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToBenchmark: () -> Unit,
    onNavigateToDnsList: () -> Unit,
    onProviderSelected: (DnsProvider) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val networkInfo by viewModel.networkInfo.collectAsStateWithLifecycle()
    val currentDns by viewModel.currentDns.collectAsStateWithLifecycle()
    val benchmarkProgress by viewModel.benchmarkProgress.collectAsStateWithLifecycle()
    val recommendation by viewModel.recommendation.collectAsStateWithLifecycle()
    val vpnActive by viewModel.vpnActive.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "DNS Optimizer Pro",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Gaming DNS Optimization",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Network Status Card
        NetworkStatusCard(
            networkInfo = networkInfo,
            vpnActive = vpnActive
        )

        // Current DNS Card
        CurrentDnsCard(
            currentDns = currentDns,
            vpnActive = vpnActive,
            onDisconnect = { viewModel.disconnectDns() }
        )

        // Recommendation Card
        recommendation?.let { rec ->
            RecommendationCard(
                rankedResult = rec,
                onApply = { provider -> onProviderSelected(provider) },
                onDetails = { onNavigateToDnsList() }
            )
        }

        // Quick Benchmark Card
        QuickBenchmarkCard(
            isRunning = uiState.isBenchmarkRunning,
            lastScore = uiState.lastBenchmarkScore,
            onStartBenchmark = { viewModel.startQuickBenchmark() },
            onSeeAll = { onNavigateToBenchmark() }
        )

        // Error message
        uiState.errorMessage?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.clearError() }) {
                        Icon(Icons.Filled.Close, contentDescription = "Dismiss")
                    }
                }
            }
        }
    }
}

@Composable
fun NetworkStatusCard(
    networkInfo: com.dnsoptimizer.pro.data.model.NetworkInfo,
    vpnActive: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(if (networkInfo.isConnected) ExcellentGreen else FailedRed)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Network Status",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when (networkInfo.networkType) {
                        com.dnsoptimizer.pro.data.model.NetworkType.WIFI -> WifiColor.copy(alpha = 0.2f)
                        com.dnsoptimizer.pro.data.model.NetworkType.MOBILE -> MobileColor.copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = networkInfo.networkType.name,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (networkInfo.networkType) {
                            com.dnsoptimizer.pro.data.model.NetworkType.WIFI -> WifiColor
                            com.dnsoptimizer.pro.data.model.NetworkType.MOBILE -> MobileColor
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Network",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = networkInfo.networkName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "IPv6",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = if (networkInfo.supportsIPv6) "Available" else "Not Available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (networkInfo.supportsIPv6) ExcellentGreen else PoorOrange
                    )
                }
            }

            if (vpnActive) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DarkPrimary.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.VpnKey,
                            contentDescription = null,
                            tint = DarkPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "VPN Active - DNS Routing Enabled",
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CurrentDnsCard(
    currentDns: com.dnsoptimizer.pro.data.model.CurrentDnsConfig,
    vpnActive: Boolean,
    onDisconnect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Dns,
                    contentDescription = null,
                    tint = DarkPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Current DNS",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = currentDns.source.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = DarkPrimary
                    )
                }
                if (vpnActive) {
                    TextButton(onClick = onDisconnect) {
                        Text("Disconnect")
                    }
                }
            }

            if (currentDns.servers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                currentDns.servers.forEach { server ->
                    Text(
                        text = server,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun RecommendationCard(
    rankedResult: com.dnsoptimizer.pro.domain.benchmark.RankedResult,
    onApply: (DnsProvider) -> Unit,
    onDetails: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = null,
                    tint = ExcellentGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Recommended for Your Network",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ExcellentGreen.copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = rankedResult.result.providerName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = rankedResult.result.protocol.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = DarkPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem("Median", "${rankedResult.result.medianLatencyMs.toInt()} ms")
                        MetricItem("Min", "${rankedResult.result.minLatencyMs.toInt()} ms")
                        MetricItem("Max", "${rankedResult.result.maxLatencyMs.toInt()} ms")
                        MetricItem("Success", "${rankedResult.result.successRate.toInt()}%")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Why: ${rankedResult.recommendation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onDetails() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkPrimary)
                ) {
                    Icon(Icons.Filled.List, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View All")
                }
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun QuickBenchmarkCard(
    isRunning: Boolean,
    lastScore: Double,
    onStartBenchmark: () -> Unit,
    onSeeAll: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Speed,
                    contentDescription = null,
                    tint = DarkSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Quick Benchmark",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (lastScore > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Last Score",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "${lastScore.toInt()}/100",
                            style = MaterialTheme.typography.titleLarge,
                            color = when {
                                lastScore >= 90 -> ExcellentGreen
                                lastScore >= 75 -> GoodGreen
                                lastScore >= 60 -> AverageYellow
                                else -> PoorOrange
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onStartBenchmark,
                    enabled = !isRunning,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSecondary)
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Testing...")
                    } else {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Benchmark")
                    }
                }
            }
        }
    }
}
