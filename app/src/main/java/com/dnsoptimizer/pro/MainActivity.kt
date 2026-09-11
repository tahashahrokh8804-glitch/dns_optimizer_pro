package com.dnsoptimizer.pro

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dnsoptimizer.pro.data.model.DnsProvider
import com.dnsoptimizer.pro.domain.vpn.DnsVpnService
import com.dnsoptimizer.pro.ui.benchmark.BenchmarkScreen
import com.dnsoptimizer.pro.ui.benchmark.BenchmarkViewModel
import com.dnsoptimizer.pro.ui.dnslist.DnsListScreen
import com.dnsoptimizer.pro.ui.dnslist.DnsListViewModel
import com.dnsoptimizer.pro.ui.history.HistoryScreen
import com.dnsoptimizer.pro.ui.history.HistoryViewModel
import com.dnsoptimizer.pro.ui.home.HomeScreen
import com.dnsoptimizer.pro.ui.home.HomeViewModel
import com.dnsoptimizer.pro.ui.navigation.*
import com.dnsoptimizer.pro.ui.profiles.ProfilesScreen
import com.dnsoptimizer.pro.ui.profiles.ProfilesViewModel
import com.dnsoptimizer.pro.ui.settings.SettingsScreen
import com.dnsoptimizer.pro.ui.settings.SettingsViewModel
import com.dnsoptimizer.pro.ui.theme.DnsOptimizerTheme

class MainActivity : ComponentActivity() {

    private var pendingDnsProvider: DnsProvider? = null

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            pendingDnsProvider?.let { provider ->
                val intent = Intent(this, DnsVpnService::class.java).apply {
                    action = DnsVpnService.ACTION_START
                    putExtra(DnsVpnService.EXTRA_DNS_SERVER, provider.ipv4Primary)
                }
                startForegroundService(intent)
            }
        }
        pendingDnsProvider = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DnsOptimizerTheme(darkTheme = true) {
                DnsOptimizerApp()
            }
        }
    }

    @Composable
    fun DnsOptimizerApp() {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val homeViewModel: HomeViewModel = viewModel()
        val benchmarkViewModel: BenchmarkViewModel = viewModel()
        val dnsListViewModel: DnsListViewModel = viewModel()
        val profilesViewModel: ProfilesViewModel = viewModel()
        val historyViewModel: HistoryViewModel = viewModel()
        val settingsViewModel: SettingsViewModel = viewModel()

        Scaffold(
            bottomBar = {
                NavigationBar {
                    bottomNavigationItems.forEach { item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (currentRoute == item.screen.route) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) },
                            selected = currentRoute == item.screen.route,
                            onClick = {
                                if (currentRoute != item.screen.route) {
                                    navController.navigate(item.screen.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onNavigateToBenchmark = { navController.navigate(Screen.Benchmark.route) },
                        onNavigateToDnsList = { navController.navigate(Screen.DnsList.route) },
                        onProviderSelected = { provider ->
                            requestVpnPermission(provider)
                        }
                    )
                }

                composable(Screen.Benchmark.route) {
                    BenchmarkScreen(
                        viewModel = benchmarkViewModel,
                        onProviderClick = { /* Show details */ }
                    )
                }

                composable(Screen.DnsList.route) {
                    DnsListScreen(
                        viewModel = dnsListViewModel,
                        onProviderSelected = { provider ->
                            requestVpnPermission(provider)
                        }
                    )
                }

                composable(Screen.Profiles.route) {
                    ProfilesScreen(
                        viewModel = profilesViewModel,
                        onProfileSelected = { profile ->
                            benchmarkViewModel.selectProfile(profile)
                            navController.navigate(Screen.Benchmark.route)
                        }
                    )
                }

                composable(Screen.History.route) {
                    HistoryScreen(viewModel = historyViewModel)
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(viewModel = settingsViewModel)
                }
            }
        }
    }

    private fun requestVpnPermission(provider: DnsProvider) {
        val vpnIntent = VpnService.prepare(this)
        if (vpnIntent != null) {
            pendingDnsProvider = provider
            vpnPermissionLauncher.launch(vpnIntent)
        } else {
            // VPN permission already granted
            val intent = Intent(this, DnsVpnService::class.java).apply {
                action = DnsVpnService.ACTION_START
                putExtra(DnsVpnService.EXTRA_DNS_SERVER, provider.ipv4Primary)
            }
            startForegroundService(intent)
        }
    }
}
