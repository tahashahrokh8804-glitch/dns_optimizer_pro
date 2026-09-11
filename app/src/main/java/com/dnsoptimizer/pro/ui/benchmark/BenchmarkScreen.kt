package com.dnsoptimizer.pro.ui.benchmark

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.dnsoptimizer.pro.domain.benchmark.BenchmarkProgress
import com.dnsoptimizer.pro.domain.benchmark.QualityColor
import com.dnsoptimizer.pro.domain.benchmark.RankedResult
import com.dnsoptimizer.pro.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BenchmarkScreen(
    viewModel: BenchmarkViewModel,
    onProviderClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val providers by viewModel.providers.collectAsStateWithLifecycle()
    val selectedProfile by viewModel.selectedProfile.collectAsStateWithLifecycle()

    var sortOption by remember { mutableStateOf(SortOption.SCORE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Benchmark",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Test DNS providers on your network",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.startFullBenchmark() },
                enabled = !uiState.isBenchmarkRunning,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = DarkPrimary)
            ) {
                if (uiState.isBenchmarkRunning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Testing...")
                } else {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Run All Tests")
                }
            }

            if (uiState.isBenchmarkRunning) {
                OutlinedButton(onClick = { viewModel.cancelBenchmark() }) {
                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Progress
        AnimatedVisibility(visible = progress is BenchmarkProgress.Testing || progress is BenchmarkProgress.MultiTesting) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    when (val p = progress) {
                        is BenchmarkProgress.Testing -> {
                            LinearProgressIndicator(
                                progress = { p.currentTest.toFloat() / p.totalTests.coerceAtLeast(1) },
                                modifier = Modifier.fillMaxWidth(),
                                color = DarkPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Testing ${p.providerName} (${p.protocol}) - ${p.currentTest}/${p.totalTests}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        is BenchmarkProgress.MultiTesting -> {
                            LinearProgressIndicator(
                                progress = { p.completedCount.toFloat() / p.totalCount.coerceAtLeast(1) },
                                modifier = Modifier.fillMaxWidth(),
                                color = DarkPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Tested ${p.completedCount}/${p.totalCount} providers",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        else -> {}
                    }
                }
            }
        }

        // Sort chips
        if (results.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SortOption.entries.forEach { option ->
                    FilterChip(
                        selected = sortOption == option,
                        onClick = { sortOption = option },
                        label = { Text(option.label, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = DarkPrimary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Results list
        val sortedResults = remember(results, sortOption) {
            derivedStateOf {
                when (sortOption) {
                    SortOption.SCORE -> results.sortedByDescending { it.score }
                    SortOption.LATENCY -> results.sortedBy { it.result.medianLatencyMs }
                    SortOption.STABILITY -> results.sortedBy { it.result.jitterMs }
                    SortOption.RELIABILITY -> results.sortedByDescending { it.result.successRate }
                    SortOption.NAME -> results.sortedBy { it.result.providerName }
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(sortedResults.value) { ranked ->
                ResultCard(rankedResult = ranked, onClick = { onProviderClick(ranked.result.providerId) })
            }
        }
    }
}

@Composable
fun ResultCard(rankedResult: RankedResult, onClick: () -> Unit) {
    val result = rankedResult.result
    val statusColor = GamingColors.getStatusColor(rankedResult.score)

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Score circle
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(statusColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${rankedResult.score.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.providerName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = result.protocol.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = GamingColors.getProtocolColor(result.protocol.name)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${result.medianLatencyMs.toInt()} ms",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${result.successRate.toInt()}% success",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (result.successRate >= 90) ExcellentGreen else PoorOrange
                )
            }
        }
    }
}

enum class SortOption(val label: String) {
    SCORE("Score"),
    LATENCY("Latency"),
    STABILITY("Stability"),
    RELIABILITY("Reliability"),
    NAME("Name")
}
