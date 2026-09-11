package com.dnsoptimizer.pro.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dnsoptimizer.pro.data.model.BenchmarkResult
import com.dnsoptimizer.pro.data.repository.DnsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DnsRepository(application)
    private val _results = MutableStateFlow<List<BenchmarkResult>>(emptyList())
    val results: StateFlow<List<BenchmarkResult>> = _results.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllBenchmarkResults().collect { _results.value = it }
        }
    }

    fun deleteResult(result: BenchmarkResult) {
        viewModelScope.launch {
            repository.updateBenchmarkResult(result)
        }
    }
}
