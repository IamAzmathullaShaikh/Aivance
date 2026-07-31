package com.bangersoul.aivance.feature.interview

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QuestionAnswer
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bangersoul.aivance.core.common.model.InterviewSession
import com.bangersoul.aivance.core.designsystem.components.AivanceEmptyState
import com.bangersoul.aivance.core.designsystem.components.AivancePrimaryButton
import com.bangersoul.aivance.core.designsystem.components.AivanceScreen
import com.bangersoul.aivance.core.designsystem.components.AivanceSecondaryButton
import com.bangersoul.aivance.core.designsystem.components.BannerTone
import com.bangersoul.aivance.core.designsystem.components.InsightCard
import com.bangersoul.aivance.core.designsystem.components.MetricCard
import com.bangersoul.aivance.core.designsystem.components.ScoreGauge
import com.bangersoul.aivance.core.designsystem.components.StatusChip
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewScreen(
    viewModel: InterviewViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    AivanceScreen(
        topBar = {
            TopAppBar(
                title = { Text("Interview Preparation", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        isLoading = uiState is InterviewUiState.Preparing,
        error = (uiState as? InterviewUiState.Error)?.message,
        onRetry = { viewModel.onEvent(InterviewUiEvent.Reset) }
    ) {
        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "InterviewTransition"
        ) { state ->
            when (state) {
                InterviewUiState.Idle -> InterviewPracticeHub(
                    onStart = { r, c, t -> viewModel.onEvent(InterviewUiEvent.StartSession(r, c, t)) }
                )
                is InterviewUiState.Active -> MockInterviewSession(
                    state = state,
                    onAnswer = { viewModel.onEvent(InterviewUiEvent.SubmitAnswer(it)) },
                    onNext = { viewModel.onEvent(InterviewUiEvent.NextQuestion) },
                    onComplete = { viewModel.onEvent(InterviewUiEvent.Complete) }
                )
                is InterviewUiState.Review -> InterviewFeedbackDashboard(
                    session = state.session,
                    onNewSession = { viewModel.onEvent(InterviewUiEvent.Reset) }
                )
                else -> {}
            }
        }
    }
}

/** Session type configuration for the practice hub. */
private data class SessionType(
    val id: String,
    val label: String,
    val description: String,
    val icon: ImageVector
)

@Composable
private fun InterviewPracticeHub(
    onStart: (String, String, String) -> Unit
) {
    var role by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("TECHNICAL") }

    val sessionTypes = listOf(
        SessionType("TECHNICAL", "Technical Deep Dive", "Coding, system design, and domain expertise", Icons.Rounded.RecordVoiceOver),
        SessionType("BEHAVIORAL", "Behavioral Master", "STAR-method stories and leadership questions", Icons.Rounded.Groups),
        SessionType("HR", "HR & Culture Fit", "Motivation, salary expectations, and team fit", Icons.Rounded.QuestionAnswer)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("Practice Hub", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "AI-driven simulations tailored to your target role.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(28.dp))

        Text("Session type", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))

        sessionTypes.forEach { (type, label, description, icon) ->
            val selected = selectedType == type
            Card(
                onClick = { selectedType = type },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = AivanceTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                border = if (selected) {
                    BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                } else {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                }
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text(label, fontWeight = FontWeight.SemiBold)
                        Text(
                            description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        Text("Target role", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = role,
            onValueChange = { role = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Target role") },
            placeholder = { Text("e.g. Senior Android Engineer") },
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = company,
            onValueChange = { company = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Company (optional)") },
            placeholder = { Text("e.g. Stripe") },
            singleLine = true
        )

        Spacer(Modifier.height(32.dp))
        AivancePrimaryButton(
            text = "Start Mock Interview",
            onClick = { onStart(role, company, selectedType) },
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Rounded.PlayArrow,
            enabled = role.isNotBlank()
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Add a target role to personalize the questions.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun MockInterviewSession(
    state: InterviewUiState.Active,
    onAnswer: (String) -> Unit,
    onNext: () -> Unit,
    onComplete: () -> Unit
) {
    var answerText by remember { mutableStateOf("") }
    val session = state.session
    val question = session.questions.getOrNull(state.currentQuestionIndex)
    val total = session.questions.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(session.targetRole, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (session.companyName.isNotBlank()) {
                    Text(
                        session.companyName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            StatusChip(
                text = if (total > 0) "Question ${state.currentQuestionIndex + 1} of $total" else "Preparing",
                tone = BannerTone.INFO
            )
        }

        if (total > 0) {
            Spacer(Modifier.height(20.dp))
            LinearProgressIndicator(
                progress = { (state.currentQuestionIndex + 1).coerceAtMost(total) / total.toFloat() },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = AivanceTheme.colors.accent,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        Spacer(Modifier.height(20.dp))

        if (question != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = AivanceTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Question",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(question.text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    if (question.category.isNotBlank()) {
                        StatusChip(text = question.category, tone = BannerTone.INFO)
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = AivanceTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = AivanceTheme.colors.accent,
                        strokeWidth = 3.dp
                    )
                    Text("Preparing your questions…", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Your session has started. AI is tailoring questions to ${session.targetRole}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = answerText,
            onValueChange = { answerText = it },
            modifier = Modifier.fillMaxWidth().height(180.dp),
            label = { Text("Your answer") },
            placeholder = { Text("Use the STAR method — situation, task, action, result…") },
            enabled = !state.isSubmitting
        )

        Spacer(Modifier.height(20.dp))

        AivancePrimaryButton(
            text = "Submit Answer",
            onClick = {
                onAnswer(answerText)
                answerText = ""
            },
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Rounded.Send,
            enabled = answerText.length > 20 && !state.isSubmitting
        )

        if (state.isSubmitting) {
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        if (question != null) {
            Spacer(Modifier.height(8.dp))
            AivanceSecondaryButton(
                text = "Next Question",
                onClick = onNext,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onComplete, modifier = Modifier.fillMaxWidth()) {
            Text("End Session", color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun InterviewFeedbackDashboard(
    session: InterviewSession,
    onNewSession: () -> Unit
) {
    val feedback = session.feedback

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Session Review", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(
            buildString {
                append(session.targetRole)
                if (session.companyName.isNotBlank()) append(" · ").append(session.companyName)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))

        if (feedback != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = AivanceTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    ScoreGauge(score = feedback.overallScore, size = 110.dp)
                    Column {
                        Text("Overall Performance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "AI evaluation of your answers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (feedback.detailedSummary.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                InsightCard(text = feedback.detailedSummary, icon = Icons.Rounded.Lightbulb)
            }

            if (feedback.strengths.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                Text("Key Strengths", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                feedback.strengths.forEach { strength ->
                    Row(
                        Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            null,
                            tint = AivanceTheme.colors.success,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(strength, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (feedback.improvements.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                Text("Areas to Improve", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                feedback.improvements.forEach { improvement ->
                    Row(
                        Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Lightbulb,
                            null,
                            tint = AivanceTheme.colors.warning,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(improvement, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        } else {
            AivanceEmptyState(
                title = "Analysis in progress",
                description = "Your answers have been recorded. Detailed AI feedback will appear here once the evaluation completes.",
                icon = Icons.Rounded.TrendingUp,
                compact = true
            )
            Spacer(Modifier.height(16.dp))
            MetricCard(
                label = "Questions prepared",
                value = "${session.questions.size}",
                icon = Icons.Rounded.QuestionAnswer
            )
            Spacer(Modifier.height(12.dp))
            MetricCard(
                label = "Session type",
                value = session.type.replace('_', ' '),
                icon = Icons.Rounded.RecordVoiceOver
            )
        }

        Spacer(Modifier.height(32.dp))
        AivancePrimaryButton(
            text = "Start a New Session",
            onClick = onNewSession,
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Rounded.Refresh
        )
        Spacer(Modifier.height(48.dp))
    }
}
