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
    val vpnActive by viewModel.vpnActive.collectAsStateWithLifecycle()
    val selectedProvider by viewModel.selectedProvider.collectAsStateWithLifecycle()
    val topProviders by viewModel.topProviders.collectAsStateWithLifecycle()
    val recommendation by viewModel.recommendation.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgVoid)
    ) {
        // Subtle gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            AccentGlow.copy(alpha = 0.03f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // ═══ TOP BAR ═══
            TopBar(
                isConnected = vpnActive,
                networkName = networkInfo.networkName
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ═══ MAIN CONNECTION CIRCLE ═══
            ConnectionCircle(
                isActive = vpnActive,
                isConnecting = uiState.isConnecting,
                providerName = selectedProvider?.name ?: "System DNS",
                onToggle = {
                    if (vpnActive) viewModel.disconnectDns()
                    else selectedProvider?.let { viewModel.applyDns(it) }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ═══ SELECTED DNS INFO ═══
            SelectedDnsPanel(
                provider = selectedProvider,
                isActive = vpnActive,
                networkType = networkInfo.networkType
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ═══ QUICK SELECT ═══
            QuickSelectRow(
                providers = topProviders,
                selectedId = selectedProvider?.id,
                onSelect = { viewModel.selectProvider(it) },
                onSeeAll = onNavigateToDnsList
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ═══ RECOMMENDATION ═══
            recommendation?.let { rec ->
                RecommendCard(
                    name = rec.result.providerName,
                    score = rec.score.toInt(),
                    latency = rec.result.medianLatencyMs.toInt(),
                    protocol = rec.result.protocol.displayName,
                    reason = rec.recommendation,
                    onConnect = {
                        viewModel.selectProvider(
                            com.dnsoptimizer.pro.data.model.DnsProvider(
                                id = rec.result.providerId,
                                name = rec.result.providerName,
                                ipv4Primary = "",
                                ipv4Secondary = ""
                            )
                        )
                        // Trigger connect
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ═══ ACTION GRID ═══
            ActionGrid(
                onBenchmark = onNavigateToBenchmark,
                onDnsList = onNavigateToDnsList,
                onProfiles = { /* navigate to profiles */ }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ═══ NETWORK STATUS ═══
            NetworkBar(
                isConnected = networkInfo.isConnected,
                networkType = networkInfo.networkType,
                ipv6 = networkInfo.supportsIPv6
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun TopBar(isConnected: Boolean, networkName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "DNS Optimizer",
                style = MaterialTheme.typography.headlineMedium,
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Gaming DNS Protection",
                style = MaterialTheme.typography.bodySmall,
                color = TextDim
            )
        }

        // Status pill
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isConnected) StateConnected.copy(alpha = 0.12f) else BgElevated
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isConnected) StateConnected else StateDisconnected)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isConnected) "Protected" else "Unprotected",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isConnected) StateConnected else TextDim,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ConnectionCircle(
    isActive: Boolean,
    isConnecting: Boolean,
    providerName: String,
    onToggle: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "conn")

    // Pulse animation
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Glow opacity
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = if (isActive) 0.35f else 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Rotation for connecting state
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    val ringColor by animateColorAsState(
        targetValue = when {
            isConnecting -> StateConnecting
            isActive -> StateConnected
            else -> StateDisconnected
        },
        animationSpec = tween(400),
        label = "ringColor"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(220.dp)) {
        // Outer glow
        if (isActive) {
            Box(
                modifier = Modifier
                    .size((200 * pulseScale).dp)
                    .clip(CircleShape)
                    .background(StateGlow.copy(alpha = glowAlpha))
            )
        }

        // Ring border
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .border(
                    width = 3.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            ringColor.copy(alpha = 0.8f),
                            ringColor.copy(alpha = 0.2f),
                            ringColor.copy(alpha = 0.8f)
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Inner gradient
        Box(
            modifier = Modifier
                .size(170.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ringColor.copy(alpha = 0.08f),
                            BgDeep
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isConnecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = StateConnecting,
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        imageVector = if (isActive) Icons.Filled.LinkOff else Icons.Filled.Link,
                        contentDescription = null,
                        tint = ringColor,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when {
                        isConnecting -> "Connecting"
                        isActive -> "Connected"
                        else -> "Tap to Connect"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = ringColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    // Provider name below circle
    if (isActive || isConnecting) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = providerName,
            style = MaterialTheme.typography.bodyMedium,
            color = TextGray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SelectedDnsPanel(
    provider: DnsProvider?,
    isActive: Boolean,
    networkType: NetworkType
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BgMid),
        shape = RoundedCornerShape(16.dp),
        border = if (isActive) ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.linearGradient(listOf(BorderConnected, BorderConnected))
        ) else null
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
                            .background(if (isActive) StateConnected else StateDisconnected)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isActive) "Active DNS" else "Selected DNS",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextGray
                    )
                }
                // Network badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (networkType) {
                        NetworkType.WIFI -> Accent.copy(alpha = 0.1f)
                        NetworkType.MOBILE -> Orange.copy(alpha = 0.1f)
                        else -> BgElevated
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            when (networkType) {
                                NetworkType.WIFI -> Icons.Filled.Wifi
                                NetworkType.MOBILE -> Icons.Filled.SignalCellularAlt
                                else -> Icons.Filled.HelpOutline
                            },
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = when (networkType) {
                                NetworkType.WIFI -> Accent
                                NetworkType.MOBILE -> Orange
                                else -> TextDim
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = networkType.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = when (networkType) {
                                NetworkType.WIFI -> Accent
                                NetworkType.MOBILE -> Orange
                                else -> TextDim
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (provider != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = provider.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = provider.ipv4Primary,
                            style = MaterialTheme.typography.bodySmall,
                            color = Accent
                        )
                    }
                    // Category badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BgElevated
                    ) {
                        Text(
                            text = provider.category.name,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextDim
                        )
                    }
                }
            } else {
                Text(
                    text = "Tap a provider below to select",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextDim
                )
            }
        }
    }
}

@Composable
fun QuickSelectRow(
    providers: List<DnsProvider>,
    selectedId: String?,
    onSelect: (DnsProvider) -> Unit,
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
                style = MaterialTheme.typography.labelLarge,
                color = TextGray,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "See All →",
                style = MaterialTheme.typography.labelMedium,
                color = Accent,
                modifier = Modifier.clickable { onSeeAll() }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            providers.take(3).forEach { provider ->
                QuickChip(
                    name = provider.name,
                    ip = provider.ipv4Primary,
                    isSelected = provider.id == selectedId,
                    onClick = { onSelect(provider) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun QuickChip(
    name: String,
    ip: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Accent.copy(alpha = 0.1f) else BgMid,
        border = if (isSelected) ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.linearGradient(listOf(Accent, Accent))
        ) else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = name.take(10),
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) Accent else TextWhite,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = ip,
                style = MaterialTheme.typography.labelSmall,
                color = TextDim,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun RecommendCard(
    name: String,
    score: Int,
    latency: Int,
    protocol: String,
    reason: String,
    onConnect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BgMid),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = Accent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Recommended for You",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextGray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Accent.copy(alpha = 0.04f),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.linearGradient(listOf(BorderAccent, BorderAccent))
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
                                text = name,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = protocol,
                                style = MaterialTheme.typography.labelSmall,
                                color = Accent
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = RateExcellent.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "$score%",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = RateExcellent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem("Latency", "${latency}ms")
                        StatItem("Protocol", protocol)
                        StatItem("Score", "$score%")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDim
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onConnect,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(Icons.Filled.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Connect to $name",
                            color = BgVoid,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = TextWhite,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextDim
        )
    }
}

@Composable
fun ActionGrid(
    onBenchmark: () -> Unit,
    onDnsList: () -> Unit,
    onProfiles: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ActionTile(
            icon = Icons.Filled.Speed,
            label = "Benchmark",
            sub = "Test DNS",
            onClick = onBenchmark,
            modifier = Modifier.weight(1f)
        )
        ActionTile(
            icon = Icons.Filled.Dns,
            label = "DNS List",
            sub = "20+ providers",
            onClick = onDnsList,
            modifier = Modifier.weight(1f)
        )
        ActionTile(
            icon = Icons.Filled.Gamepad,
            label = "Profiles",
            sub = "Gaming presets",
            onClick = onProfiles,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ActionTile(
    icon: ImageVector,
    label: String,
    sub: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = BgMid
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = Accent, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = TextWhite,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = sub,
                style = MaterialTheme.typography.labelSmall,
                color = TextDim
            )
        }
    }
}

@Composable
fun NetworkBar(
    isConnected: Boolean,
    networkType: NetworkType,
    ipv6: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatusDot(
            icon = if (isConnected) Icons.Filled.Wifi else Icons.Filled.WifiOff,
            label = if (isConnected) "Online" else "Offline",
            color = if (isConnected) StateConnected else StateError
        )
        StatusDot(
            icon = when (networkType) {
                NetworkType.WIFI -> Icons.Filled.SignalWifi4Bar
                NetworkType.MOBILE -> Icons.Filled.SignalCellularAlt
                else -> Icons.Filled.HelpOutline
            },
            label = networkType.name,
            color = Accent
        )
        StatusDot(
            icon = Icons.Filled.Language,
            label = if (ipv6) "IPv6" else "IPv4",
            color = if (ipv6) Green else TextDim
        )
    }
}

@Composable
fun StatusDot(
    icon: ImageVector,
    label: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(BgElevated, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}
