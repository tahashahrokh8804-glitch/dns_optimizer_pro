package com.dnsoptimizer.pro.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    primaryContainer = DarkPrimaryVariant,
    secondary = DarkSecondary,
    secondaryContainer = DarkSecondaryVariant,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = DarkOnPrimary,
    onSecondary = DarkOnSecondary,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    error = FailedRed,
    onError = DarkOnPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    primaryContainer = LightPrimaryVariant,
    secondary = LightSecondary,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = LightOnBackground,
    onSecondary = DarkOnPrimary,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    error = FailedRed,
    onError = LightOnBackground
)

@Composable
fun DnsOptimizerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * Gaming-specific color utilities
 */
object GamingColors {
    @Composable
    fun getStatusColor(score: Double): androidx.compose.ui.graphics.Color {
        return when {
            score >= 90 -> ExcellentGreen
            score >= 75 -> GoodGreen
            score >= 60 -> AverageYellow
            score >= 40 -> PoorOrange
            else -> FailedRed
        }
    }
    
    @Composable
    fun getStatusText(score: Double): String {
        return when {
            score >= 90 -> "Excellent"
            score >= 75 -> "Good"
            score >= 60 -> "Average"
            score >= 40 -> "Poor"
            else -> "Failed"
        }
    }
    
    @Composable
    fun getProtocolColor(protocol: String): androidx.compose.ui.graphics.Color {
        return when (protocol.uppercase()) {
            "UDP" -> UdpColor
            "TCP" -> TcpColor
            "DOT" -> DotColor
            "DOH" -> DohColor
            "DOH3" -> Doh3Color
            "DOQ" -> DoqColor
            else -> DarkPrimary
        }
    }
}
