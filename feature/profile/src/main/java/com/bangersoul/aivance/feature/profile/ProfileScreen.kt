package com.bangersoul.aivance.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bangersoul.aivance.core.designsystem.components.*
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme

/**
 * Profile hub — organizes the user's account, career, and platform
 * configuration into focused sections instead of one long scroll.
 */
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToInterview: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAiSettings: () -> Unit = {},
    onNavigateToProviders: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToRoadmap: () -> Unit = {},
    onNavigateToLearning: () -> Unit = {},
    onNavigateToSavedJobs: () -> Unit = {},
    onNavigateToAiChat: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        AivanceTopBar(title = "Profile")
        when (val state = uiState) {
            is ProfileUiState.Loading -> SkeletonDashboard(modifier = Modifier.fillMaxSize())
            is ProfileUiState.Error -> AivanceError(
                message = state.message,
                onRetry = { viewModel.onEvent(ProfileUiEvent.LoadProfile) }
            )
            is ProfileUiState.Success -> ProfileHubContent(
                fullName = state.fullName,
                email = state.email,
                targetRole = state.targetRole,
                skills = state.skills,
                experienceYears = state.experienceYears,
                apiKey = state.apiKey,
                onEvent = viewModel::onEvent,
                onNavigateToInterview = onNavigateToInterview,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToAiSettings = onNavigateToAiSettings,
                onNavigateToProviders = onNavigateToProviders,
                onNavigateToNotifications = onNavigateToNotifications,
                onNavigateToAnalytics = onNavigateToAnalytics,
                onNavigateToRoadmap = onNavigateToRoadmap,
                onNavigateToLearning = onNavigateToLearning,
                onNavigateToSavedJobs = onNavigateToSavedJobs,
                onNavigateToAiChat = onNavigateToAiChat
            )
            else -> {}
        }
    }
}

@Composable
private fun ProfileHubContent(
    fullName: String,
    email: String,
    targetRole: String,
    skills: String,
    experienceYears: Int,
    apiKey: String,
    onEvent: (ProfileUiEvent) -> Unit,
    onNavigateToInterview: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAiSettings: () -> Unit,
    onNavigateToProviders: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToRoadmap: () -> Unit,
    onNavigateToLearning: () -> Unit,
    onNavigateToSavedJobs: () -> Unit,
    onNavigateToAiChat: () -> Unit
) {
    var name by remember(fullName) { mutableStateOf(fullName) }
    var emailField by remember(email) { mutableStateOf(email) }
    var role by remember(targetRole) { mutableStateOf(targetRole) }
    var skillsField by remember(skills) { mutableStateOf(skills) }
    var expYears by remember(experienceYears) { mutableStateOf(experienceYears.toString()) }
    var keyField by remember(apiKey) { mutableStateOf(apiKey) }
    var isKeyVisible by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Identity header
        item {
            DashboardCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = AivanceTheme.shapes.extraLarge,
                        color = AivanceTheme.colors.accent.copy(alpha = 0.14f),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(
                                Icons.Rounded.Person,
                                contentDescription = null,
                                modifier = Modifier.size(30.dp),
                                tint = AivanceTheme.colors.accent
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            fullName.ifBlank { "Your Name" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            email.ifBlank { "Add your email" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (targetRole.isNotBlank()) {
                            StatusChip(
                                text = targetRole,
                                tone = BannerTone.INFO,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick actions
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item { ProfilePill("Saved Jobs", Icons.Rounded.BookmarkBorder, onNavigateToSavedJobs) }
                item { ProfilePill("AI Chat", Icons.Rounded.Chat, onNavigateToAiChat) }
                item { ProfilePill("Interview", Icons.Rounded.RecordVoiceOver, onNavigateToInterview) }
            }
        }

        // Personal section — inline editor
        item {
            SectionHeader(title = "Personal")
        }
        item {
            DashboardCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; onEvent(ProfileUiEvent.UpdateFullName(it)) },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = emailField,
                        onValueChange = { emailField = it; onEvent(ProfileUiEvent.UpdateEmail(it)) },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = expYears,
                        onValueChange = { expYears = it; it.toIntOrNull()?.let { v -> onEvent(ProfileUiEvent.UpdateExperience(v)) } },
                        label = { Text("Years of Experience") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Career section — target role & skills
        item {
            SectionHeader(title = "Career")
        }
        item {
            DashboardCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = { role = it; onEvent(ProfileUiEvent.UpdateTargetRole(it)) },
                        label = { Text("Target Role") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = skillsField,
                        onValueChange = { skillsField = it; onEvent(ProfileUiEvent.UpdateSkills(it)) },
                        label = { Text("Skills (comma separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    AivancePrimaryButton(
                        text = "Save Profile",
                        onClick = { onEvent(ProfileUiEvent.SaveProfile) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Career intelligence links
        item {
            ProfileActionCard("Career Analytics", "KPIs, trends, and insights", Icons.Rounded.BarChart, onNavigateToAnalytics)
        }
        item {
            ProfileActionCard("Career Roadmap", "Step-by-step growth plan", Icons.Rounded.Route, onNavigateToRoadmap)
        }
        item {
            ProfileActionCard("Learning Hub", "Skills and resources", Icons.Rounded.School, onNavigateToLearning)
        }

        // Account section
        item {
            SectionHeader(title = "Account")
        }
        item {
            ProfileActionCard("Settings", "Language and preferences", Icons.Rounded.Settings, onNavigateToSettings)
        }
        item {
            ProfileActionCard("Notifications", "Alerts and reminders", Icons.Rounded.Notifications, onNavigateToNotifications)
        }

        // Platform section
        item {
            SectionHeader(title = "Platform")
        }
        item {
            ProfileActionCard("AI Configuration", "Models and parameters", Icons.Rounded.AutoAwesome, onNavigateToAiSettings)
        }
        item {
            ProfileActionCard("Provider Management", "AI, job, and enrichment providers", Icons.Rounded.Tune, onNavigateToProviders)
        }
        item {
            DashboardCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("API Key", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = keyField,
                        onValueChange = { keyField = it; onEvent(ProfileUiEvent.UpdateApiKey(it)) },
                        label = { Text("Gemini API Key") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                Icon(
                                    if (isKeyVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = if (isKeyVisible) "Hide API key" else "Show API key"
                                )
                            }
                        }
                    )
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun ProfilePill(label: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = AivanceTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = AivanceTheme.colors.accent)
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ProfileActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    DashboardCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = AivanceTheme.shapes.medium,
                color = AivanceTheme.colors.accent.copy(alpha = 0.12f),
                modifier = Modifier.size(40.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = AivanceTheme.colors.accent)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ProfileScreenPreview() {
    AivanceTheme(darkTheme = true) {
        ProfileHubContent(
            fullName = "John Doe",
            email = "john@example.com",
            targetRole = "Senior Android Engineer",
            skills = "Kotlin, Compose, MVVM",
            experienceYears = 5,
            apiKey = "sk-...",
            onEvent = {},
            onNavigateToInterview = {},
            onNavigateToSettings = {},
            onNavigateToAiSettings = {},
            onNavigateToProviders = {},
            onNavigateToNotifications = {},
            onNavigateToAnalytics = {},
            onNavigateToRoadmap = {},
            onNavigateToLearning = {},
            onNavigateToSavedJobs = {},
            onNavigateToAiChat = {}
        )
    }
}
