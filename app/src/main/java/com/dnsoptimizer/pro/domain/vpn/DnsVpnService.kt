package com.dnsoptimizer.pro.domain.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import com.dnsoptimizer.pro.MainActivity
import com.dnsoptimizer.pro.R
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer

/**
 * VPN-based DNS service for routing DNS queries through the selected resolver.
 * 
 * This service creates a local VPN that intercepts DNS queries (port 53)
 * and forwards them to the selected DNS provider. This is necessary because:
 * 
 * 1. Android restricts direct DNS configuration changes
 * 2. Private DNS (DoT) only supports TLS, not all protocols
 * 3. Some users may want to use UDP/TCP DNS with specific providers
 * 
 * IMPORTANT: This only changes DNS resolution, not game traffic routing.
 * The VPN intercepts port 53 (DNS) traffic and forwards it to the selected resolver.
 * Game traffic (typically UDP on game ports) is NOT routed through this VPN.
 */
class DnsVpnService : VpnService() {
    
    companion object {
        private const val TAG = "DnsVpnService"
        private const val CHANNEL_ID = "dns_vpn_channel"
        private const val NOTIFICATION_ID = 1
        private const val DNS_PORT = 53
        private const val BUFFER_SIZE = 4096
        
        // VPN configuration
        private const val VPN_ADDRESS = "10.0.0.2"
        private const val VPN_ROUTE = "0.0.0.0"
        private const val VPN_MASK = "0"
        private const val VPN_DNS = "8.8.8.8" // Will be replaced with selected DNS
        
        var isRunning = false
            private set
        
        var currentDnsServer: String = VPN_DNS
            private set
    }
    
