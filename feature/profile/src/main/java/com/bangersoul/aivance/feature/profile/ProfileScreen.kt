package com.bangersoul.aivance.feature.profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bangersoul.aivance.core.designsystem.components.AivanceScreen
import com.bangersoul.aivance.core.designsystem.components.DashboardCard
import com.bangersoul.aivance.core.designsystem.components.SectionHeader
import com.bangersoul.aivance.core.designsystem.components.TimelineItem
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme
import com.bangersoul.aivance.core.designsystem.theme.Zinc800
import com.bangersoul.aivance.feature.profile.domain.CareerRoadmap
import com.bangersoul.aivance.feature.profile.domain.RoadmapStep

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToInterview: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val geminiApiKey by viewModel.geminiApiKey.collectAsState()

    AivanceScreen(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Roadmap") },
                actions = {
                    if (uiState is RoadmapUiState.Success) {
                        IconButton(onClick = { viewModel.resetRoadmap() }) {
                            Icon(Icons.Rounded.Refresh, contentDescription = "Reset Roadmap")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        isLoading = uiState is RoadmapUiState.Loading,
        error = (uiState as? RoadmapUiState.Error)?.message,
        onRetry = { /* Implementation depends on which action failed */ }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = AivanceTheme.spacing.extraLarge)
        ) {
            item {
                AnimatedContent(
                    targetState = uiState,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "RoadmapContentTransition"
                ) { state ->
                    when (state) {
                        is RoadmapUiState.Idle -> {
                            RoadmapInputForm(
                                onGenerate = { role, skills ->
                                    viewModel.generateRoadmap(role, skills)
                                }
                            )
                        }

                        is RoadmapUiState.Success -> {
                            RoadmapHeader(
                                roadmap = state.roadmap,
                                progress = viewModel.calculateProgress(state.roadmap)
                            )
                        }

                        else -> {
                            Spacer(modifier = Modifier.height(1.dp))
                        }
                    }
                }
            }

            if (uiState is RoadmapUiState.Success) {
                val roadmap = (uiState as RoadmapUiState.Success).roadmap
                itemsIndexed(roadmap.steps) { index, step ->
                    TimelineItem(
                        title = step.title,
                        description = step.description,
                        isCompleted = step.isCompleted,
                        onCheckedChange = { viewModel.toggleStep(roadmap.id, step.id, it) },
                        isLast = index == roadmap.steps.lastIndex,
                        modifier = Modifier.padding(horizontal = AivanceTheme.spacing.large)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(AivanceTheme.spacing.extraLarge))
                    Button(
                        onClick = onNavigateToInterview,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AivanceTheme.spacing.large),
                        shape = AivanceTheme.shapes.medium
                    ) {
                        Text("Practice Interviews", modifier = Modifier.padding(vertical = AivanceTheme.spacing.small))
                    }
                }
            }

            item {
                SettingsSection(
                    apiKey = geminiApiKey,
                    onSaveApiKey = viewModel::updateGeminiApiKey,
                    modifier = Modifier.padding(horizontal = AivanceTheme.spacing.large)
                )
            }
        }
    }
}

@Composable
private fun RoadmapInputForm(
    onGenerate: (String, String) -> Unit
) {
    var role by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AivanceTheme.spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Plan Your Career",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Tell us your target role and current skills to generate a tailored roadmap.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = AivanceTheme.spacing.small, bottom = AivanceTheme.spacing.extraLarge)
        )

        OutlinedTextField(
            value = role,
            onValueChange = { role = it },
            label = { Text("Target Role") },
            placeholder = { Text("e.g. Senior Android Engineer") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Zinc800
            )
        )

        Spacer(modifier = Modifier.height(AivanceTheme.spacing.medium))

        OutlinedTextField(
            value = skills,
            onValueChange = { skills = it },
            label = { Text("Current Skills") },
            placeholder = { Text("e.g. Kotlin, Compose, MVVM") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Zinc800
            )
        )

        Spacer(modifier = Modifier.height(AivanceTheme.spacing.extraLarge))

        Button(
            onClick = { onGenerate(role, skills) },
            modifier = Modifier.fillMaxWidth(),
            enabled = role.isNotBlank() && skills.isNotBlank(),
            shape = AivanceTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Generate Roadmap", modifier = Modifier.padding(vertical = AivanceTheme.spacing.small))
        }
    }
}

@Composable
private fun RoadmapHeader(
    roadmap: CareerRoadmap,
    progress: Float
) {
    val completedSteps = roadmap.steps.count { it.isCompleted }
    val totalSteps = roadmap.steps.size

    Column(modifier = Modifier.padding(AivanceTheme.spacing.large)) {
        Text(
            text = "Career Roadmap for",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = roadmap.targetRole,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(AivanceTheme.spacing.large))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(AivanceTheme.spacing.small))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = Zinc800,
            strokeCap = StrokeCap.Round
        )

        Spacer(modifier = Modifier.height(AivanceTheme.spacing.small))

        Text(
            text = "$completedSteps of $totalSteps steps completed",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsSection(
    apiKey: String,
    onSaveApiKey: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var key by remember(apiKey) { mutableStateOf(apiKey) }
    var isVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        SectionHeader(
            title = "Settings",
            modifier = Modifier.padding(top = AivanceTheme.spacing.extraLarge, bottom = AivanceTheme.spacing.medium)
        )

        DashboardCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(AivanceTheme.spacing.medium)) {
                Text(
                    text = "Gemini API Key",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Required for generating personalized roadmaps and interview feedback.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = AivanceTheme.spacing.medium)
                )

                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isVisible = !isVisible }) {
                            Icon(
                                imageVector = if (isVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                contentDescription = if (isVisible) "Hide API Key" else "Show API Key"
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Zinc800
                    )
                )

                Spacer(modifier = Modifier.height(AivanceTheme.spacing.medium))

                Button(
                    onClick = { onSaveApiKey(key) },
                    modifier = Modifier.align(Alignment.End),
                    shape = AivanceTheme.shapes.medium,
                    enabled = key.isNotBlank() && key != apiKey
                ) {
                    Text("Save Key")
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun RoadmapInputFormPreview() {
    AivanceTheme(darkTheme = true) {
        RoadmapInputForm(onGenerate = { _, _ -> })
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun RoadmapSuccessPreview() {
    val mockRoadmap = CareerRoadmap(
        id = 1,
        targetRole = "Senior Android Engineer",
        currentSkills = "Kotlin, MVVM",
        steps = listOf(
            RoadmapStep(1, "Master Jetpack Compose", "Learn advanced layouts and animations.", 1, true),
            RoadmapStep(2, "Deep Dive into Coroutines", "Understand structured concurrency and flows.", 2, false),
            RoadmapStep(3, "System Design Practice", "Design scalable mobile architectures.", 3, false)
        )
    )
    AivanceTheme(darkTheme = true) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                RoadmapHeader(roadmap = mockRoadmap, progress = 0.33f)
            }
            itemsIndexed(mockRoadmap.steps) { index, step ->
                TimelineItem(
                    title = step.title,
                    description = step.description,
                    isCompleted = step.isCompleted,
                    onCheckedChange = { },
                    isLast = index == mockRoadmap.steps.lastIndex,
                    modifier = Modifier.padding(horizontal = AivanceTheme.spacing.large)
                )
            }
            item {
                SettingsSection(apiKey = "sk-...", onSaveApiKey = {})
            }
        }
    }
}
