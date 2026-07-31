package com.bangersoul.aivance.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bangersoul.aivance.core.designsystem.components.AivanceScreen
import com.bangersoul.aivance.core.designsystem.theme.DarkAccent
import com.bangersoul.aivance.core.designsystem.theme.Zinc800

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel? = null,
    onBack: () -> Unit = {},
    onNavigateToAiSettings: () -> Unit = {},
    onNavigateToProviders: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToAppearance: () -> Unit = {}
) {
    AivanceScreen(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { SettingsSectionHeader("Appearance") }
            item { SettingsClickableItem(Icons.Rounded.Palette, "Appearance", "Theme, accent, and dynamic color", onNavigateToAppearance) }

            item { SettingsSectionHeader("General") }
            item { SettingsItem(Icons.Rounded.Language, "Language", "English") }

            item { SettingsSectionHeader("Platform Intelligence") }
            item { SettingsClickableItem(Icons.Rounded.AutoAwesome, "AI Configuration", "Models, temperature, and tokens", onNavigateToAiSettings) }
            item { SettingsClickableItem(Icons.Rounded.Tune, "Provider Management", "AI, Job, and Enrichment", onNavigateToProviders) }

            item { SettingsSectionHeader("Privacy & Security") }
            item { SettingsClickableItem(Icons.Rounded.PrivacyTip, "Privacy Center", "Export, delete, and encryption status", onNavigateToPrivacy) }
            item { SettingsClickableItem(Icons.Rounded.BarChart, "Analytics Insights", "Detailed career KPIs", onNavigateToAnalytics) }

            item { SettingsSectionHeader("Storage") }
            item { SettingsClickableItem(Icons.Rounded.Storage, "Clear Cache", "Free up space") {} }

            item { SettingsSectionHeader("About") }
            item { SettingsClickableItem(Icons.Rounded.Info, "About Aivance", "Version 1.0.0") {} }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = DarkAccent,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingsItem(icon: ImageVector, title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Zinc800)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                if (subtitle.isNotEmpty()) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun SettingsClickableItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Zinc800)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                if (subtitle.isNotEmpty()) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
