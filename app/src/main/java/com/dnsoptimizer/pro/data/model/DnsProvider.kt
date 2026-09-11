package com.dnsoptimizer.pro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Represents a DNS provider with all its configuration details.
 * This is the core data model for the DNS database.
 */
@Entity(tableName = "dns_providers")
data class DnsProvider(
    @PrimaryKey
    val id: String,
    
    val name: String,
    
    @SerializedName("ipv4_primary")
    val ipv4Primary: String,
    
    @SerializedName("ipv4_secondary")
    val ipv4Secondary: String,
    
    @SerializedName("ipv6_primary")
    val ipv6Primary: String? = null,
    
    @SerializedName("ipv6_secondary")
    val ipv6Secondary: String? = null,
    
    @SerializedName("doh_endpoint")
    val dohEndpoint: String? = null,
    
    @SerializedName("doh3_endpoint")
    val doh3Endpoint: String? = null,
    
    @SerializedName("doq_endpoint")
    val doqEndpoint: String? = null,
    
    @SerializedName("dot_hostname")
    val dotHostname: String? = null,
    
    val category: DnsCategory = DnsCategory.GLOBAL,
    
    val description: String = "",
    
    @SerializedName("privacy_level")
    val privacyLevel: PrivacyLevel = PrivacyLevel.STANDARD,
    
    @SerializedName("privacy_features")
    val privacyFeatures: List<String> = emptyList(),
    
    @SerializedName("iran_compatibility")
    val iranCompatibility: IranCompatibility = IranCompatibility.UNKNOWN,
    
    val reliability: ReliabilityStatus = ReliabilityStatus.TESTED,
    
    @SerializedName("last_verified")
    val lastVerified: Long = 0L,
    
    @SerializedName("game_compatibility")
    val gameCompatibility: List<GameCompatibility> = emptyList(),
    
    @SerializedName("isp_notes")
    val ispNotes: String = "",
    
    @SerializedName("protocol_support")
    val protocolSupport: ProtocolSupport = ProtocolSupport(),
    
    val isActive: Boolean = true,
    
    val priority: Int = 0
)

enum class DnsCategory {
    GLOBAL,
    IRANIAN,
    GAMING,
    PRIVACY_FOCUSED,
    CDN_OPTIMIZED,
    CUSTOM
}

enum class PrivacyLevel {
    HIGH,
    MODERATE,
    STANDARD,
    LOW
}

enum class IranCompatibility {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    UNKNOWN,
    BLOCKED
}

enum class ReliabilityStatus {
    VERIFIED,
    TESTED,
    UNVERIFIED,
    DEPRECATED,
    UNREACHABLE
}

data class GameCompatibility(
    val name: String,
    val isFullySupported: Boolean = true,
    val notes: String = ""
)

data class ProtocolSupport(
    val udp: Boolean = true,
    val tcp: Boolean = true,
    val dot: Boolean = false,
    val doh: Boolean = false,
    val doh3: Boolean = false,
    val doq: Boolean = false
)
