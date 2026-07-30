package com.bangersoul.aivance.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bangersoul.aivance.core.designsystem.components.AivanceScreen
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

    AivanceScreen(
        topBar = {
            TopAppBar(
                title = { Text("AI Providers", fontWeight = FontWeight.Bold) },
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
            Icon(Icons.Rounded.Tune, null, tint = MaterialTheme.colorScheme.primary)
            Text(
                "Manage Providers",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            when (uiState) {
                is com.bangersoul.aivance.feature.profile.ProviderManagementUiState.Loading -> {
                    Text("Loading...")
                }
                is com.bangersoul.aivance.feature.profile.ProviderManagementUiState.Success -> {
                    val state = uiState as com.bangersoul.aivance.feature.profile.ProviderManagementUiState.Success
                    state.providers.forEach { provider ->
                        Text(
                            "${provider.name} - ${if (provider.isEnabled) "Enabled" else "Disabled"}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                is com.bangersoul.aivance.feature.profile.ProviderManagementUiState.Error -> {
                    Text(
                        (uiState as com.bangersoul.aivance.feature.profile.ProviderManagementUiState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
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

// ──────────────────────────────────────────────────
// Analytics Dashboard Screen
// ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsDashboardScreen(
    viewModel: AnalyticsDashboardViewModel,
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    AivanceScreen(
        topBar = {
            TopAppBar(
                title = { Text("Analytics", fontWeight = FontWeight.Bold) },
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
            Icon(Icons.Rounded.BarChart, null, tint = MaterialTheme.colorScheme.primary)
            Text("Analytics Dashboard", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            when (uiState) {
                is com.bangersoul.aivance.feature.profile.AnalyticsDashboardUiState.Loading ->
                    Text("Loading...")
                is com.bangersoul.aivance.feature.profile.AnalyticsDashboardUiState.Success -> {
                    val state = uiState as com.bangersoul.aivance.feature.profile.AnalyticsDashboardUiState.Success
                    Text("Analyses: ${state.totalAnalyses}", style = MaterialTheme.typography.bodyLarge)
                    Text("Cover Letters: ${state.totalCoverLetters}", style = MaterialTheme.typography.bodyLarge)
                    Text("Interviews: ${state.totalInterviews}", style = MaterialTheme.typography.bodyLarge)
                    Text("Applications: ${state.totalApplications}", style = MaterialTheme.typography.bodyLarge)
                    Text("Avg ATS: ${state.averageAtsScore}%", style = MaterialTheme.typography.bodyLarge)
                }
                is com.bangersoul.aivance.feature.profile.AnalyticsDashboardUiState.Error ->
                    Text((uiState as com.bangersoul.aivance.feature.profile.AnalyticsDashboardUiState.Error).message,
                        color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// ──────────────────────────────────────────────────
// Career Roadmap Screen
// ──────────────────────────────────────────────────

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
