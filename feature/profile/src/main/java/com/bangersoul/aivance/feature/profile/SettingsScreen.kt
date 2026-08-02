package com.bangersoul.aivance.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
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
    onNavigateToProviders: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToAppearance: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
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
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
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
            item { SettingsSectionHeader(stringResource(R.string.section_general)) }
            item { SettingsClickableItem(Icons.Rounded.Palette, stringResource(R.string.appearance_item), stringResource(R.string.appearance_item_sub), onNavigateToAppearance) }
            item { LanguageSettingsItem(settings?.language ?: "en", onSelect = { viewModel?.onEvent(SettingsUiEvent.SetLanguage(it)) }) }

            item { SettingsSectionHeader(stringResource(R.string.section_notifications)) }
            item {
                SettingsToggleItem(
                    icon = Icons.Rounded.WorkOutline,
                    title = stringResource(R.string.job_alerts),
                    subtitle = stringResource(R.string.job_alerts_sub),
                    checked = settings?.jobAlertsEnabled ?: true,
                    onCheckedChange = { viewModel?.onEvent(SettingsUiEvent.SetJobAlerts(it)) }
                )
            }
            item {
                SettingsToggleItem(
                    icon = Icons.Rounded.Event,
                    title = stringResource(R.string.interview_reminders),
                    subtitle = stringResource(R.string.interview_reminders_sub),
                    checked = settings?.interviewRemindersEnabled ?: true,
                    onCheckedChange = { viewModel?.onEvent(SettingsUiEvent.SetInterviewReminders(it)) }
                )
            }
            item {
                SettingsToggleItem(
                    icon = Icons.Rounded.Quickreply,
                    title = stringResource(R.string.follow_up_reminders),
                    subtitle = stringResource(R.string.follow_up_reminders_sub),
                    checked = settings?.followUpRemindersEnabled ?: true,
                    onCheckedChange = { viewModel?.onEvent(SettingsUiEvent.SetFollowUpReminders(it)) }
                )
            }

            item { SettingsSectionHeader(stringResource(R.string.section_providers)) }
            // All AI / Job / Enrichment provider selection lives in Provider
            // Management alone — no duplicate dropdowns elsewhere.
            item { SettingsClickableItem(Icons.Rounded.Tune, stringResource(R.string.provider_management_item), stringResource(R.string.provider_management_item_sub), onNavigateToProviders) }

            item { SettingsSectionHeader(stringResource(R.string.section_privacy)) }
            item { SettingsClickableItem(Icons.Rounded.PrivacyTip, stringResource(R.string.privacy_center_item), stringResource(R.string.privacy_center_item_sub), onNavigateToPrivacy) }
            item { SettingsClickableItem(Icons.Rounded.BarChart, stringResource(R.string.analytics_insights), stringResource(R.string.analytics_insights_sub), onNavigateToAnalytics) }
            item {
                SettingsClickableItem(
                    icon = Icons.Rounded.FileDownload,
                    title = stringResource(R.string.export_my_data),
                    subtitle = stringResource(R.string.export_my_data_sub),
                    onClick = { viewModel?.onEvent(SettingsUiEvent.ExportSettings) }
                )
            }
            item {
                SettingsClickableItem(
                    icon = Icons.Rounded.DeleteForever,
                    title = stringResource(R.string.delete_account),
                    subtitle = stringResource(R.string.delete_account_sub),
                    onClick = { showDeleteConfirm = true }
                )
            }

            item { SettingsSectionHeader(stringResource(R.string.section_about)) }
            item { SettingsClickableItem(Icons.Rounded.Info, stringResource(R.string.about_item), stringResource(R.string.version_placeholder), onNavigateToAbout) }

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
                    Text(stringResource(R.string.sign_out))
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
            title = { Text(stringResource(R.string.delete_account_confirm)) },
            text = { Text(stringResource(R.string.delete_account_confirm_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel?.onEvent(SettingsUiEvent.SignOut)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
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

private val supportedLanguages = listOf(
    "en" to "English",
    "hi" to "हिन्दी (Hindi)",
    "es" to "Español",
    "fr" to "Français",
    "de" to "Deutsch",
    "zh" to "中文",
    "ja" to "日本語"
)

/**
 * Functional language picker — tapping the row opens a dialog of supported
 * languages; the selection persists through [SettingsViewModel] into the
 * encrypted DataStore preference.
 */
@Composable
private fun LanguageSettingsItem(
    current: String,
    onSelect: (String) -> Unit
) {
    var open by remember { mutableStateOf(false) }
    val currentName = supportedLanguages.firstOrNull { it.first == current }?.second ?: stringResource(R.string.language_english)

    Card(
        onClick = { open = true },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Zinc800)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Rounded.Language, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.language), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(currentName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    if (open) {
        AlertDialog(
            onDismissRequest = { open = false },
            title = { Text(stringResource(R.string.language)) },
            text = {
                Column {
                    supportedLanguages.forEach { (code, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(code)
                                    open = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            if (code == current) {
                                Icon(
                                    Icons.Rounded.CheckCircle,
                                    contentDescription = stringResource(R.string.selected),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { open = false }) { Text(stringResource(R.string.done)) }
            }
        )
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
