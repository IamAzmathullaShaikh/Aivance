package com.bangersoul.aivance.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bangersoul.aivance.core.designsystem.components.AivanceScreen
import com.bangersoul.aivance.core.designsystem.components.DashboardCard
import com.bangersoul.aivance.core.designsystem.components.SectionHeader
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme
import com.bangersoul.aivance.core.designsystem.theme.Zinc800
import com.bangersoul.aivance.feature.profile.ProfileUiState

@OptIn(ExperimentalMaterial3Api::class)
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
    val uiState by viewModel.uiState.collectAsState()

    AivanceScreen(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        isLoading = uiState is ProfileUiState.Loading,
        error = (uiState as? ProfileUiState.Error)?.message,
        onRetry = { viewModel.onEvent(ProfileUiEvent.LoadProfile) }
    ) {
        when (val state = uiState) {
            is ProfileUiState.Success -> {
                ProfileContent(
                    fullName = state.fullName,
                    email = state.email,
                    targetRole = state.targetRole,
                    skills = state.skills,
                    experienceYears = state.experienceYears,
                    apiKey = state.apiKey,
                    onEvent = viewModel::onEvent,
                    onNavigateToInterview = onNavigateToInterview
                )
            }
            else -> {}
        }
    }
}

@Composable
private fun ProfileContent(
    fullName: String,
    email: String,
    targetRole: String,
    skills: String,
    experienceYears: Int,
    apiKey: String,
    onEvent: (ProfileUiEvent) -> Unit,
    onNavigateToInterview: () -> Unit
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
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Profile Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            DashboardCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Personal Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = name, onValueChange = { name = it; onEvent(ProfileUiEvent.UpdateFullName(it)) }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = emailField, onValueChange = { emailField = it; onEvent(ProfileUiEvent.UpdateEmail(it)) }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = expYears, onValueChange = { expYears = it; it.toIntOrNull()?.let { v -> onEvent(ProfileUiEvent.UpdateExperience(v)) } }, label = { Text("Years of Experience") }, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        item {
            DashboardCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Career Goals", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = role, onValueChange = { role = it; onEvent(ProfileUiEvent.UpdateTargetRole(it)) }, label = { Text("Target Role") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = skillsField, onValueChange = { skillsField = it; onEvent(ProfileUiEvent.UpdateSkills(it)) }, label = { Text("Skills (comma separated)") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                }
            }
        }

        item {
            DashboardCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("API Key", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = keyField,
                        onValueChange = { keyField = it; onEvent(ProfileUiEvent.UpdateApiKey(it)) },
                        label = { Text("Gemini API Key") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                Icon(if (isKeyVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, contentDescription = null)
                            }
                        }
                    )
                }
            }
        }

        item {
            Button(
                onClick = { onEvent(ProfileUiEvent.SaveProfile) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Save Profile", modifier = Modifier.padding(vertical = 8.dp))
            }
        }

        item {
            Button(
                onClick = onNavigateToInterview,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
            ) {
                Text("Practice Interviews", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ProfileScreenPreview() {
    AivanceTheme(darkTheme = true) {
        ProfileContent(
            fullName = "John Doe",
            email = "john@example.com",
            targetRole = "Senior Android Engineer",
            skills = "Kotlin, Compose, MVVM",
            experienceYears = 5,
            apiKey = "sk-...",
            onEvent = {},
            onNavigateToInterview = {}
        )
    }
}
