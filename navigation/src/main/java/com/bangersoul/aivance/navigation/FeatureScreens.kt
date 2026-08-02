package com.bangersoul.aivance.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.bangersoul.aivance.core.designsystem.components.AivancePrimaryButton
import com.bangersoul.aivance.core.designsystem.components.AivanceScreen
import com.bangersoul.aivance.core.designsystem.components.AivanceSecondaryButton
import com.bangersoul.aivance.core.designsystem.components.BannerTone
import com.bangersoul.aivance.core.designsystem.components.StatusChip
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme
import com.bangersoul.aivance.feature.profile.ProviderCategory
import com.bangersoul.aivance.feature.profile.ProviderHealthStatus
import com.bangersoul.aivance.feature.profile.ProviderManagementUiEvent
import com.bangersoul.aivance.feature.profile.ProviderManagementUiState
import com.bangersoul.aivance.feature.profile.AiSettingsViewModel
import com.bangersoul.aivance.feature.profile.AnalyticsDashboardViewModel
import com.bangersoul.aivance.feature.profile.CareerRoadmapViewModel
import com.bangersoul.aivance.feature.profile.LearningHubViewModel
import com.bangersoul.aivance.feature.profile.NotificationsViewModel
import com.bangersoul.aivance.feature.profile.ProviderManagementViewModel

