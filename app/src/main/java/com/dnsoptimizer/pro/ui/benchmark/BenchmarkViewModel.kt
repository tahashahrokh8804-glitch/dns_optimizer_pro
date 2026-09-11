package com.dnsoptimizer.pro.ui.benchmark

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dnsoptimizer.pro.data.model.*
import com.dnsoptimizer.pro.data.repository.DnsRepository
import com.dnsoptimizer.pro.domain.benchmark.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BenchmarkViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DnsRepository(application)
    private val benchmarkEngine = DnsBenchmarkEngine()
    private val scoringEngine = ScoringEngine()

    private val _uiState = MutableStateFlow(BenchmarkUiState())
    val uiState: StateFlow<BenchmarkUiState> = _uiState.asStateFlow()

    private val _results = MutableStateFlow<List<RankedResult>>(emptyList())
    val results: StateFlow<List<RankedResult>> = _results.asStateFlow()

    private val _progress = MutableStateFlow<BenchmarkProgress>(BenchmarkProgress.Idle)
    val progress: StateFlow<BenchmarkProgress> = _progress.asStateFlow()

    private val _providers = MutableStateFlow<List<DnsProvider>>(emptyList())
    val providers: StateFlow<List<DnsProvider>> = _providers.asStateFlow()

    private val _selectedProfile = MutableStateFlow<DnsProfile?>(null)
    val selectedProfile: StateFlow<DnsProfile?> = _selectedProfile.asStateFlow()

    init {
        loadProviders()
        observeProgress()
    }

    private fun loadProviders() {
        viewModelScope.launch {
            repository.getAllProviders().collect { list ->
                _providers.value = list
            }
        }
    }

    private fun observeProgress() {
        viewModelScope.launch {
            benchmarkEngine.progress.collect { p ->
                _progress.value = p
                _uiState.value = _uiState.value.copy(isBenchmarkRunning = p is BenchmarkProgress.Testing || p is BenchmarkProgress.MultiTesting)
            }
        }
    }

    fun selectProfile(profile: DnsProfile?) { _selectedProfile.value = profile }

    fun startFullBenchmark() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRunning = true, errorMessage = null)
            try {
                val testDomains = _selectedProfile.value?.testDomains ?: DnsBenchmarkEngine.TEST_DOMAINS
                val provs = _providers.value
                val rawResults = benchmarkEngine.benchmarkAllProviders(provs)
                val ranked = scoringEngine.rankResults(rawResults)
                _results.value = ranked
                rawResults.forEach { repository.saveBenchmarkResult(it) }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(isRunning = false)
            }
        }
    }

    fun cancelBenchmark() { benchmarkEngine.cancelAll() }

    fun clearError() { _uiState.value = _uiState.value.copy(errorMessage = null) }
}

data class BenchmarkUiState(
    val isBenchmarkRunning: Boolean = false,
    val errorMessage: String? = null
)
