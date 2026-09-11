package com.dnsoptimizer.pro.data.remote

import android.util.Log
import com.dnsoptimizer.pro.data.model.DnsDatabaseSchema
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Remote API service for fetching DNS database updates.
 */
class DnsApiService {
    
    companion object {
        private const val TAG = "DnsApiService"
        private const val CONNECT_TIMEOUT = 15000
        private const val READ_TIMEOUT = 30000
    }
    
    private val gson = Gson()
    
    /**
     * Fetch DNS database from remote URL.
     * Returns null if fetch fails.
     */
    suspend fun fetchDatabase(url: String): DnsDatabaseSchema? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        
        try {
            connection = URL(url).openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT
                readTimeout = READ_TIMEOUT
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "DNSOptimizerPro/1.0")
            }
            
            connection.connect()
            
            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                val database = gson.fromJson(response, DnsDatabaseSchema::class.java)
                Log.i(TAG, "Successfully fetched database: ${database.providers.size} providers")
                database
            } else {
                Log.w(TAG, "Failed to fetch database: HTTP ${connection.responseCode}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching database", e)
            null
        } finally {
            connection?.disconnect()
        }
    }
    
    /**
     * Check if a remote database update is available.
     */
    suspend fun checkForUpdate(currentVersion: String, checkUrl: String): Boolean = 
        withContext(Dispatchers.IO) {
            try {
                val connection = URL(checkUrl).openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "GET"
                    connectTimeout = CONNECT_TIMEOUT
                    readTimeout = READ_TIMEOUT
                }
                
                connection.connect()
                
                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val latestVersion = gson.fromJson(response, Map::class.java)
                    val remoteVersion = latestVersion["database_version"] as? String
                    
                    remoteVersion != null && remoteVersion != currentVersion
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to check for update", e)
                false
            }
        }
}