// ──────────────────────────────────────────────────
// AI Settings Screen
// ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSettingsScreen(
    viewModel: AiSettingsViewModel,
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    AivanceScreen(
        topBar = {
            TopAppBar(
                title = { Text("AI Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Rounded.Settings, null, tint = MaterialTheme.colorScheme.primary)
            Text(
                "AI Configuration",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            when (uiState) {
                is com.bangersoul.aivance.feature.profile.AiSettingsUiState.Loading -> {
                    Text("Loading...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                is com.bangersoul.aivance.feature.profile.AiSettingsUiState.Success -> {
                    val state = uiState as com.bangersoul.aivance.feature.profile.AiSettingsUiState.Success
                    Text(
                        "Provider: ${state.config.providerName}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "Model: ${state.config.selectedModel.ifEmpty { "Not set" }}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Status: ${state.connectionStatus.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                is com.bangersoul.aivance.feature.profile.AiSettingsUiState.Error -> {
                    Text(
                        (uiState as com.bangersoul.aivance.feature.profile.AiSettingsUiState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────
// Provider Management Screen
// ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderManagementScreen(
    viewModel: ProviderManagementViewModel,
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is com.bangersoul.aivance.feature.profile.ProviderManagementUiEffect.ShowSnackbar ->
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                is com.bangersoul.aivance.feature.profile.ProviderManagementUiEffect.ConnectionTestResult ->
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                else -> {}
            }
        }
    }

    AivanceScreen(
        topBar = {
            TopAppBar(
                title = { Text("Providers", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(ProviderManagementUiEvent.Refresh) }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Refresh providers")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) {
        when (val state = uiState) {
            is ProviderManagementUiState.Loading -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("Loading providers…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            is ProviderManagementUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    AivancePrimaryButton(
                        text = "Retry",
                        onClick = { viewModel.onEvent(ProviderManagementUiEvent.Refresh) }
                    )
                }
            }
            is ProviderManagementUiState.Success -> {
                ProviderManagementList(
                    state = state,
                    onEvent = viewModel::onEvent
                )
            }
        }
    }
}

@Composable
private fun ProviderManagementList(
    state: ProviderManagementUiState.Success,
    onEvent: (ProviderManagementUiEvent) -> Unit
) {
    val aiProviders = state.providers.filter { it.category == ProviderCategory.AI }
    val jobProviders = state.providers.filter { it.category == ProviderCategory.JOB }
    val enrichmentProviders = state.providers.filter { it.category == ProviderCategory.ENRICHMENT }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionLabel("AI Providers") }
        items(aiProviders, key = { it.id }) { provider ->
            ProviderCard(provider, state, onEvent)
        }
        if (jobProviders.isNotEmpty()) {
            item { SectionLabel("Job Providers") }
            items(jobProviders, key = { it.id }) { provider ->
                ProviderCard(provider, state, onEvent)
            }
        }
        if (enrichmentProviders.isNotEmpty()) {
            item { SectionLabel("Enrichment Providers") }
            items(enrichmentProviders, key = { it.id }) { provider ->
                ProviderCard(provider, state, onEvent)
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun ProviderCard(
    provider: com.bangersoul.aivance.feature.profile.ProviderInfo,
    state: ProviderManagementUiState.Success,
    onEvent: (ProviderManagementUiEvent) -> Unit
) {
    val apiKeyDraft = state.apiKeyDrafts[provider.id].orEmpty()
    var modelMenuOpen by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AivanceTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(provider.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (provider.description.isNotBlank()) {
                        Text(
                            provider.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }
                ProviderHealthChip(provider.healthStatus)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (provider.isEnabled) "Enabled" else "Disabled",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (provider.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Switch(
                    checked = provider.isEnabled,
                    onCheckedChange = { onEvent(ProviderManagementUiEvent.ToggleProvider(provider.id, it)) }
                )
            }

            OutlinedTextField(
                value = apiKeyDraft,
                onValueChange = { onEvent(ProviderManagementUiEvent.SetApiKey(provider.id, it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(if (provider.apiKeyConfigured) "API Key (configured)" else "API Key") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                trailingIcon = { Icon(Icons.Rounded.Key, contentDescription = null) }
            )

            if (provider.availableModels.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Model", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(0.3f))
                    androidx.compose.material3.OutlinedButton(
                        onClick = { modelMenuOpen = true },
                        modifier = Modifier.weight(0.7f)
                    ) {
                        Text(
                            provider.selectedModel.ifBlank { "Select…" },
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    DropdownMenu(
                        expanded = modelMenuOpen,
                        onDismissRequest = { modelMenuOpen = false }
                    ) {
                        provider.availableModels.forEach { model ->
                            DropdownMenuItem(
                                text = { Text(model) },
                                onClick = {
                                    modelMenuOpen = false
                                    onEvent(ProviderManagementUiEvent.SelectModel(provider.id, model))
                                }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AivanceSecondaryButton(
                    text = "Save",
                    onClick = { onEvent(ProviderManagementUiEvent.SaveProvider(provider.id)) },
                    modifier = Modifier.weight(1f)
                )
                AivancePrimaryButton(
                    text = if (state.testingProviderId == provider.id) "Testing…" else "Test",
                    onClick = { onEvent(ProviderManagementUiEvent.TestConnection(provider.id)) },
                    modifier = Modifier.weight(1f),
                    enabled = state.testingProviderId != provider.id
                )
            }
        }
    }
}

@Composable
private fun ProviderHealthChip(status: ProviderHealthStatus) {
    val (tone, label) = when (status) {
        ProviderHealthStatus.HEALTHY -> BannerTone.SUCCESS to "Healthy"
        ProviderHealthStatus.DEGRADED -> BannerTone.WARNING to "Degraded"
        ProviderHealthStatus.UNHEALTHY -> BannerTone.ERROR to "Unhealthy"
        ProviderHealthStatus.UNKNOWN -> BannerTone.INFO to "Unknown"
    }
    StatusChip(text = label, tone = tone)
}

// ──────────────────────────────────────────────────
// Notifications Screen
// ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    AivanceScreen(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Rounded.Notifications, null, tint = MaterialTheme.colorScheme.primary)
            Text(
                "Notifications",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            when (uiState) {
                is com.bangersoul.aivance.feature.profile.NotificationsUiState.Loading ->
                    Text("Loading...")
                is com.bangersoul.aivance.feature.profile.NotificationsUiState.Success -> {
                    val state = uiState as com.bangersoul.aivance.feature.profile.NotificationsUiState.Success
                    Text("${state.unreadCount} unread", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary)
                    if (state.notifications.isEmpty()) {
                        Text("No notifications yet",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 24.dp))
                    }
                }
                is com.bangersoul.aivance.feature.profile.NotificationsUiState.Empty ->
                    Text("No notifications yet", style = MaterialTheme.typography.bodyLarge)
                is com.bangersoul.aivance.feature.profile.NotificationsUiState.Error ->
                    Text((uiState as com.bangersoul.aivance.feature.profile.NotificationsUiState.Error).message,
                        color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// Career Roadmap Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerRoadmapScreen(
    viewModel: CareerRoadmapViewModel,
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    AivanceScreen(
        topBar = {
            TopAppBar(
                title = { Text("Career Roadmap", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Rounded.Route, null, tint = MaterialTheme.colorScheme.primary)
            Text("Career Roadmap", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            when (uiState) {
                is com.bangersoul.aivance.feature.profile.CareerRoadmapUiState.Idle -> {
                    Text("Set your target role to get started.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                is com.bangersoul.aivance.feature.profile.CareerRoadmapUiState.Loading ->
                    Text("Generating roadmap...")
                is com.bangersoul.aivance.feature.profile.CareerRoadmapUiState.Success -> {
                    val state = uiState as com.bangersoul.aivance.feature.profile.CareerRoadmapUiState.Success
                    Text("Target: ${state.roadmap.targetRole}", fontWeight = FontWeight.Bold)
                    Text("Progress: ${(state.progressPercent * 100).toInt()}%")
                    state.roadmap.steps.forEach { step ->
                        Text("• ${step.title} - ${if (step.isCompleted) "✅" else "☐"}")
                    }
                }
                is com.bangersoul.aivance.feature.profile.CareerRoadmapUiState.Error ->
                    Text((uiState as com.bangersoul.aivance.feature.profile.CareerRoadmapUiState.Error).message,
                        color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// ──────────────────────────────────────────────────
// Learning Hub Screen
// ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningHubScreen(
    viewModel: LearningHubViewModel,
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    AivanceScreen(
        topBar = {
            TopAppBar(
                title = { Text("Learning Hub", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Rounded.School, null, tint = MaterialTheme.colorScheme.primary)
            Text("Learning Hub", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            when (uiState) {
                is com.bangersoul.aivance.feature.profile.LearningHubUiState.Idle -> {
                    Text("Discover learning resources to level up your career.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                is com.bangersoul.aivance.feature.profile.LearningHubUiState.Loading ->
                    Text("Loading resources...")
                is com.bangersoul.aivance.feature.profile.LearningHubUiState.Success -> {
                    val state = uiState as com.bangersoul.aivance.feature.profile.LearningHubUiState.Success
                    Text("Recommended Skills:", fontWeight = FontWeight.Bold)
                    state.recommendedSkills.forEach { skill ->
                        Text("• $skill")
                    }
                    Spacer(module = Modifier.padding(vertical = 8.dp))
                    Text("Resources:", fontWeight = FontWeight.Bold)
                    state.suggestedResources.forEach { resource ->
                        Text("• ${resource.title} (${resource.type.name})")
                    }
                }
                is com.bangersoul.aivance.feature.profile.LearningHubUiState.Error ->
                    Text((uiState as com.bangersoul.aivance.feature.profile.LearningHubUiState.Error).message,
                        color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun Spacer(module: Modifier) {
    androidx.compose.foundation.layout.Spacer(modifier = module)
}
