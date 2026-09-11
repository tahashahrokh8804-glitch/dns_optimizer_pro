package com.dnsoptimizer.pro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a gaming profile with associated test domains and recommendations.
 */
@Entity(tableName = "dns_profiles")
data class DnsProfile(
    @PrimaryKey
    val id: String,
    
    val name: String,
    
    val description: String,
    
    val icon: String = "🎮",
    
    val testDomains: List<String>,
    
    val serviceEndpoints: List<String> = emptyList(),
    
    val isCustom: Boolean = false,
    
    val isActive: Boolean = true,
    
    val priority: Int = 0
)

/**
 * Predefined gaming profiles for common use cases.
 */
object GamingProfiles {
    val GENERAL_GAMING = DnsProfile(
        id = "general_gaming",
        name = "General Gaming",
        description = "Optimized for general gaming across all platforms",
        icon = "🎮",
        testDomains = listOf(
            "google.com",
            "cloudflare.com",
            "amazon.com",
            "microsoft.com",
            "apple.com",
            "steamcdn-a.akamaihd.net",
            "epicgames.com"
        ),
        serviceEndpoints = listOf(
            "https://www.google.com",
            "https://store.steampowered.com",
            "https://www.epicgames.com"
        ),
        priority = 1
    )
    
    val STEAM = DnsProfile(
        id = "steam",
        name = "Steam",
        description = "Optimized for Steam client and store",
        icon = "🚂",
        testDomains = listOf(
            "store.steampowered.com",
            "steamcdn-a.akamaihd.net",
            "steamcommunity.com",
            "api.steampowered.com",
            "login.steampowered.com",
            "help.steampowered.com"
        ),
        serviceEndpoints = listOf(
            "https://store.steampowered.com",
            "https://steamcommunity.com"
        ),
        priority = 2
    )
    
    val EPIC_GAMES = DnsProfile(
        id = "epic_games",
        name = "Epic Games",
        description = "Optimized for Epic Games Store and launcher",
        icon = "🎯",
        testDomains = listOf(
            "epicgames.com",
            "unrealengine.com",
            "launcher-public-service-prod06.ol.epicgames.com",
            "download.epicgames.com",
            "cdn1.unrealengine.com"
        ),
        serviceEndpoints = listOf(
            "https://www.epicgames.com",
            "https://www.unrealengine.com"
        ),
        priority = 3
    )
    
    val RIOT_GAMES = DnsProfile(
        id = "riot_games",
        name = "Riot Games",
        description = "Optimized for League of Legends, Valorant, and other Riot games",
        icon = "⚔️",
        testDomains = listOf(
            "riotgames.com",
            "leagueoflegends.com",
            "playvalorant.com",
            "contactenvironment.riotgames.com",
            "loldna.leagueoflegends.com"
        ),
        serviceEndpoints = listOf(
            "https://www.riotgames.com",
            "https://playvalorant.com"
        ),
        priority = 4
    )
    
    val CALL_OF_DUTY = DnsProfile(
        id = "call_of_duty",
        name = "Call of Duty",
        description = "Optimized for Call of Duty titles",
        icon = "🎖️",
        testDomains = listOf(
            "callofduty.com",
            "activision.com",
            "battle.net",
            "blizzard.com",
            "secure.api.callofduty.com"
        ),
        serviceEndpoints = listOf(
            "https://www.callofduty.com",
            "https://battle.net"
        ),
        priority = 5
    )
    
    val PUBG = DnsProfile(
        id = "pubg",
        name = "PUBG",
        description = "Optimized for PUBG Mobile and PC",
        icon = "🪖",
        testDomains = listOf(
            "pubg.com",
            "krafton.com",
            "accounts.pubg.com",
            "api.pubg.com",
            "steamcdn-a.akamaihd.net"
        ),
        serviceEndpoints = listOf(
            "https://www.pubg.com",
            "https://accounts.pubg.com"
        ),
        priority = 6
    )
    
    val MINECRAFT = DnsProfile(
        id = "minecraft",
        name = "Minecraft",
        description = "Optimized for Minecraft Java and Bedrock editions",
        icon = "⛏️",
        testDomains = listOf(
            "minecraft.net",
            "mojang.com",
            "minecraftforge.net",
            "fabricmc.net",
            "authserver.mojang.com"
        ),
        serviceEndpoints = listOf(
            "https://www.minecraft.net",
            "https://www.mojang.com"
        ),
        priority = 7
    )
    
    val PLAYSTATION = DnsProfile(
        id = "playstation",
        name = "PlayStation",
        description = "Optimized for PlayStation Network services",
        icon = "🎮",
        testDomains = listOf(
            "playstation.com",
            "sony.com",
            "store.playstation.com",
            "id.sonyentertainmentnetwork.com",
            "dms.playstation.com"
        ),
        serviceEndpoints = listOf(
            "https://www.playstation.com",
            "https://store.playstation.com"
        ),
        priority = 8
    )
    
    val XBOX = DnsProfile(
        id = "xbox",
        name = "Xbox",
        description = "Optimized for Xbox Live and Game Pass services",
        icon = "🟢",
        testDomains = listOf(
            "xbox.com",
            "microsoft.com",
            "xboxlive.com",
            "live.com",
            "gamepass.com"
        ),
        serviceEndpoints = listOf(
            "https://www.xbox.com",
            "https://www.xbox.com/en-US/xbox-game-pass"
        ),
        priority = 9
    )
    
    fun getAll(): List<DnsProfile> = listOf(
        GENERAL_GAMING,
        STEAM,
        EPIC_GAMES,
        RIOT_GAMES,
        CALL_OF_DUTY,
        PUBG,
        MINECRAFT,
        PLAYSTATION,
        XBOX
    )
}
