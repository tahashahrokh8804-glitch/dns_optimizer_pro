package com.dnsoptimizer.pro.ui.theme

import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════
// DEEP SPACE GAMING PALETTE
// ═══════════════════════════════════════════

// Backgrounds — layered darkness
val BgVoid = Color(0xFF030712)        // deepest black
val BgDeep = Color(0xFF0A0F1C)        // main background
val BgMid = Color(0xFF111827)         // cards / surfaces
val BgElevated = Color(0xFF1A2332)    // elevated cards
val BgHighlight = Color(0xFF1F2937)   // hover / pressed

// Accent — electric cyan gaming glow
val Accent = Color(0xFF00E5FF)        // primary accent
val AccentDim = Color(0xFF00B8D4)     // dimmed accent
val AccentGlow = Color(0x3300E5FF)    // glow background
val AccentSoft = Color(0x1A00E5FF)    // soft background

// Secondary accents
val Blue = Color(0xFF3B82F6)
val Purple = Color(0xFF8B5CF6)
val Green = Color(0xFF10B981)
val Yellow = Color(0xFFFBBF24)
val Orange = Color(0xFFF97316)
val Red = Color(0xFFEF4444)
val Pink = Color(0xFFEC4899)

// Connection states
val StateConnected = Color(0xFF10B981)
val StateConnecting = Color(0xFFFBBF24)
val StateDisconnected = Color(0xFF4B5563)
val StateError = Color(0xFFEF4444)
val StateGlow = Color(0x4010B981)

// Text
val TextWhite = Color(0xFFF9FAFB)
val TextGray = Color(0xFF9CA3AF)
val TextDim = Color(0xFF6B7280)
val TextAccent = Accent

// Protocol badges
val ProtoUdp = Color(0xFF00E5FF)
val ProtoTcp = Color(0xFF8B5CF6)
val ProtoDot = Color(0xFFFBBF24)
val ProtoDoh = Color(0xFF10B981)
val ProtoDoh3 = Color(0xFF3B82F6)
val ProtoDoq = Color(0xFFEC4899)

// Quality rating
val RateExcellent = Color(0xFF10B981)
val RateGood = Color(0xFF34D399)
val RateAverage = Color(0xFFFBBF24)
val RatePoor = Color(0xFFF97316)
val RateFailed = Color(0xFFEF4444)

// Gradient presets
val GradientCyan = listOf(Accent, Blue)
val GradientPurple = listOf(Purple, Blue)
val GradientGreen = listOf(Green, Accent)
val GradientConnected = listOf(Color(0xFF059669), StateConnected, Color(0xFF6EE7B7))
val GradientCard = listOf(BgMid, BgElevated)

// Borders
val BorderSubtle = Color(0xFF1F2937)
val BorderAccent = Accent.copy(alpha = 0.3f)
val BorderConnected = StateConnected.copy(alpha = 0.4f)

// ═══ LEGACY ALIASES (backward compat) ═══
val DarkPrimary = Accent
val DarkPrimaryVariant = AccentDim
val DarkSecondary = Orange
val DarkSecondaryVariant = Yellow
val DarkSurface = BgMid
val DarkSurfaceVariant = BgElevated
val DarkBackground = BgDeep
val DarkOnSurface = TextWhite
val DarkOnBackground = TextWhite
val DarkOnPrimary = BgVoid
val DarkOnSecondary = BgVoid
val ExcellentGreen = RateExcellent
val GoodGreen = RateGood
val AverageYellow = RateAverage
val PoorOrange = RatePoor
val FailedRed = StateError
val UdpColor = ProtoUdp
val TcpColor = ProtoTcp
val DotColor = ProtoDot
val DohColor = ProtoDoh
val Doh3Color = ProtoDoh3
val DoqColor = ProtoDoq
val WifiColor = Accent
val MobileColor = Orange
val EthernetColor = Green
val VpnColor = Purple
val CardBackground = BgMid
val CardBorder = BorderSubtle
val CardHighlight = BgElevated
val LightPrimary = Color(0xFF00695C)
val LightPrimaryVariant = Color(0xFF004D40)
val LightSecondary = Orange
val LightSurface = Color(0xFFFAFAFA)
val LightBackground = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF1A1A1A)
val LightOnBackground = Color(0xFF000000)
val GradientStart = BgDeep
val GradientMiddle = BgMid
val GradientEnd = BgElevated
