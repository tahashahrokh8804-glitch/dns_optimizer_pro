package com.dnsoptimizer.pro.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dnsoptimizer.pro.data.repository.DnsRepository
import com.dnsoptimizer.pro.data.repository.UpdateResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DnsRepository(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun updateDatabase() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true)
            when (val result = repository.updateDatabase()) {
                is UpdateResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        lastUpdateMessage = "Updated ${result.providerCount} providers"
                    )
                }
                is UpdateResult.NetworkError -> {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        lastUpdateMessage = "Network error - using bundled database"
                    )
                }
                is UpdateResult.ValidationError -> {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        lastUpdateMessage = "Invalid data received"
                    )
                }
                else -> {}
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(lastUpdateMessage = null)
    }
}

data class SettingsUiState(
    val isUpdating: Boolean = false,
    val lastUpdateMessage: String? = null
)
