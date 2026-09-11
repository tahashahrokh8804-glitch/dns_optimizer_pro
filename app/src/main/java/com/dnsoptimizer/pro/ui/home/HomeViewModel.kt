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
        loadLatestBenchmark()
        observeBenchmarkProgress()
    }
    
    /**
     * Load current network information.
     */
    private fun loadNetworkInfo() {
        viewModelScope.launch {
            val info = repository.getNetworkInfo()
            _networkInfo.value = info
            _uiState.value = _uiState.value.copy(isConnected = info.isConnected)
        }
    }
    
    /**
     * Load the latest benchmark result.
     */
    private fun loadLatestBenchmark() {
        viewModelScope.launch {
            repository.getAllBenchmarkResults().first().let { results ->
                if (results.isNotEmpty()) {
                    val latest = results.first()
                    val ranked = scoringEngine.rankResults(listOf(latest)).firstOrNull()
                    _recommendation.value = ranked
                    _uiState.value = _uiState.value.copy(
                        lastBenchmarkTime = latest.timestamp,
                        lastBenchmarkScore = latest.score
                    )
                }
            }
        }
    }
    
    /**
     * Observe benchmark engine progress.
     */
    private fun observeBenchmarkProgress() {
        viewModelScope.launch {
            benchmarkEngine.progress.collect { progress ->
                _benchmarkProgress.value = progress
                _uiState.value = _uiState.value.copy(
                    isBenchmarkRunning = progress is BenchmarkProgress.Testing || 
                                       progress is BenchmarkProgress.MultiTesting
                )
            }
        }
    }
    
    /**
     * Start a quick benchmark of the top 5 providers.
     */
    fun startQuickBenchmark() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBenchmarkRunning = true)
            
            try {
                val providers = repository.getAllProvidersList().take(5)
                val results = benchmarkEngine.benchmarkAllProviders(providers)
                
                // Save results
                results.forEach { result ->
                    repository.saveBenchmarkResult(result)
                }
                
                // Find best recommendation
                val ranked = scoringEngine.rankResults(results)
                if (ranked.isNotEmpty()) {
                    _recommendation.value = ranked.first()
                }
                
                loadLatestBenchmark()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message
                )
            } finally {
                _uiState.value = _uiState.value.copy(isBenchmarkRunning = false)
            }
        }
    }
    
    /**
     * Apply a DNS configuration.
     */
    fun applyDns(provider: DnsProvider) {
        viewModelScope.launch {
            try {
                // Start VPN service with selected DNS
                val intent = Intent(getApplication(), DnsVpnService::class.java).apply {
                    action = DnsVpnService.ACTION_START
                    putExtra(DnsVpnService.EXTRA_DNS_SERVER, provider.ipv4Primary)
                }
                getApplication<Application>().startForegroundService(intent)
                
                _vpnActive.value = true
                _currentDns.value = CurrentDnsConfig(
                    servers = listOf(provider.ipv4Primary, provider.ipv4Secondary),
                    source = DnsConfigSource.VPN
                )
                
                _uiState.value = _uiState.value.copy(
                    activeDnsProvider = provider,
                    isDnsActive = true
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to apply DNS: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Disconnect VPN and revert to system DNS.
     */
    fun disconnectDns() {
        val intent = Intent(getApplication(), DnsVpnService::class.java).apply {
            action = DnsVpnService.ACTION_STOP
        }
        getApplication<Application>().startForegroundService(intent)
        
        _vpnActive.value = false
        _currentDns.value = CurrentDnsConfig(source = DnsConfigSource.SYSTEM)
        _uiState.value = _uiState.value.copy(
            activeDnsProvider = null,
            isDnsActive = false
        )
    }
    
    /**
     * Refresh network info.
     */
    fun refreshNetworkInfo() {
        loadNetworkInfo()
    }
    
    /**
     * Clear error message.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    override fun onCleared() {
        super.onCleared()
        benchmarkEngine.cleanup()
    }
}

/**
 * Home screen UI state.
 */
data class HomeUiState(
    val isConnected: Boolean = false,
    val isBenchmarkRunning: Boolean = false,
    val isDnsActive: Boolean = false,
    val activeDnsProvider: DnsProvider? = null,
    val lastBenchmarkTime: Long = 0L,
    val lastBenchmarkScore: Double = 0.0,
    val errorMessage: String? = null
)
