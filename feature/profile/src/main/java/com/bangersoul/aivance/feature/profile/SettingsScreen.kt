package com.bangersoul.aivance.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    onNavigateToAppearance: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val uiState = viewModel?.uiState?.collectAsStateWithLifecycle()?.value
    val effects by rememberUpdatedState(viewModel?.effects)
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val currentOnSignOut by rememberUpdatedState(onSignOut)

    LaunchedEffect(Unit) {
        effects?.collect { effect ->
            when (effect) {
                is SettingsUiEffect.SignedOut -> currentOnSignOut()
                is SettingsUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                else -> {}
            }
        }
    }

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
        val settings = (uiState as? SettingsUiState.Success)?.settings

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { SettingsSectionHeader("General") }
            item { SettingsClickableItem(Icons.Rounded.Palette, "Appearance", "Theme, accent, and dynamic color", onNavigateToAppearance) }
            item { SettingsItem(Icons.Rounded.Language, "Language", "English") }

            item { SettingsSectionHeader("Notifications") }
            item {
                SettingsToggleItem(
                    icon = Icons.Rounded.WorkOutline,
                    title = "Job alerts",
                    subtitle = "New matching jobs",
                    checked = settings?.jobAlertsEnabled ?: true,
                    onCheckedChange = { viewModel?.onEvent(SettingsUiEvent.SetJobAlerts(it)) }
                )
            }
            item {
                SettingsToggleItem(
                    icon = Icons.Rounded.Event,
                    title = "Interview reminders",
                    subtitle = "Upcoming sessions",
                    checked = settings?.interviewRemindersEnabled ?: true,
                    onCheckedChange = { viewModel?.onEvent(SettingsUiEvent.SetInterviewReminders(it)) }
                )
            }
            item {
                SettingsToggleItem(
                    icon = Icons.Rounded.Quickreply,
                    title = "Follow-up reminders",
                    subtitle = "Application follow-ups",
                    checked = settings?.followUpRemindersEnabled ?: true,
                    onCheckedChange = { viewModel?.onEvent(SettingsUiEvent.SetFollowUpReminders(it)) }
                )
            }

            item { SettingsSectionHeader("Providers") }
            item { SettingsClickableItem(Icons.Rounded.AutoAwesome, "AI Configuration", "Models, temperature, and tokens", onNavigateToAiSettings) }
            item { SettingsClickableItem(Icons.Rounded.Tune, "Provider Management", "AI, Job, and Enrichment", onNavigateToProviders) }

            item { SettingsSectionHeader("Privacy & Security") }
            item { SettingsClickableItem(Icons.Rounded.PrivacyTip, "Privacy Center", "Encryption, export, and delete", onNavigateToPrivacy) }
            item { SettingsClickableItem(Icons.Rounded.BarChart, "Analytics Insights", "Detailed career KPIs", onNavigateToAnalytics) }
            item {
                SettingsClickableItem(
                    icon = Icons.Rounded.FileDownload,
                    title = "Export my data",
                    subtitle = "Download your data as JSON",
                    onClick = { viewModel?.onEvent(SettingsUiEvent.ExportSettings) }
                )
            }
            item {
                SettingsClickableItem(
                    icon = Icons.Rounded.DeleteForever,
                    title = "Delete account",
                    subtitle = "Permanently remove your data",
                    onClick = { showDeleteConfirm = true }
                )
            }

            item { SettingsSectionHeader("About") }
            item { SettingsClickableItem(Icons.Rounded.Info, "About Aivance", "Version 1.0.0") {} }

            item {
                Spacer(Modifier.size(8.dp))
            }
            item {
                OutlinedButton(
                    onClick = { viewModel?.onEvent(SettingsUiEvent.SignOut) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Rounded.Logout, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Sign Out")
                }
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete account?") },
            text = { Text("This permanently removes your profile and career data. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel?.onEvent(SettingsUiEvent.SignOut)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
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
            Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
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
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
