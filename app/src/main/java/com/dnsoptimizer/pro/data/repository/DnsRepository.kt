package com.dnsoptimizer.pro.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import com.dnsoptimizer.pro.data.local.AppDatabase
import com.dnsoptimizer.pro.data.local.DnsDatabase
import com.dnsoptimizer.pro.data.model.*
import com.dnsoptimizer.pro.data.remote.DnsApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.net.InetAddress

/**
 * Repository for managing DNS providers and benchmark results.
 * Handles local database operations and remote updates.
 */
class DnsRepository(private val context: Context) {
    
    companion object {
        private const val TAG = "DnsRepository"
        private const val REMOTE_DB_URL = "https://raw.githubusercontent.com/dns-optimizer-pro/database/main/dns_database.json"
        private const val CACHE_DURATION_MS = 24 * 60 * 60 * 1000L // 24 hours
    }
    
    private val database = AppDatabase.getDatabase(context)
    private val providerDao = database.dnsProviderDao()
    private val benchmarkDao = database.benchmarkResultDao()
    private val profileDao = database.dnsProfileDao()
    private val apiService = DnsApiService()
    
    init {
        // Initialize with bundled database on first run
        kotlinx.coroutines.runBlocking { initializeDatabase() }
    }
    
    /**
     * Initialize database with bundled providers if empty.
     */
    private suspend fun initializeDatabase() {
        try {
            val count = providerDao.getActiveProviderCount()
            if (count == 0) {
                // First run - insert bundled providers
                val bundledProviders = DnsDatabase.getBundledProviders()
                providerDao.insertProviders(bundledProviders)
                Log.i(TAG, "Initialized database with ${bundledProviders.size} providers")
                
                // Insert bundled profiles
                val bundledProfiles = DnsDatabase.getBundledProfiles()
                profileDao.insertProfiles(bundledProfiles)
                Log.i(TAG, "Initialized database with ${bundledProfiles.size} profiles")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize database", e)
        }
    }
    
    /**
     * Get all active DNS providers.
     */
    fun getAllProviders(): Flow<List<DnsProvider>> = providerDao.getAllActiveProviders()
    
    /**
     * Get all active DNS providers as a list.
     */
    suspend fun getAllProvidersList(): List<DnsProvider> = providerDao.getAllActiveProvidersList()
    
    /**
     * Get provider by ID.
     */
    suspend fun getProviderById(id: String): DnsProvider? = providerDao.getProviderById(id)
    
    /**
     * Get providers by category.
     */
    fun getProvidersByCategory(category: DnsCategory): Flow<List<DnsProvider>> = 
        providerDao.getProvidersByCategory(category)
    
    /**
     * Get Iranian-compatible providers.
     */
    fun getIranCompatibleProviders(): Flow<List<DnsProvider>> = 
        providerDao.getIranCompatibleProviders()
    
    /**
     * Get all gaming profiles.
     */
    fun getAllProfiles(): Flow<List<DnsProfile>> = profileDao.getAllActiveProfiles()
    
    /**
     * Get profile by ID.
     */
    suspend fun getProfileById(id: String): DnsProfile? = profileDao.getProfileById(id)
    
    /**
     * Update DNS database from remote source.
     * Falls back to bundled database if remote update fails.
     */
    suspend fun updateDatabase(): UpdateResult = withContext(Dispatchers.IO) {
        try {
            val remoteData = apiService.fetchDatabase(REMOTE_DB_URL)
            
            if (remoteData != null && remoteData.schemaVersion >= 1) {
                // Validate remote data
                if (validateRemoteData(remoteData)) {
                    // Clear and insert new providers
                    providerDao.deleteAllActiveProviders()
                    providerDao.insertProviders(remoteData.providers)
                    
                    // Update metadata
                    // In a real app, we'd store metadata separately
                    
                    Log.i(TAG, "Database updated from remote: ${remoteData.providers.size} providers")
                    UpdateResult.Success(remoteData.providers.size)
                } else {
                    Log.w(TAG, "Remote data validation failed")
                    UpdateResult.ValidationError
                }
            } else {
                Log.w(TAG, "Failed to fetch remote database")
                UpdateResult.NetworkError
            }
        } catch (e: Exception) {
            Log.e(TAG, "Database update failed", e)
            UpdateResult.NetworkError
        }
    }
    
    /**
     * Validate remote database data before accepting.
     */
    private fun validateRemoteData(data: DnsDatabaseSchema): Boolean {
        // Check schema version
        if (data.schemaVersion < 1) return false
        
        // Check provider count
        if (data.providers.isEmpty()) return false
        
        // Validate each provider
        for (provider in data.providers) {
            if (provider.id.isBlank() || provider.name.isBlank()) return false
            if (provider.ipv4Primary.isBlank()) return false
            
            // Validate IPv4 format
            try {
                InetAddress.getByName(provider.ipv4Primary)
            } catch (e: Exception) {
                Log.w(TAG, "Invalid IPv4 for provider ${provider.id}: ${provider.ipv4Primary}")
                return false
            }
        }
        
        return true
    }
    
    /**
     * Get current network information.
     */
    fun getNetworkInfo(): NetworkInfo {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = network?.let { connectivityManager.getNetworkCapabilities(it) }
        
        val isConnected = capabilities != null
        val networkType = when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> NetworkType.WIFI
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> NetworkType.MOBILE
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> NetworkType.ETHERNET
            else -> NetworkType.UNKNOWN
        }
        
        // Get network name
        val networkName = when (networkType) {
            NetworkType.WIFI -> {
                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val connectionInfo = wifiManager.connectionInfo
                connectionInfo?.ssid?.removeSurrounding("\"") ?: "Unknown WiFi"
            }
            NetworkType.MOBILE -> "Mobile Data"
            NetworkType.ETHERNET -> "Ethernet"
            else -> "Not Connected"
        }
        
        // Check IPv6 support
        val supportsIPv6 = try {
            val linkProperties = network?.let { connectivityManager.getLinkProperties(it) }
            linkProperties?.linkAddresses?.any { it.address is java.net.Inet6Address } ?: false
        } catch (e: Exception) {
            false
        }
        
        // Get current DNS servers
        val currentDns = try {
            val linkProperties = network?.let { connectivityManager.getLinkProperties(it) }
            linkProperties?.dnsServers?.map { it.hostAddress ?: "" } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        
        return NetworkInfo(
            networkType = networkType,
            networkName = networkName,
            isConnected = isConnected,
            supportsIPv4 = true,
            supportsIPv6 = supportsIPv6,
            currentDns = currentDns,
            currentDnsProtocol = "System Default",
            network = network
        )
    }
    
    /**
     * Save benchmark result.
     */
    suspend fun saveBenchmarkResult(result: BenchmarkResult): Long = 
        benchmarkDao.insertResult(result)
    
    /**
     * Update benchmark result.
     */
    suspend fun updateBenchmarkResult(result: BenchmarkResult) = 
        benchmarkDao.updateResult(result)
    
    /**
     * Get all benchmark results.
     */
    fun getAllBenchmarkResults(): Flow<List<BenchmarkResult>> = benchmarkDao.getAllResults()
    
    /**
     * Get benchmark results for a specific provider.
     */
    fun getBenchmarkResultsForProvider(providerId: String): Flow<List<BenchmarkResult>> = 
        benchmarkDao.getResultsByProvider(providerId)
    
    /**
     * Get recent benchmark results.
     */
    suspend fun getRecentResults(protocol: DnsProtocol, limit: Int = 50): List<BenchmarkResult> = 
        benchmarkDao.getRecentResults(protocol, limit)
    
    /**
     * Clean up old benchmark results (keep last 30 days).
     */
    suspend fun cleanupOldResults() {
        val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
        benchmarkDao.deleteOldResults(thirtyDaysAgo)
    }
}

/**
 * Result of a database update operation.
 */
sealed class UpdateResult {
    data class Success(val providerCount: Int) : UpdateResult()
    data object NetworkError : UpdateResult()
    data object ValidationError : UpdateResult()
    data object CacheHit : UpdateResult()
}
