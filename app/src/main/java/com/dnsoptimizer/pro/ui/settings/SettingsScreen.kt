package com.dnsoptimizer.pro.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnsoptimizer.pro.ui.theme.*

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        // Database section
        SettingsSection(title = "DNS Database") {
            SettingsItem(
                icon = Icons.Filled.CloudDownload,
                title = "Update Database",
                subtitle = "Fetch latest DNS providers from remote server",
                onClick = { viewModel.updateDatabase() },
                trailing = {
                    if (uiState.isUpdating) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                }
            )

            uiState.lastUpdateMessage?.let { msg ->
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            SettingsItem(
                icon = Icons.Filled.Storage,
                title = "Bundled Database",
                subtitle = "20 DNS providers built-in for offline use",
                onClick = {}
            )
        }

        // Privacy section
        SettingsSection(title = "Privacy") {
            SettingsItem(
                icon = Icons.Filled.Security,
                title = "Privacy Policy",
                subtitle = "How we handle your data",
                onClick = {}
            )
            SettingsItem(
                icon = Icons.Filled.VisibilityOff,
                title = "No Telemetry",
                subtitle = "This app does not collect any personal data",
                onClick = {}
            )
        }

        // About section
        SettingsSection(title = "About") {
            SettingsItem(
                icon = Icons.Filled.Info,
                title = "DNS Optimizer Pro",
                subtitle = "Version 1.0.0 - Gaming DNS Optimization",
                onClick = {}
            )
            SettingsItem(
                icon = Icons.Filled.Code,
                title = "Open Source",
                subtitle = "Built with Kotlin, Jetpack Compose, Material 3",
                onClick = {}
            )
        }

        // Important notice
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSecondary.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = null,
                        tint = DarkSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Important",
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "DNS optimization affects domain resolution, matchmaking, login, and launcher requests. It does NOT reduce in-game latency after a connection is established.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = DarkPrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(content = content)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    Surface(
        onClick = onClick,
        color = androidx.compose.ui.graphics.Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            trailing?.invoke()
        }
    }
}
