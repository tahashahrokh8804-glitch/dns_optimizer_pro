package com.dnsoptimizer.pro

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        Log.i(TAG, "DNS Optimizer Pro initialized")
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vpnChannel = NotificationChannel(
                VPN_CHANNEL_ID,
                "DNS VPN Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active DNS VPN connection"
            }

            val benchmarkChannel = NotificationChannel(
                BENCHMARK_CHANNEL_ID,
                "Benchmark Notifications",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "DNS benchmark progress"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(vpnChannel)
            notificationManager.createNotificationChannel(benchmarkChannel)
        }
    }

    companion object {
        private const val TAG = "DnsOptimizerApp"
        const val VPN_CHANNEL_ID = "dns_vpn_channel"
        const val BENCHMARK_CHANNEL_ID = "benchmark_channel"
    }
}
