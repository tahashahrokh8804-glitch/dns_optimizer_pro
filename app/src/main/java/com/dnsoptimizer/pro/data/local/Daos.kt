package com.dnsoptimizer.pro.data.local

import androidx.room.*
import com.dnsoptimizer.pro.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DnsProviderDao {
    @Query("SELECT * FROM dns_providers WHERE isActive = 1 ORDER BY priority ASC, name ASC")
    fun getAllActiveProviders(): Flow<List<DnsProvider>>
    
    @Query("SELECT * FROM dns_providers WHERE isActive = 1 ORDER BY priority ASC, name ASC")
    suspend fun getAllActiveProvidersList(): List<DnsProvider>
    
    @Query("SELECT * FROM dns_providers WHERE id = :providerId")
    suspend fun getProviderById(providerId: String): DnsProvider?
    
    @Query("SELECT * FROM dns_providers WHERE category = :category AND isActive = 1")
    fun getProvidersByCategory(category: DnsCategory): Flow<List<DnsProvider>>
    
    @Query("SELECT * FROM dns_providers WHERE iranCompatibility IN ('EXCELLENT', 'GOOD') AND isActive = 1")
    fun getIranCompatibleProviders(): Flow<List<DnsProvider>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvider(provider: DnsProvider)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProviders(providers: List<DnsProvider>)
    
    @Update
    suspend fun updateProvider(provider: DnsProvider)
    
    @Delete
    suspend fun deleteProvider(provider: DnsProvider)
    
    @Query("DELETE FROM dns_providers WHERE isActive = 1")
    suspend fun deleteAllActiveProviders()
    
    @Query("SELECT COUNT(*) FROM dns_providers WHERE isActive = 1")
    suspend fun getActiveProviderCount(): Int
}

@Dao
interface BenchmarkResultDao {
    @Query("SELECT * FROM benchmark_results ORDER BY timestamp DESC")
    fun getAllResults(): Flow<List<BenchmarkResult>>
    
    @Query("SELECT * FROM benchmark_results WHERE providerId = :providerId ORDER BY timestamp DESC")
    fun getResultsByProvider(providerId: String): Flow<List<BenchmarkResult>>
    
    @Query("SELECT * FROM benchmark_results WHERE providerId = :providerId AND protocol = :protocol ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestResult(providerId: String, protocol: DnsProtocol): BenchmarkResult?
    
    @Query("SELECT * FROM benchmark_results WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getResultsAfter(startTime: Long): Flow<List<BenchmarkResult>>
    
    @Query("SELECT * FROM benchmark_results WHERE networkType = :networkType ORDER BY timestamp DESC")
    fun getResultsByNetwork(networkType: NetworkType): Flow<List<BenchmarkResult>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: BenchmarkResult): Long
    
    @Update
    suspend fun updateResult(result: BenchmarkResult)
    
    @Delete
    suspend fun deleteResult(result: BenchmarkResult)
    
    @Query("DELETE FROM benchmark_results WHERE timestamp < :beforeTime")
    suspend fun deleteOldResults(beforeTime: Long)
    
    @Query("""
        SELECT * FROM benchmark_results 
        WHERE protocol = :protocol 
        ORDER BY timestamp DESC 
        LIMIT :limit
    """)
    suspend fun getRecentResults(protocol: DnsProtocol, limit: Int): List<BenchmarkResult>
}

@Dao
interface DnsProfileDao {
    @Query("SELECT * FROM dns_profiles WHERE isActive = 1 ORDER BY priority ASC")
    fun getAllActiveProfiles(): Flow<List<DnsProfile>>
    
    @Query("SELECT * FROM dns_profiles WHERE id = :profileId")
    suspend fun getProfileById(profileId: String): DnsProfile?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: DnsProfile)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(profiles: List<DnsProfile>)
    
    @Update
    suspend fun updateProfile(profile: DnsProfile)
    
    @Delete
    suspend fun deleteProfile(profile: DnsProfile)
}
