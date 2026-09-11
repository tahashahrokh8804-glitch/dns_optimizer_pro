package com.dnsoptimizer.pro.ui.dnslist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dnsoptimizer.pro.data.model.*
import com.dnsoptimizer.pro.data.repository.DnsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DnsListViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DnsRepository(application)

    private val _providers = MutableStateFlow<List<DnsProvider>>(emptyList())
    val providers: StateFlow<List<DnsProvider>> = _providers.asStateFlow()

    private val _selectedCategory = MutableStateFlow<DnsCategory?>(null)
    val selectedCategory: StateFlow<DnsCategory?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadProviders()
    }

    private fun loadProviders() {
        viewModelScope.launch {
            repository.getAllProviders().combine(_selectedCategory) { list, cat ->
                if (cat == null) list else list.filter { it.category == cat }
            }.combine(_searchQuery) { list, query ->
                if (query.isBlank()) list else list.filter {
                    it.name.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
                }
            }.collect { _providers.value = it }
        }
    }

    fun setCategory(category: DnsCategory?) { _selectedCategory.value = category }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun refreshProviders() { loadProviders() }
}
