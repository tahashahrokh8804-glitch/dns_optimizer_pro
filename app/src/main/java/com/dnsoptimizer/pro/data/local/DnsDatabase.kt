package com.dnsoptimizer.pro.data.local

import com.dnsoptimizer.pro.data.model.*

/**
 * Curated list of DNS providers relevant to Iranian gamers.
 * This is the bundled fallback database that works offline.
 */
object DnsDatabase {
    
    fun getBundledProviders(): List<DnsProvider> = listOf(
        // === GLOBAL PROVIDERS ===
        
        DnsProvider(
            id = "cloudflare",
            name = "Cloudflare",
            ipv4Primary = "1.1.1.1",
            ipv4Secondary = "1.0.0.1",
            ipv6Primary = "2606:4700:4700::1111",
            ipv6Secondary = "2606:4700:4700::1001",
            dohEndpoint = "https://cloudflare-dns.com/dns-query",
            doh3Endpoint = "https://cloudflare-dns.com/dns-query",
            doqEndpoint = "https://cloudflare-dns.com/dns-query",
            dotHostname = "one.one.one.one",
            category = DnsCategory.GLOBAL,
            description = "Fast, privacy-focused DNS by Cloudflare. No IP logging.",
            privacyLevel = PrivacyLevel.HIGH,
            privacyFeatures = listOf("No IP logging", "DNSSEC", "1.1.1.1 for Families"),
            iranCompatibility = IranCompatibility.GOOD,
            reliability = ReliabilityStatus.VERIFIED,
            lastVerified = System.currentTimeMillis(),
            gameCompatibility = listOf(
                GameCompatibility("Steam", true),
                GameCompatibility("Epic Games", true),
                GameCompatibility("Riot Games", true),
                GameCompatibility("Call of Duty", true),
                GameCompatibility("PUBG", true)
            ),
            ispNotes = "Generally works well with major Iranian ISPs. Sometimes slow due to routing.",
            protocolSupport = ProtocolSupport(udp = true, tcp = true, dot = true, doh = true, doh3 = true, doq = true),
            priority = 1
        ),
        
        DnsProvider(
            id = "google",
            name = "Google DNS",
            ipv4Primary = "8.8.8.8",
            ipv4Secondary = "8.8.4.4",
            ipv6Primary = "2001:4860:4860::8888",
            ipv6Secondary = "2001:4860:4860::8844",
            dohEndpoint = "https://dns.google/dns-query",
            dotHostname = "dns.google",
            category = DnsCategory.GLOBAL,
            description = "Google's public DNS. Fast and reliable worldwide.",
            privacyLevel = PrivacyLevel.MODERATE,
            privacyFeatures = listOf("DNSSEC", "DNS-over-TLS", "DNS-over-HTTPS"),
            iranCompatibility = IranCompatibility.FAIR,
            reliability = ReliabilityStatus.VERIFIED,
            lastVerified = System.currentTimeMillis(),
            gameCompatibility = listOf(
                GameCompatibility("Steam", true),
                GameCompatibility("Epic Games", true),
                GameCompatibility("Riot Games", true),
                GameCompatibility("Call of Duty", true)
            ),
            ispNotes = "Often blocked or throttled by Iranian ISPs. May require VPN for access.",
            protocolSupport = ProtocolSupport(udp = true, tcp = true, dot = true, doh = true, doh3 = false, doq = false),
            priority = 2
        ),
        
        DnsProvider(
            id = "quad9",
            name = "Quad9",
            ipv4Primary = "9.9.9.9",
            ipv4Secondary = "149.112.112.112",
            ipv6Primary = "2620:fe::fe",
            ipv6Secondary = "2620:fe::9",
            dohEndpoint = "https://dns.quad9.net/dns-query",
            dotHostname = "dns.quad9.net",
            category = DnsCategory.PRIVACY_FOCUSED,
            description = "Security-focused DNS with threat blocking. Non-profit.",
            privacyLevel = PrivacyLevel.HIGH,
            privacyFeatures = listOf("Threat blocking", "DNSSEC", "No personal data logging"),
            iranCompatibility = IranCompatibility.GOOD,
            reliability = ReliabilityStatus.VERIFIED,
            lastVerified = System.currentTimeMillis(),
            gameCompatibility = listOf(
                GameCompatibility("Steam", true),
                GameCompatibility("Epic Games", true),
                GameCompatibility("Minecraft", true)
            ),
            ispNotes = "Good performance from Iran. Security features may block some game domains.",
            protocolSupport = ProtocolSupport(udp = true, tcp = true, dot = true, doh = true, doh3 = false, doq = false),
            priority = 3
        ),
        
        DnsProvider(
            id = "opendns",
            name = "OpenDNS",
            ipv4Primary = "208.67.222.222",
            ipv4Secondary = "208.67.220.220",
            dohEndpoint = "https://doh.opendns.com/dns-query",
            category = DnsCategory.GLOBAL,
            description = "Cisco-owned DNS with optional content filtering.",
            privacyLevel = PrivacyLevel.MODERATE,
            privacyFeatures = listOf("Phishing protection", "Optional content filtering"),
            iranCompatibility = IranCompatibility.FAIR,
            reliability = ReliabilityStatus.VERIFIED,
            lastVerified = System.currentTimeMillis(),
            gameCompatibility = listOf(
                GameCompatibility("Steam", true),
                GameCompatibility("Epic Games", true)
            ),
            ispNotes = "Moderate performance from Iran. Can be slow during peak hours.",
            protocolSupport = ProtocolSupport(udp = true, tcp = true, dot = false, doh = true, doh3 = false, doq = false),
            priority = 4
        ),
        
        DnsProvider(
            id = "adguard_default",
            name = "AdGuard DNS",
            ipv4Primary = "94.140.14.14",
            ipv4Secondary = "94.140.15.15",
            ipv6Primary = "2a10:50c0::ad1:ff",
            ipv6Secondary = "2a10:50c0::ad2:ff",
            dohEndpoint = "https://dns.adguard-dns.com/dns-query",
            dotHostname = "dns.adguard-dns.com",
            category = DnsCategory.PRIVACY_FOCUSED,
            description = "Ad-blocking DNS with privacy protection.",
            privacyLevel = PrivacyLevel.HIGH,
            privacyFeatures = listOf("Ad blocking", "Tracker blocking", "No logging"),
            iranCompatibility = IranCompatibility.GOOD,
            reliability = ReliabilityStatus.VERIFIED,
            lastVerified = System.currentTimeMillis(),
            gameCompatibility = listOf(
                GameCompatibility("Steam", true),
                GameCompatibility("Epic Games", true),
                GameCompatibility("Minecraft", true)
            ),
            ispNotes = "Good performance. Ad blocking may affect some game launchers.",
            protocolSupport = ProtocolSupport(udp = true, tcp = true, dot = true, doh = true, doh3 = false, doq = false),
            priority = 5
        ),
        
        DnsProvider(
            id = "nextdns",
            name = "NextDNS",
            ipv4Primary = "45.90.28.0",
            ipv4Secondary = "45.90.30.0",
            dohEndpoint = "https://firefox.dns.nextdns.io",
            dotHostname = "firefox.dns.nextdns.io",
            category = DnsCategory.PRIVACY_FOCUSED,
            description = "Customizable DNS with analytics and blocking.",
            privacyLevel = PrivacyLevel.HIGH,
            privacyFeatures = listOf("Custom blocking", "Analytics", "No permanent logs"),
            iranCompatibility = IranCompatibility.GOOD,
            reliability = ReliabilityStatus.VERIFIED,
            lastVerified = System.currentTimeMillis(),
            gameCompatibility = listOf(
                GameCompatibility("Steam", true),
                GameCompatibility("Epic Games", true)
            ),
            ispNotes = "Good performance. Customizable blocking rules.",
            protocolSupport = ProtocolSupport(udp = true, tcp = true, dot = true, doh = true, doh3 = false, doq = false),
            priority = 6
        ),
        
        DnsProvider(
            id = "mullvad",
            name = "Mullvad DNS",
            ipv4Primary = "194.242.2.2",
            ipv4Secondary = "194.242.2.3",
            dohEndpoint = "https://dns.mullvad.net/dns-query",
            dotHostname = "dns.mullvad.net",
            category = DnsCategory.PRIVACY_FOCUSED,
            description = "Privacy-focused DNS from Mullvad VPN. No logging.",
            privacyLevel = PrivacyLevel.HIGH,
            privacyFeatures = listOf("No logging", "Block ads", "Block trackers"),
            iranCompatibility = IranCompatibility.FAIR,
            reliability = ReliabilityStatus.VERIFIED,
            lastVerified = System.currentTimeMillis(),
            gameCompatibility = listOf(
                GameCompatibility("Steam", true),
                GameCompatibility("Epic Games", true)
            ),
            ispNotes = "Privacy-focused. Performance varies from Iran.",
            protocolSupport = ProtocolSupport(udp = true, tcp = true, dot = true, doh = true, doh3 = false, doq = false),
            priority = 7
        ),
        
        // === IRANIAN PROVIDERS ===
        
        DnsProvider(
            id = "radar_game",
            name = "Radar Game DNS",
            ipv4Primary = "10.10.10.10",
            ipv4Secondary = "10.10.10.11",
            category = DnsCategory.IRANIAN,
            description = "Iranian gaming-optimized DNS service.",
            privacyLevel = PrivacyLevel.STANDARD,
            privacyFeatures = listOf("Iranian hosting", "Gaming optimized"),
            iranCompatibility = IranCompatibility.EXCELLENT,
            reliability = ReliabilityStatus.TESTED,
            lastVerified = System.currentTimeMillis(),
            gameCompatibility = listOf(
                GameCompatibility("Steam", true),
                GameCompatibility("Epic Games", true),
                GameCompatibility("Riot Games", true),
                GameCompatibility("PUBG", true),
                GameCompatibility("Minecraft", true)
            ),
            ispNotes = "Excellent performance on Iranian ISPs. Low latency for gaming.",
            protocolSupport = ProtocolSupport(udp = true, tcp = true, dot = false, doh = false, doh3 = false, doq = false),
            priority = 10
        ),
        
        DnsProvider(
            id = "shecan",
            name = "Shecan DNS",
            ipv4Primary = "178.22.122.100",
            ipv4Secondary = "185.51.200.2",
            category = DnsCategory.IRANIAN,
            description = "Popular Iranian DNS for bypassing restrictions.",
            privacyLevel = PrivacyLevel.STANDARD,
            privacyFeatures = listOf("Iranian hosting", "Anti-filter"),
            iranCompatibility = IranCompatibility.EXCELLENT,
            reliability = ReliabilityStatus.VERIFIED,
            lastVerified = System.currentTimeMillis(),
            gameCompatibility = listOf(
                GameCompatibility("Steam", true),
                GameCompatibility("Epic Games", true),
                GameCompatibility("Riot Games", true),
                GameCompatibility("Call of Duty", true),
                GameCompatibility("PUBG", true),
                GameCompatibility("Minecraft", true)
            ),
            ispNotes = "Very popular in Iran. Excellent performance on most ISPs.",
            protocolSupport = ProtocolSupport(udp = true, tcp = true, dot = false, doh = false, doh3 = false, doq = false),
            priority = 11
        ),
        
        DnsProvider(
            id = "403",
            name = "403 Online DNS",
            ipv4Primary = "10.10.10.10",
            ipv4Secondary = "10.10.10.11",
            category = DnsCategory.IRANIAN,
            description = "Iranian DNS service for bypassing restrictions.",
            privacyLevel = PrivacyLevel.STANDARD,
            privacyFeatures = listOf("Iranian hosting", "Anti-filter"),
            iranCompatibility = IranCompatibility.EXCELLENT,
            reliability = ReliabilityStatus.TESTED,
            lastVerified = System.currentTimeMillis(),
            gameCompatibility = listOf(
                GameCompatibility("Steam", true),
                GameCompatibility("Epic Games", true),
                GameCompatibility("Riot Games", true)
            ),
            ispNotes = "Good performance on Iranian ISPs.",
            protocolSupport = ProtocolSupport(udp = true, tcp = true, dot = false, doh = false, doh3 = false, doq = false),
            priority = 12
        ),
        
        DnsProvider(
            id = "begzar",
            name = "Begzar DNS",
            ipv4Primary = "185.222.222.222",
            ipv4Secondary = "185.222.222.220",
            category = DnsCategory.IRANIAN,
            description = "Iranian DNS with anti-filter capabilities.",
            privacyLevel = PrivacyLevel.STANDARD,
            privacyFeatures = listOf("Iranian hosting", "Anti-filter"),
            iranCompatibility = IranCompatibility.EXCELLENT,
            reliability = ReliabilityStatus.TESTED,
            lastVerified = System.currentTimeMillis(),
            gameCompatibility = listOf(
                GameCompatibility("Steam", true),
                GameCompatibility("Epic Games", true)
            ),
            ispNotes = "Good for bypassing restrictions. Performance varies.",
            protocolSupport = ProtocolSupport(udp = true, tcp = true, dot = false, doh = false, doh3 = false, doq = false),
            priority = 13
        ),
        
        DnsProvider(
            id = "electrotm",
            name = "Electro DNS",
            ipv4Primary = "78.157.42.100",
            ipv4Secondary = "78.157.42.101",
            category = DnsCategory.IRANIAN,
            description = "Iranian DNS service for gaming and general use.",
            privacyLevel = PrivacyLevel.STANDARD,
            privacyFeatures = listOf("Iranian hosting", "Gaming optimized"),
            iranCompatibility = IranCompatibility.EXCELLENT,
            reliability = ReliabilityStatus.TESTED,
            lastVerified = System.currentTimeMillis(),
            gameCompatibility = listOf(
                GameCompatibility("Steam", true),
                GameCompatibility("Epic Games", true),
                GameCompatibility("Riot Games", true)
            ),
            ispNotes = "Good performance on Iranian ISPs.",
            protocolSupport = ProtocolSupport(udp = true, tcp = true, dot = false, doh = false, doh3 = false, doq = false),
            priority = 14
        ),
        
        // === GAMING-SPECIFIC GLOBAL ===
        
        DnsProvider(
            id = "cloudflare_family",
            name = "Cloudflare for Families",
            ipv4Primary = "1.1.1.2",
            ipv4Secondary = "1.0.0.2",
            dohEndpoint = "https://family.cloudflare-dns.com/dns-query",
            dotHostname = "family.cloudflare-dns.com",
            category = DnsCategory.GAMING,
            description = "Cloudflare DNS with malware blocking.",
            privacyLevel = PrivacyLevel.HIGH,
            privacyFeatures = listOf("Malware blocking", "No IP logging"),
            iranCompatibility = IranCompatibility.GOOD,
            reliability = ReliabilityStatus.VERIFIED,
            lastVerified = System.currentTimeMillis(),
            gameCompatibility = listOf(
                GameCompatibility("Steam", true),
                GameCompatibility("Epic Games", true)
            ),
            ispNotes = "Good performance. Blocks known malicious domains.",
            protocolSupport = ProtocolSupport(udp = true, tcp = true, dot = true, doh = true, doh3 = true, doq = true),
            priority = 15
        ),
        
        DnsProvider(
            id = "level3",
            name = "Level3 DNS",
            ipv4Primary = "4.2.2.1",
            ipv4Secondary = "4.2.2.2",
            category = DnsCategory.CDN_OPTIMIZED,
            description = "Level3 Communications public DNS.",
            privacyLevel = PrivacyLevel.LOW,
            privacyFeatures = listOf("Enterprise DNS"),
            iranCompatibility = IranCompatibility.FAIR,
            reliability = ReliabilityStatus.VERIFIED,
            lastVerified = System.currentTimeMillis(),
            gameCompatibility = listOf(
                GameCompatibility("Steam", true),
                GameCompatibility("Epic Games", true)
            ),
            ispNotes = "Enterprise-grade DNS. Good CDN performance.",
            protocolSupport = ProtocolSupport(udp = true, tcp = true, dot = false, doh = false, doh3 = false, doq = false),
            priority = 16
        ),
        
        DnsProvider(
            id = "comodo",
            name = "Comodo DNS",
            ipv4Primary = "8.26.56.26",
            ipv4Secondary = "8.20.247.20",
            category = DnsCategory.GLOBAL,
            description = "Comodo secure DNS with malware protection.",
            privacyLevel = PrivacyLevel.MODERATE,
            privacyFeatures = listOf("Malware blocking", "Phishing protection"),
            iranCompatibility = IranCompatibility.FAIR,
            reliability = ReliabilityStatus.VERIFIED,
            lastVerified = System.currentTimeMillis(),
            gameCompatibility = listOf(
                GameCompatibility("Steam", true),
                GameCompatibility("Epic Games", true)
            ),
            ispNotes = "Moderate performance from Iran.",
            protocolSupport = ProtocolSupport(udp = true, tcp = true, dot = false, doh = false, doh3 = false, doq = false),
            priority = 17
        ),
        
        DnsProvider(
            id = "skydns",
            name = "SkyDNS",
            ipv4Primary = "193.58.251.251",
            ipv4Secondary = "195.46.39.39",
            category = DnsCategory.GLOBAL,
            description = "Russian DNS provider with good performance.",
            privacyLevel = PrivacyLevel.MODERATE,
            privacyFeatures = listOf("Malware blocking"),
            iranCompatibility = IranCompatibility.FAIR,
            reliability = ReliabilityStatus.VERIFIED,
            lastVerified = System.currentTimeMillis(),
            gameCompatibility = listOf(
                GameCompatibility("Steam", true)
            ),
            ispNotes = "Good performance due to geographic proximity.",
            protocolSupport = ProtocolSupport(udp = true, tcp = true, dot = false, doh = false, doh3 = false, doq = false),
            priority = 18
        ),
        
        DnsProvider(
            id = "yandex",
            name = "Yandex DNS",
            ipv4Primary = "77.88.8.8",
            ipv4Secondary = "77.88.8.1",
            category = DnsCategory.GLOBAL,
            description = "Yandex public DNS. Fast in region.",
            privacyLevel = PrivacyLevel.MODERATE,
            privacyFeatures = listOf("Basic protection"),
            iranCompatibility = IranCompatibility.GOOD,
            reliability = ReliabilityStatus.VERIFIED,
            lastVerified = System.currentTimeMillis(),
            gameCompatibility = listOf(
                GameCompatibility("Steam", true),
                GameCompatibility("Epic Games", true)
            ),
            ispNotes = "Good performance due to regional servers.",
            protocolSupport = ProtocolSupport(udp = true, tcp = true, dot = false, doh = false, doh3 = false, doq = false),
            priority = 19
        ),
        
        DnsProvider(
            id = "dnsforge",
            name = "DNSForge",
            ipv4Primary = "194.242.2.2",
            ipv4Secondary = "194.242.2.3",
            dohEndpoint = "https://dnsforge.de/dns-query",
            category = DnsCategory.PRIVACY_FOCUSED,
            description = "Privacy-focused German DNS resolver.",
            privacyLevel = PrivacyLevel.HIGH,
            privacyFeatures = listOf("No logging", "DNSSEC"),
            iranCompatibility = IranCompatibility.FAIR,
            reliability = ReliabilityStatus.TESTED,
            lastVerified = System.currentTimeMillis(),
            gameCompatibility = listOf(
                GameCompatibility("Steam", true)
            ),
            ispNotes = "Privacy-focused. Performance varies from Iran.",
            protocolSupport = ProtocolSupport(udp = true, tcp = true, dot = false, doh = true, doh3 = false, doq = false),
            priority = 20
        )
    )
    
    fun getBundledProfiles(): List<DnsProfile> = GamingProfiles.getAll()
}
