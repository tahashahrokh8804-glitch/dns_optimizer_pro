# DNS Optimizer Pro 🎮

A production-quality Android app that helps Iranian gamers find and use the best DNS configuration for their network.

## How It Works

DNS Optimizer Pro benchmarks DNS providers using **real network tests** — not simulated data. It measures actual DNS query latency across multiple protocols (UDP, TCP, DoT, DoH) and recommends the best option based on YOUR specific network conditions.

### What DNS Actually Changes

- ✅ Domain resolution speed
- ✅ Matchmaking/login/launcher connection setup
- ✅ Store and website loading
- ❌ In-game latency after connection is established
- ❌ Game server ping (determined by routing, not DNS)

## Architecture

```
app/src/main/java/com/dnsoptimizer/pro/
├── App.kt                          # Application class
├── MainActivity.kt                 # Single activity with Compose navigation
├── data/
│   ├── model/
│   │   ├── DnsProvider.kt         # Provider data model (Room entity)
│   │   ├── BenchmarkResult.kt     # Benchmark results (Room entity)
│   │   ├── DnsProfile.kt         # Gaming profiles (Room entity)
│   │   └── NetworkInfo.kt        # Network state + schema
│   ├── local/
│   │   ├── AppDatabase.kt        # Room database + type converters
│   │   ├── Daos.kt               # Data access objects
│   │   └── DnsDatabase.kt        # Bundled fallback database (20 providers)
│   ├── remote/
│   │   └── DnsApiService.kt      # Remote database updates
│   └── repository/
│       └── DnsRepository.kt      # Data access layer
├── domain/
│   ├── benchmark/
│   │   ├── DnsBenchmarkEngine.kt # Real DNS benchmarking engine
│   │   └── ScoringEngine.kt      # Multi-factor scoring system
│   └── vpn/
│       └── DnsVpnService.kt      # VPN-based DNS routing
└── ui/
    ├── theme/                     # Dark gaming theme (Material 3)
    ├── navigation/                # Bottom navigation
    ├── home/                      # Dashboard screen
    ├── benchmark/                 # Full benchmark screen
    ├── dnslist/                   # DNS provider browser
    ├── profiles/                  # Gaming profiles
    ├── history/                   # Benchmark history
    ├── settings/                  # App settings
    └── components/                # Shared UI components
```

## Benchmark Methodology

For each DNS provider and protocol:

1. **Reachability test** — Can we connect?
2. **UDP DNS query** — Standard port 53 queries (10x minimum)
3. **TCP DNS query** — Reliable fallback
4. **DoT (DNS over TLS)** — Encrypted connection
5. **DoH (DNS over HTTPS)** — Web-friendly encryption

Metrics collected per test:
- Min/Max/Avg/Median/P95 latency (ms)
- Jitter (standard deviation)
- Success rate (%)
- Timeout rate
- DNSSEC support

**Scoring weights**: Latency 40%, Reliability 25%, Stability 20%, Timeout 10%, Protocol 5%

## DNS Provider Database

### Global Providers
| Provider | Primary | DoH | DoT | Privacy |
|----------|---------|-----|-----|---------|
| Cloudflare | 1.1.1.1 | ✅ | ✅ | High |
| Google DNS | 8.8.8.8 | ✅ | ✅ | Moderate |
| Quad9 | 9.9.9.9 | ✅ | ✅ | High |
| AdGuard | 94.140.14.14 | ✅ | ✅ | High |
| OpenDNS | 208.67.222.222 | ✅ | ❌ | Moderate |

### Iranian Providers
| Provider | Primary | Iran Compat | Notes |
|----------|---------|-------------|-------|
| Radar Game | 10.10.10.10 | Excellent | Gaming optimized |
| Shecan | 178.22.122.100 | Excellent | Anti-filter |
| 403 Online | 10.10.10.10 | Excellent | Anti-filter |
| Begzar | 185.222.222.222 | Excellent | Anti-filter |

## Protocol Comparison

| Protocol | Port | Encryption | Speed | Reliability |
|----------|------|------------|-------|-------------|
| UDP | 53 | None | Fastest | Good |
| TCP | 53 | None | Fast | Excellent |
| DoT | 853 | TLS | Medium | Good |
| DoH | 443 | HTTPS | Medium | Good |
| DoH3 | 443 | QUIC+HTTPS | Fast | Good |
| DoQ | 853 | QUIC | Fast | Good |

## Gaming Profiles

- **General Gaming** — Tests across all platforms
- **Steam** — Store, CDN, community
- **Epic Games** — Store, launcher
- **Riot Games** — LoL, Valorant
- **Call of Duty** — Battle.net integration
- **PUBG** — Mobile and PC
- **Minecraft** — Java and Bedrock
- **PlayStation** — PSN services
- **Xbox** — Live and Game Pass

## VPN/DNS Implementation

The app uses Android's `VpnService` to route DNS queries through the selected resolver:

1. Creates a local VPN interface
2. Intercepts DNS queries (port 53)
3. Forwards them to the chosen DNS provider
4. Returns responses to the calling app

**Important**: This ONLY routes DNS traffic. Game traffic (UDP on game ports) is NOT routed through the VPN.

## Android Limitations

- **Private DNS**: Android 9+ supports system-wide DoT via Private DNS settings, but only DoT
- **VPN required**: For UDP/TCP DNS or DoH, a VPN service is needed
- **No root required**: Uses standard Android APIs
- **Battery impact**: VPN-based DNS has minimal battery impact

## Building the APK

### Prerequisites
- JDK 17+
- Android SDK (API 34+)
- Gradle 8.13+

### Build Commands
```bash
# Debug build
./gradlew assembleDebug

# Release build (requires signing config)
./gradlew assembleRelease

# Clean build
./gradlew clean assembleDebug
```

### Known Issue: ARM/Termux Builds
Google's Maven CDN may be unreliable from certain networks. If build fails with dependency errors:
1. Try again after a few minutes (CDN issues are often temporary)
2. Use Android Studio on a desktop machine
3. Use a VPN to access Google Maven

## How to Add a New DNS Provider

1. Edit `DnsDatabase.kt`
2. Add a new `DnsProvider` entry to `getBundledProviders()`
3. Fill in all fields (see existing entries for format)
4. Rebuild the app

## How to Update the Remote DNS Database

1. Host a JSON file matching `DnsDatabaseSchema`
2. Set the URL in `DnsRepository.REMOTE_DB_URL`
3. Users can update via Settings > Update Database

## Running Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

## Privacy

- **No telemetry** by default
- **No data collection** — all benchmarks run locally
- **No account required**
- DNS queries go directly to the selected provider (not through our servers)
- Remote database updates only fetch provider configurations, not user data

## Privacy Policy

This app performs DNS benchmarking tests by sending DNS queries to third-party DNS resolvers. These queries are standard DNS lookups for well-known domains (google.com, cloudflare.com, etc.) and do not contain personal information. The app does not collect, store, or transmit any personal data. All benchmark results are stored locally on the device. The optional remote database update only downloads DNS provider configurations (IP addresses, hostnames) and does not transmit any user data.

## License

MIT License
