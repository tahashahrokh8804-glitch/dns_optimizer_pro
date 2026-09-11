package com.dnsoptimizer.pro.ui.dnslist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnsoptimizer.pro.data.model.DnsCategory
import com.dnsoptimizer.pro.data.model.DnsProvider
import com.dnsoptimizer.pro.data.model.IranCompatibility
import com.dnsoptimizer.pro.data.model.ProtocolSupport
import com.dnsoptimizer.pro.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DnsListScreen(
    viewModel: DnsListViewModel,
    onProviderSelected: (DnsProvider) -> Unit
) {
    val providers by viewModel.providers.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "DNS Providers",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${providers.size} providers available",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search providers...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category filters
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { viewModel.setCategory(null) },
                    label = { Text("All") }
                )
            }
            items(DnsCategory.entries.toList()) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { viewModel.setCategory(category) },
                    label = { Text(category.name.replace("_", " ")) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Provider list
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(providers) { provider ->
                ProviderCard(provider = provider, onClick = { onProviderSelected(provider) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderCard(provider: DnsProvider, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = provider.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = provider.ipv4Primary,
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkPrimary
                    )
                }

                // Iran compatibility badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (provider.iranCompatibility) {
                        IranCompatibility.EXCELLENT -> ExcellentGreen.copy(alpha = 0.15f)
                        IranCompatibility.GOOD -> GoodGreen.copy(alpha = 0.15f)
                        IranCompatibility.FAIR -> AverageYellow.copy(alpha = 0.15f)
                        else -> PoorOrange.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = provider.iranCompatibility.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (provider.iranCompatibility) {
                            IranCompatibility.EXCELLENT -> ExcellentGreen
                            IranCompatibility.GOOD -> GoodGreen
                            IranCompatibility.FAIR -> AverageYellow
                            else -> PoorOrange
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = provider.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Protocol support badges
            ProtocolBadges(provider.protocolSupport)
        }
    }
}

@Composable
fun ProtocolBadges(protocolSupport: ProtocolSupport) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        if (protocolSupport.udp) ProtocolBadge("UDP", UdpColor)
        if (protocolSupport.tcp) ProtocolBadge("TCP", TcpColor)
        if (protocolSupport.dot) ProtocolBadge("DoT", DotColor)
        if (protocolSupport.doh) ProtocolBadge("DoH", DohColor)
        if (protocolSupport.doh3) ProtocolBadge("DoH3", Doh3Color)
        if (protocolSupport.doq) ProtocolBadge("DoQ", DoqColor)
    }
}

@Composable
fun ProtocolBadge(label: String, color: androidx.compose.ui.graphics.Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
