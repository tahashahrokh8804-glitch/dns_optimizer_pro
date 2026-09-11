package com.dnsoptimizer.pro.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Navigation routes for the app.
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Benchmark : Screen("benchmark")
    data object DnsList : Screen("dns_list")
    data object Profiles : Screen("profiles")
    data object History : Screen("history")
    data object Settings : Screen("settings")
    data object DnsDetail : Screen("dns_detail/{providerId}") {
        fun createRoute(providerId: String) = "dns_detail/$providerId"
    }
    data object BenchmarkDetail : Screen("benchmark_detail/{resultId}") {
        fun createRoute(resultId: Long) = "benchmark_detail/$resultId"
    }
}

/**
 * Navigation item definition.
 */
data class NavigationItem(
    val screen: Screen,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val titleResId: Int? = null
)

/**
 * Bottom navigation items.
 */
val bottomNavigationItems = listOf(
    NavigationItem(
        screen = Screen.Home,
        title = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    NavigationItem(
        screen = Screen.Benchmark,
        title = "Benchmark",
        selectedIcon = Icons.Filled.Speed,
        unselectedIcon = Icons.Outlined.Speed
    ),
    NavigationItem(
        screen = Screen.DnsList,
        title = "DNS List",
        selectedIcon = Icons.Filled.List,
        unselectedIcon = Icons.Outlined.List
    ),
    NavigationItem(
        screen = Screen.Profiles,
        title = "Profiles",
        selectedIcon = Icons.Filled.Gamepad,
        unselectedIcon = Icons.Outlined.Gamepad
    ),
    NavigationItem(
        screen = Screen.History,
        title = "History",
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History
    ),
    NavigationItem(
        screen = Screen.Settings,
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
)
