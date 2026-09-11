package com.dnsoptimizer.pro.ui.theme

import androidx.compose.ui.graphics.Color

// === DARK GAMING PALETTE ===
// Deep space black backgrounds
val BackgroundDeep = Color(0xFF050810)
val BackgroundMid = Color(0xFF0B1120)
val BackgroundLight = Color(0xFF111827)
val SurfaceDark = Color(0xFF1A2332)
val SurfaceMid = Color(0xFF1F2937)
val SurfaceLight = Color(0xFF283548)

// Accent colors
val AccentCyan = Color(0xFF00D4FF)
val AccentCyanDim = Color(0xFF00A3CC)
val AccentBlue = Color(0xFF3B82F6)
val AccentPurple = Color(0xFF8B5CF6)
val AccentGreen = Color(0xFF10B981)
val AccentOrange = Color(0xFFF59E0B)
val AccentRed = Color(0xFFEF4444)

// Connection states
val Connected = Color(0xFF10B981)
val ConnectedGlow = Color(0x3310B981)
val Disconnected = Color(0xFF6B7280)
val Connecting = Color(0xFFF59E0B)
val Error = Color(0xFFEF4444)

// Text
val TextPrimary = Color(0xFFF9FAFB)
val TextSecondary = Color(0xFF9CA3AF)
val TextMuted = Color(0xFF6B7280)
val TextAccent = AccentCyan

// Protocol colors
val ProtocolUdp = Color(0xFF00D4FF)
val ProtocolTcp = Color(0xFF8B5CF6)
val ProtocolDot = Color(0xFFF59E0B)
val ProtocolDoh = Color(0xFF10B981)
val ProtocolDoh3 = Color(0xFF3B82F6)
val ProtocolDoq = Color(0xFFEC4899)

// Quality colors
val QualityExcellent = Color(0xFF10B981)
val QualityGood = Color(0xFF34D399)
val QualityAverage = Color(0xFFFBBF24)
val QualityPoor = Color(0xFFF97316)
val QualityFailed = Color(0xFFEF4444)

// Gradient accents
val GradientCyan = listOf(AccentCyan, AccentBlue)
val GradientPurple = listOf(AccentPurple, AccentBlue)
val GradientGreen = listOf(AccentGreen, AccentCyan)
val GradientConnected = listOf(Color(0xFF059669), Color(0xFF10B981), Color(0xFF34D399))

// Card
val CardBackground = Color(0xFF111827)
val CardBorder = Color(0xFF1F2937)
val CardHighlight = Color(0xFF1A2332)

// === LEGACY ALIASES (backward compatibility with existing screens) ===
val DarkPrimary = AccentCyan
val DarkPrimaryVariant = AccentCyanDim
val DarkSecondary = AccentOrange
val DarkSecondaryVariant = Color(0xFFFBBF24)
val DarkSurface = SurfaceDark
val DarkSurfaceVariant = SurfaceMid
val DarkBackground = BackgroundDeep
val DarkOnSurface = TextPrimary
val DarkOnBackground = TextPrimary
val DarkOnPrimary = BackgroundDeep
val DarkOnSecondary = BackgroundDeep
val ExcellentGreen = QualityExcellent
val GoodGreen = QualityGood
val AverageYellow = QualityAverage
val PoorOrange = QualityPoor
val FailedRed = AccentRed
val UdpColor = ProtocolUdp
val TcpColor = ProtocolTcp
val DotColor = ProtocolDot
val DohColor = ProtocolDoh
val Doh3Color = ProtocolDoh3
val DoqColor = ProtocolDoq
val WifiColor = AccentCyan
val MobileColor = AccentOrange
val EthernetColor = AccentGreen
val VpnColor = AccentPurple
val LightPrimary = Color(0xFF00695C)
val LightPrimaryVariant = Color(0xFF004D40)
val LightSecondary = AccentOrange
val LightSurface = Color(0xFFFAFAFA)
val LightBackground = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF1A1A1A)
val LightOnBackground = Color(0xFF000000)
val GradientStart = BackgroundDeep
val GradientMiddle = BackgroundMid
val GradientEnd = SurfaceDark
