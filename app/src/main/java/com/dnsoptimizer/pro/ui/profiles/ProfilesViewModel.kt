package com.dnsoptimizer.pro.ui.profiles

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dnsoptimizer.pro.data.model.DnsProfile
import com.dnsoptimizer.pro.data.repository.DnsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfilesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DnsRepository(application)
    private val _profiles = MutableStateFlow<List<DnsProfile>>(emptyList())
    val profiles: StateFlow<List<DnsProfile>> = _profiles.asStateFlow()

    private val _selectedProfile = MutableStateFlow<DnsProfile?>(null)
    val selectedProfile: StateFlow<DnsProfile?> = _selectedProfile.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllProfiles().collect { _profiles.value = it }
        }
    }

    fun selectProfile(profile: DnsProfile) { _selectedProfile.value = profile }
}
