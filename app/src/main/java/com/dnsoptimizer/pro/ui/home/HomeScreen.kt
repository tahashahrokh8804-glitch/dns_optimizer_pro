package com.dnsoptimizer.pro.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnsoptimizer.pro.data.model.*
import com.dnsoptimizer.pro.ui.theme.*

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
    val recommendation by viewModel.recommendation.collectAsStateWithLifecycle()
    val vpnActive by viewModel.vpnActive.collectAsStateWithLifecycle()
    val selectedProvider by viewModel.selectedProvider.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundDeep, BackgroundMid, BackgroundDeep)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Text(
                text = "DNS Optimizer",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Gaming DNS Protection",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            // === MAIN CONNECTION BUTTON ===
            ConnectionButton(
                isActive = vpnActive,
                isConnecting = uiState.isConnecting,
                onToggle = {
                    if (vpnActive) {
                        viewModel.disconnectDns()
                    } else {
                        selectedProvider?.let { viewModel.applyDns(it) }
                    }
                }
            )

            // Connection status text
            ConnectionStatusText(
                isActive = vpnActive,
                isConnecting = uiState.isConnecting,
                providerName = selectedProvider?.name ?: currentDns.servers.firstOrNull() ?: "System DNS"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // === CURRENT DNS INFO CARD ===
            DnsInfoCard(
                provider = selectedProvider,
                isActive = vpnActive,
                networkType = networkInfo.networkType,
                networkName = networkInfo.networkName
            )

            // === QUICK DNS SELECTOR ===
            QuickDnsSelector(
                providers = viewModel.topProviders.collectAsStateWithLifecycle().value,
                selectedProvider = selectedProvider,
                onProviderSelected = { viewModel.selectProvider(it) },
                onSeeAll = onNavigateToDnsList
            )

            // === RECOMMENDATION CARD ===
            recommendation?.let { rec ->
                RecommendationCard(
                    rankedResult = rec,
                    onApply = { provider ->
                        viewModel.selectProvider(provider)
                        viewModel.applyDns(provider)
                    }
                )
            }

            // === ACTION BUTTONS ===
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton(
                    icon = Icons.Filled.Speed,
                    label = "Benchmark",
                    subtitle = "Test DNS",
                    onClick = onNavigateToBenchmark,
                    modifier = Modifier.weight(1f)
                )
                ActionButton(
                    icon = Icons.Filled.Dns,
                    label = "DNS List",
                    subtitle = "All providers",
                    onClick = onNavigateToDnsList,
                    modifier = Modifier.weight(1f)
                )
            }

            // === NETWORK STATUS ===
            NetworkStatusFooter(
                isConnected = networkInfo.isConnected,
                networkType = networkInfo.networkType,
                supportsIPv6 = networkInfo.supportsIPv6
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ConnectionButton(
    isActive: Boolean,
    isConnecting: Boolean,
    onToggle: () -> Unit
) {
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val glowAlpha by pulseAnim.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isActive) 0.6f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val buttonColor by animateColorAsState(
        targetValue = when {
            isConnecting -> Connecting
            isActive -> Connected
            else -> Disconnected
        },
        animationSpec = tween(300),
        label = "buttonColor"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(180.dp)
    ) {
        // Outer glow ring
        if (isActive) {
            Box(
                modifier = Modifier
                    .size((180 * pulseScale).dp)
                    .clip(CircleShape)
                    .background(ConnectedGlow.copy(alpha = glowAlpha))
            )
        }

        // Main button
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            buttonColor.copy(alpha = 0.3f),
                            buttonColor.copy(alpha = 0.1f)
                        )
                    )
                )
                .border(3.dp, buttonColor.copy(alpha = 0.5f), CircleShape)
                .clickable { onToggle() },
            contentAlignment = Alignment.Center
        ) {
            if (isConnecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(60.dp),
                    color = Connecting,
                    strokeWidth = 3.dp
                )
            } else {
                Icon(
                    imageVector = if (isActive) Icons.Filled.PowerSettingsNew else Icons.Filled.Power,
                    contentDescription = if (isActive) "Disconnect" else "Connect",
                    tint = buttonColor,
                    modifier = Modifier.size(60.dp)
                )
            }
        }
    }
}

