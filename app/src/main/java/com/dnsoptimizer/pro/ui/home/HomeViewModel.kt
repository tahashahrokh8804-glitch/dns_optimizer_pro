package com.dnsoptimizer.pro.ui.home

import android.app.Application
import android.content.Intent
import android.net.VpnService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dnsoptimizer.pro.data.model.*
import com.dnsoptimizer.pro.data.repository.DnsRepository
import com.dnsoptimizer.pro.domain.benchmark.BenchmarkProgress
import com.dnsoptimizer.pro.domain.benchmark.DnsBenchmarkEngine
import com.dnsoptimizer.pro.domain.benchmark.RankedResult
import com.dnsoptimizer.pro.domain.benchmark.ScoringEngine
import com.dnsoptimizer.pro.domain.vpn.DnsVpnService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DnsRepository(application)
    private val benchmarkEngine = DnsBenchmarkEngine()
    private val scoringEngine = ScoringEngine()

    // UI State
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Network info
    private val _networkInfo = MutableStateFlow(NetworkInfo())
    val networkInfo: StateFlow<NetworkInfo> = _networkInfo.asStateFlow()

    // Current DNS config
    private val _currentDns = MutableStateFlow(CurrentDnsConfig())
    val currentDns: StateFlow<CurrentDnsConfig> = _currentDns.asStateFlow()

    // Selected provider (for quick connect)
    private val _selectedProvider = MutableStateFlow<DnsProvider?>(null)
    val selectedProvider: StateFlow<DnsProvider?> = _selectedProvider.asStateFlow()

    // Top 3 providers for quick select
    private val _topProviders = MutableStateFlow<List<DnsProvider>>(emptyList())
    val topProviders: StateFlow<List<DnsProvider>> = _topProviders.asStateFlow()

    // All providers
    private val _allProviders = MutableStateFlow<List<DnsProvider>>(emptyList())
    val allProviders: StateFlow<List<DnsProvider>> = _allProviders.asStateFlow()

    // Benchmark progress
    private val _benchmarkProgress = MutableStateFlow<BenchmarkProgress>(BenchmarkProgress.Idle)
    val benchmarkProgress: StateFlow<BenchmarkProgress> = _benchmarkProgress.asStateFlow()

    // Recommended DNS
    private val _recommendation = MutableStateFlow<RankedResult?>(null)
    val recommendation: StateFlow<RankedResult?> = _recommendation.asStateFlow()

    // VPN state
    private val _vpnActive = MutableStateFlow(DnsVpnService.isRunning)
    val vpnActive: StateFlow<Boolean> = _vpnActive.asStateFlow()

    init {
        loadNetworkInfo()
        loadProviders()
        loadLatestBenchmark()
        observeVpnState()
    }

    private fun loadNetworkInfo() {
        viewModelScope.launch {
            val info = repository.getNetworkInfo()
            _networkInfo.value = info
        }
    }

    private fun loadProviders() {
        viewModelScope.launch {
            repository.getAllProviders().collect { list ->
                _allProviders.value = list
                // Set top 3 for quick select (mix of global + Iranian)
                _topProviders.value = list.take(3)
            }
        }
    }

    private fun loadLatestBenchmark() {
        viewModelScope.launch {
            repository.getAllBenchmarkResults().first().let { results ->
                if (results.isNotEmpty()) {
                    val ranked = scoringEngine.rankResults(results)
                    if (ranked.isNotEmpty()) {
                        _recommendation.value = ranked.first()
                    }
                }
            }
        }
    }

    private fun observeVpnState() {
        viewModelScope.launch {
            while (true) {
                _vpnActive.value = DnsVpnService.isRunning
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    fun selectProvider(provider: DnsProvider) {
        _selectedProvider.value = provider
    }

    fun applyDns(provider: DnsProvider) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isConnecting = true)
            try {
                val intent = Intent(getApplication(), DnsVpnService::class.java).apply {
                    action = DnsVpnService.ACTION_START
                    putExtra(DnsVpnService.EXTRA_DNS_SERVER, provider.ipv4Primary)
                }
                getApplication<Application>().startForegroundService(intent)

                // Wait for VPN to start
                kotlinx.coroutines.delay(2000)

                _vpnActive.value = DnsVpnService.isRunning
                _selectedProvider.value = provider
                _currentDns.value = CurrentDnsConfig(
                    servers = listOf(provider.ipv4Primary, provider.ipv4Secondary),
                    source = DnsConfigSource.VPN
                )
                _uiState.value = _uiState.value.copy(
                    isConnecting = false,
                    activeDnsProvider = provider,
                    isDnsActive = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isConnecting = false,
                    errorMessage = "Failed to connect: ${e.message}"
                )
            }
        }
    }

    fun disconnectDns() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isConnecting = true)
            try {
                val intent = Intent(getApplication(), DnsVpnService::class.java).apply {
                    action = DnsVpnService.ACTION_STOP
                }
                getApplication<Application>().startForegroundService(intent)

                kotlinx.coroutines.delay(1000)

                _vpnActive.value = DnsVpnService.isRunning
                _currentDns.value = CurrentDnsConfig(source = DnsConfigSource.SYSTEM)
                _uiState.value = _uiState.value.copy(
                    isConnecting = false,
                    activeDnsProvider = null,
                    isDnsActive = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isConnecting = false,
                    errorMessage = "Failed to disconnect: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        benchmarkEngine.cleanup()
    }
}

data class HomeUiState(
    val isConnecting: Boolean = false,
    val isDnsActive: Boolean = false,
    val activeDnsProvider: DnsProvider? = null,
    val errorMessage: String? = null
)
