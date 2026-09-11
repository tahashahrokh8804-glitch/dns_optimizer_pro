package com.dnsoptimizer.pro.data.model

import android.net.Network

/**
 * Represents current network information.
 */
data class NetworkInfo(
    val networkType: NetworkType = NetworkType.UNKNOWN,
    val networkName: String = "",
    val isConnected: Boolean = false,
    val isVpnActive: Boolean = false,
    val supportsIPv4: Boolean = true,
    val supportsIPv6: Boolean = false,
    val currentDns: List<String> = emptyList(),
    val currentDnsProtocol: String = "Unknown",
    val network: Network? = null,
    val linkSpeed: Int = 0, // Mbps
    val signalStrength: Int = 0 // dBm
)

/**
 * Represents the current DNS configuration of the device.
 */
data class CurrentDnsConfig(
    val servers: List<String> = emptyList(),
    val domains: List<String> = emptyList(),
    val isPrivateDnsActive: Boolean = false,
    val privateDnsHostname: String? = null,
    val source: DnsConfigSource = DnsConfigSource.SYSTEM
)

enum class DnsConfigSource {
    SYSTEM,
    PRIVATE_DNS,
    VPN,
    CUSTOM,
    UNKNOWN
}

/**
 * DNS database schema for remote updates.
 */
data class DnsDatabaseSchema(
    val schemaVersion: Int = 1,
    val databaseVersion: String = "1.0.0",
    val updatedAt: Long = System.currentTimeMillis(),
    val providers: List<DnsProvider> = emptyList(),
    val protocols: ProtocolConfig = ProtocolConfig(),
    val metadata: DatabaseMetadata = DatabaseMetadata()
)

data class ProtocolConfig(
    val supportedProtocols: List<String> = listOf("udp", "tcp", "doh", "dot"),
    val defaultTestCount: Int = 10,
    val defaultTimeoutMs: Long = 5000,
    val maxConcurrentTests: Int = 3
)

data class DatabaseMetadata(
    val author: String = "DNS Optimizer Pro",
    val description: String = "DNS provider database for Iranian gamers",
    val license: String = "MIT",
    val minAppVersion: String = "1.0.0",
    val maxAppVersion: String = "99.99.99"
)