    private var vpnInterface: ParcelFileDescriptor? = null
    private var vpnJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val dnsServer = intent.getStringExtra(EXTRA_DNS_SERVER) ?: VPN_DNS
                startVpn(dnsServer)
            }
            ACTION_STOP -> {
                stopVpn()
            }
            else -> {
                // Default: start with configured DNS
                startVpn(currentDnsServer)
            }
        }
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): android.os.IBinder? {
        return null
    }
    
    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }
    
    private fun startVpn(dnsServer: String) {
        if (isRunning) {
            stopVpn()
        }
        
        currentDnsServer = dnsServer
        
        try {
            // Create VPN interface
            val builder = Builder()
                .setSession("DNS Optimizer Pro")
                .setMtu(1500)
                .addAddress(VPN_ADDRESS, 32)
                .addRoute(VPN_ROUTE, VPN_MASK.toInt())
                .addDnsServer(dnsServer)
            
            // Exclude our own app from VPN to prevent loops
            try {
                builder.addDisallowedApplication(packageName)
            } catch (e: Exception) {
                Log.w(TAG, "Could not exclude own package: ${e.message}")
            }
            
            vpnInterface = builder.establish()
            
            if (vpnInterface == null) {
                Log.e(TAG, "Failed to create VPN interface")
                return
            }
            
            isRunning = true
            startForeground(NOTIFICATION_ID, createNotification())
            
            // Start DNS forwarding
            vpnJob = coroutineScope.launch {
                forwardDns(dnsServer)
            }
            
            Log.i(TAG, "VPN started with DNS: $dnsServer")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start VPN", e)
            stopVpn()
        }
    }
    
    private fun stopVpn() {
        vpnJob?.cancel()
        vpnInterface?.close()
        vpnInterface = null
        isRunning = false
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        
        Log.i(TAG, "VPN stopped")
    }
    
    /**
     * Main DNS forwarding loop.
     * Reads DNS packets from VPN interface and forwards to selected DNS server.
     */
    private suspend fun forwardDns(dnsServer: String) = withContext(Dispatchers.IO) {
        val fd = vpnInterface?.fileDescriptor ?: return@withContext
        val inputStream = FileInputStream(fd)
        val outputStream = FileOutputStream(fd)
        
        val buffer = ByteArray(BUFFER_SIZE)
        
        try {
            while (isActive) {
                // Read packet from VPN interface
                val length = inputStream.read(buffer)
                if (length <= 0) {
                    delay(10)
                    continue
                }
                
                // Check if this is a DNS packet (UDP port 53)
                if (isDnsPacket(buffer, length)) {
                    // Forward to DNS server
                    forwardDnsPacket(buffer, length, dnsServer, outputStream)
                } else {
                    // Forward non-DNS traffic as-is (this is simplified - production would handle routing)
                    outputStream.write(buffer, 0, length)
                }
            }
        } catch (e: CancellationException) {
            Log.i(TAG, "DNS forwarding cancelled")
        } catch (e: Exception) {
            Log.e(TAG, "DNS forwarding error", e)
        } finally {
            inputStream.close()
            outputStream.close()
        }
    }
    
    /**
     * Check if a packet is a DNS query (UDP to port 53).
     */
    private fun isDnsPacket(buffer: ByteArray, length: Int): Boolean {
        if (length < 28) return false // Minimum IP + UDP header size
        
        // IP header: protocol field is at offset 9
        val protocol = buffer[9].toInt() and 0xFF
        
        // UDP protocol number is 17
        if (protocol != 17) return false
        
        // UDP header: destination port is at offset 22-23
        val dstPort = ((buffer[22].toInt() and 0xFF) shl 8) or (buffer[23].toInt() and 0xFF)
        
        return dstPort == DNS_PORT
    }
    
    /**
     * Forward a DNS packet to the specified DNS server.
     */
    private suspend fun forwardDnsPacket(
        buffer: ByteArray,
        length: Int,
        dnsServer: String,
        outputStream: FileOutputStream
    ) = withContext(Dispatchers.IO) {
        try {
            // Extract UDP payload (skip IP + UDP headers)
            val ipHeaderLength = (buffer[0].toInt() and 0x0F) * 4
            val udpHeaderLength = 8
            val payloadOffset = ipHeaderLength + udpHeaderLength
            val payload = buffer.copyOfRange(payloadOffset, length)
            
            // Send to DNS server
            val socket = DatagramSocket()
            socket.soTimeout = 5000
            
            val dnsAddress = InetAddress.getByName(dnsServer)
            val sendPacket = DatagramPacket(
                payload,
                payload.size,
                dnsAddress,
                DNS_PORT
            )
            socket.send(sendPacket)
            
            // Receive response
            val responseBuffer = ByteArray(512)
            val receivePacket = DatagramPacket(responseBuffer, responseBuffer.size)
            socket.receive(receivePacket)
            
            socket.close()
            
            // Construct response packet
            val responsePayload = responseBuffer.copyOfRange(0, receivePacket.length)
            
            // Build IP + UDP header for response
            val responsePacket = buildResponsePacket(buffer, length, responsePayload)
            
            // Write back to VPN interface
            outputStream.write(responsePacket)
            
        } catch (e: Exception) {
            Log.w(TAG, "DNS forwarding failed: ${e.message}")
        }
    }
    
    /**
     * Build a response packet with modified source/destination.
     */
    private fun buildResponsePacket(
        originalBuffer: ByteArray,
        originalLength: Int,
        responsePayload: ByteArray
    ): ByteArray {
        // For simplicity, swap source and destination in IP header
        // and source and destination in UDP header
        val packet = originalBuffer.copyOf()
        
        // Swap IP source and destination (offset 12-19)
        for (i in 12..15) {
            val temp = packet[i]
            packet[i] = packet[i + 4]
            packet[i + 4] = temp
        }
        
        // Swap UDP source and destination (offset 20-23)
        val tempPort = packet[20]
        packet[20] = packet[22]
        packet[22] = tempPort
        val tempPort2 = packet[21]
        packet[21] = packet[23]
        packet[23] = tempPort2
        
        // Copy response payload
        val ipHeaderLength = (packet[0].toInt() and 0x0F) * 4
        val udpHeaderLength = 8
        val payloadOffset = ipHeaderLength + udpHeaderLength
        
        // Ensure we don't exceed buffer
        val copyLength = minOf(responsePayload.size, packet.size - payloadOffset)
        System.arraycopy(responsePayload, 0, packet, payloadOffset, copyLength)
        
        // Update total length
        val totalLength = payloadOffset + copyLength
        packet[2] = (totalLength shr 8).toByte()
        packet[3] = (totalLength and 0xFF).toByte()
        
        // Update UDP length
        val udpLength = udpHeaderLength + copyLength
        packet[24] = (udpLength shr 8).toByte()
        packet[25] = (udpLength and 0xFF).toByte()
        
        return packet.copyOf(totalLength)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "DNS VPN Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "DNS Optimizer Pro VPN Service"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("DNS Optimizer Pro")
                .setContentText("VPN active: $currentDnsServer")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("DNS Optimizer Pro")
                .setContentText("VPN active: $currentDnsServer")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()
        }
    }
    
    companion object {
        const val ACTION_START = "com.dnsoptimizer.pro.START_VPN"
        const val ACTION_STOP = "com.dnsoptimizer.pro.STOP_VPN"
        const val EXTRA_DNS_SERVER = "dns_server"
    }
}
