package com.dnsoptimizer.pro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dnsoptimizer.pro.data.model.*

@Database(
    entities = [
        DnsProvider::class,
        BenchmarkResult::class,
        DnsProfile::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dnsProviderDao(): DnsProviderDao
    abstract fun benchmarkResultDao(): BenchmarkResultDao
    abstract fun dnsProfileDao(): DnsProfileDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dns_optimizer_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String = value.joinToString(",")
    
    @TypeConverter
    fun toStringList(value: String): List<String> = 
        if (value.isEmpty()) emptyList() else value.split(",")
    
    @TypeConverter
    fun fromDnsCategory(value: DnsCategory): String = value.name
    
    @TypeConverter
    fun toDnsCategory(value: String): DnsCategory = DnsCategory.valueOf(value)
    
    @TypeConverter
    fun fromPrivacyLevel(value: PrivacyLevel): String = value.name
    
    @TypeConverter
    fun toPrivacyLevel(value: String): PrivacyLevel = PrivacyLevel.valueOf(value)
    
    @TypeConverter
    fun fromIranCompatibility(value: IranCompatibility): String = value.name
    
    @TypeConverter
    fun toIranCompatibility(value: String): IranCompatibility = IranCompatibility.valueOf(value)
    
    @TypeConverter
    fun fromReliabilityStatus(value: ReliabilityStatus): String = value.name
    
    @TypeConverter
    fun toReliabilityStatus(value: String): ReliabilityStatus = ReliabilityStatus.valueOf(value)
    
    @TypeConverter
    fun fromDnsProtocol(value: DnsProtocol): String = value.name
    
    @TypeConverter
    fun toDnsProtocol(value: String): DnsProtocol = DnsProtocol.valueOf(value)
    
    @TypeConverter
    fun fromNetworkType(value: NetworkType): String = value.name
    
    @TypeConverter
    fun toNetworkType(value: String): NetworkType = NetworkType.valueOf(value)
    
    @TypeConverter
    fun fromBenchmarkStatus(value: BenchmarkStatus): String = value.name
    
    @TypeConverter
    fun toBenchmarkStatus(value: String): BenchmarkStatus = BenchmarkStatus.valueOf(value)
    
    @TypeConverter
    fun fromGameCompatibilityList(value: List<GameCompatibility>): String {
        return value.joinToString(";") { "${it.name}|${it.isFullySupported}|${it.notes}" }
    }
    
    @TypeConverter
    fun toGameCompatibilityList(value: String): List<GameCompatibility> {
        if (value.isEmpty()) return emptyList()
        return value.split(";").map { item ->
            val parts = item.split("|")
            GameCompatibility(
                name = parts[0],
                isFullySupported = parts[1].toBoolean(),
                notes = parts.getOrElse(2) { "" }
            )
        }
    }
    
    @TypeConverter
    fun fromProtocolSupport(value: ProtocolSupport): String {
        return "${value.udp},${value.tcp},${value.dot},${value.doh},${value.doh3},${value.doq}"
    }
    
    @TypeConverter
    fun toProtocolSupport(value: String): ProtocolSupport {
        val parts = value.split(",")
        return ProtocolSupport(
            udp = parts[0].toBoolean(),
            tcp = parts[1].toBoolean(),
            dot = parts[2].toBoolean(),
            doh = parts[3].toBoolean(),
            doh3 = parts[4].toBoolean(),
            doq = parts[5].toBoolean()
        )
    }
}