@Composable
fun ConnectionStatusText(
    isActive: Boolean,
    isConnecting: Boolean,
    providerName: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = when {
                isConnecting -> "Connecting..."
                isActive -> "Connected"
                else -> "Disconnected"
            },
            style = MaterialTheme.typography.headlineSmall,
            color = when {
                isConnecting -> Connecting
                isActive -> Connected
                else -> TextMuted
            },
            fontWeight = FontWeight.Bold
        )
        if (isActive || isConnecting) {
            Text(
                text = providerName,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun DnsInfoCard(
    provider: DnsProvider?,
    isActive: Boolean,
    networkType: NetworkType,
    networkName: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isActive) Connected else Disconnected)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Active DNS",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (networkType) {
                        NetworkType.WIFI -> AccentCyan.copy(alpha = 0.1f)
                        NetworkType.MOBILE -> AccentOrange.copy(alpha = 0.1f)
                        else -> SurfaceDark
                    }
                ) {
                    Text(
                        text = networkName,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (networkType) {
                            NetworkType.WIFI -> AccentCyan
                            NetworkType.MOBILE -> AccentOrange
                            else -> TextMuted
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            provider?.let { p ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = p.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = p.ipv4Primary,
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentCyan
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = p.category.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            } ?: run {
                Text(
                    text = "Select a DNS provider to connect",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
fun QuickDnsSelector(
    providers: List<DnsProvider>,
    selectedProvider: DnsProvider?,
    onProviderSelected: (DnsProvider) -> Unit,
    onSeeAll: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quick Select",
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "See All",
                style = MaterialTheme.typography.labelMedium,
                color = AccentCyan,
                modifier = Modifier.clickable { onSeeAll() }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            providers.take(3).forEach { provider ->
                QuickDnsChip(
                    provider = provider,
                    isSelected = selectedProvider?.id == provider.id,
                    onClick = { onProviderSelected(provider) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun QuickDnsChip(
    provider: DnsProvider,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) AccentCyan.copy(alpha = 0.15f) else CardBackground,
        border = if (isSelected) ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.linearGradient(listOf(AccentCyan, AccentCyan))
        ) else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = provider.name.take(8),
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) AccentCyan else TextPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(
                text = provider.ipv4Primary,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun RecommendationCard(
    rankedResult: com.dnsoptimizer.pro.domain.benchmark.RankedResult,
    onApply: (DnsProvider) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Recommended for Your Network",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AccentCyan.copy(alpha = 0.05f),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.linearGradient(listOf(AccentCyan.copy(alpha = 0.3f), AccentBlue.copy(alpha = 0.3f)))
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = rankedResult.result.providerName,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = rankedResult.result.protocol.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentCyan
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = QualityExcellent.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${rankedResult.score.toInt()}%",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = QualityExcellent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MetricItem("Median", "${rankedResult.result.medianLatencyMs.toInt()} ms")
                        MetricItem("Min", "${rankedResult.result.minLatencyMs.toInt()} ms")
                        MetricItem("Success", "${rankedResult.result.successRate.toInt()}%")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Why: ${rankedResult.recommendation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { /* Will be wired to apply */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Connect to ${rankedResult.result.providerName}", color = BackgroundDeep, fontWeight = FontWeight.Bold)
                    }
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
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    label: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = AccentCyan,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
fun NetworkStatusFooter(
    isConnected: Boolean,
    networkType: NetworkType,
    supportsIPv6: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatusChip(
            icon = if (isConnected) Icons.Filled.Wifi else Icons.Filled.WifiOff,
            label = if (isConnected) "Online" else "Offline",
            color = if (isConnected) Connected else Error
        )
        StatusChip(
            icon = when (networkType) {
                NetworkType.WIFI -> Icons.Filled.SignalWifi4Bar
                NetworkType.MOBILE -> Icons.Filled.SignalCellularAlt
                else -> Icons.Filled.HelpOutline
            },
            label = networkType.name,
            color = AccentCyan
        )
        StatusChip(
            icon = Icons.Filled.Language,
            label = if (supportsIPv6) "IPv6" else "IPv4",
            color = if (supportsIPv6) AccentGreen else TextMuted
        )
    }
}

@Composable
fun StatusChip(
    icon: ImageVector,
    label: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(SurfaceDark, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
