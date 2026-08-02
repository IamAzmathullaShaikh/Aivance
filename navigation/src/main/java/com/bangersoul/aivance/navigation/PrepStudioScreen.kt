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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bangersoul.aivance.core.designsystem.components.AivanceEmptyState
import com.bangersoul.aivance.core.designsystem.components.AivancePrimaryButton
import com.bangersoul.aivance.core.designsystem.components.AivanceScreen
import com.bangersoul.aivance.core.designsystem.components.InsightCard
import com.bangersoul.aivance.core.designsystem.components.ScoreGauge
import com.bangersoul.aivance.core.designsystem.components.StatusChip
import com.bangersoul.aivance.core.designsystem.components.BannerTone
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme
import com.bangersoul.aivance.feature.interview.InterviewUiEvent
import com.bangersoul.aivance.feature.interview.InterviewUiState
import com.bangersoul.aivance.feature.interview.InterviewViewModel
import com.bangersoul.aivance.feature.interview.QuestionBankUiEvent
import com.bangersoul.aivance.feature.interview.QuestionBankUiState
import com.bangersoul.aivance.feature.interview.QuestionBankViewModel
import com.bangersoul.aivance.feature.profile.LearningHubUiState
import com.bangersoul.aivance.feature.profile.LearningHubViewModel

/**
 * Prep Studio — the merged Interview + Learning intelligence engine.
 *
 * Replaces the separate Interview and Learning Hub destinations with a single
 * tabbed surface: Practice (live mock interview), History (past sessions and
 * AI feedback), and Learn (recommended skills + resources).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrepStudioScreen(
    interviewViewModel: InterviewViewModel,
    questionBankViewModel: QuestionBankViewModel = hiltViewModel(),
    learningViewModel: LearningHubViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Practice", "History", "Question Bank", "Learn")

    AivanceScreen(
        topBar = {
            TopAppBar(
                title = { Text("Prep Studio", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(label) }
                    )
                }
            }
            when (selectedTab) {
                0 -> PracticeTab(interviewViewModel)
                1 -> HistoryTab(interviewViewModel)
                2 -> QuestionBankTab(questionBankViewModel)
                3 -> LearnTab(learningViewModel)
            }
        }
    }
}

@Composable
private fun PracticeTab(viewModel: InterviewViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (val state = uiState) {
        is InterviewUiState.Idle -> PracticeHub(
            onStart = { r, c, t -> viewModel.onEvent(InterviewUiEvent.StartSession(r, c, t)) }
        )
        is InterviewUiState.Preparing -> LoadingPanel("Starting your session…")
        is InterviewUiState.Active -> ActiveSessionPanel(
            state = state,
            onAnswer = { viewModel.onEvent(InterviewUiEvent.SubmitAnswer(it)) },
            onNext = { viewModel.onEvent(InterviewUiEvent.NextQuestion) },
            onComplete = { viewModel.onEvent(InterviewUiEvent.Complete) }
        )
        is InterviewUiState.Review -> SessionReviewPanel(
            session = state.session,
            onNewSession = { viewModel.onEvent(InterviewUiEvent.Reset) }
        )
        is InterviewUiState.Error -> AivanceEmptyState(
            title = "Session failed",
            description = state.message,
            icon = Icons.Rounded.ErrorOutline
        )
    }
}

@Composable
private fun PracticeHub(onStart: (String, String, String) -> Unit) {
    var role by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("BEHAVIORAL") }

    val types = listOf(
        "TECHNICAL" to "Technical",
        "BEHAVIORAL" to "Behavioral",
        "HR" to "HR & Culture"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("New Practice Session", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            "AI-driven simulations tailored to your target role.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(8.dp))
        Text("Session type", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            types.forEach { (id, label) ->
                FilterChip(
                    selected = type == id,
                    onClick = { type = id },
                    label = { Text(label) }
                )
            }
        }

        OutlinedTextField(
            value = role,
            onValueChange = { role = it },
            label = { Text("Target role") },
            placeholder = { Text("e.g. Senior Android Engineer") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = company,
            onValueChange = { company = it },
            label = { Text("Company (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))
        AivancePrimaryButton(
            text = "Start Mock Interview",
            onClick = { onStart(role, company, type) },
            modifier = Modifier.fillMaxWidth(),
            enabled = role.isNotBlank()
        )
    }
}

@Composable
private fun LoadingPanel(text: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(36.dp))
        Spacer(Modifier.height(16.dp))
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ActiveSessionPanel(
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
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(session.targetRole, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (session.companyName.isNotBlank()) {
                    Text(session.companyName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            StatusChip(
                text = if (total > 0) "Q${state.currentQuestionIndex + 1}/$total" else "Preparing",
                tone = BannerTone.INFO
            )
        }

        if (total > 0) {
            LinearProgressIndicator(
                progress = { (state.currentQuestionIndex + 1).coerceAtMost(total) / total.toFloat() },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = AivanceTheme.colors.accent
            )
        }

        question?.let { q ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Question", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    Text(q.text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    if (q.category.isNotBlank()) StatusChip(text = q.category, tone = BannerTone.INFO)
                }
            }
        } ?: LoadingPanel("Preparing your questions…")

        OutlinedTextField(
            value = answerText,
            onValueChange = { answerText = it },
            label = { Text("Your answer") },
            placeholder = { Text("Use the STAR method — situation, task, action, result…") },
            modifier = Modifier.fillMaxWidth().height(160.dp),
            enabled = !state.isSubmitting
        )

        AivancePrimaryButton(
            text = "Submit Answer",
            onClick = {
                onAnswer(answerText)
                answerText = ""
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = answerText.length > 20 && !state.isSubmitting
        )

        if (question != null) {
            OutlinedButton(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                Text("Next Question")
            }
        }
        TextButton(onClick = onComplete, modifier = Modifier.fillMaxWidth()) {
            Text("End Session", color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun SessionReviewPanel(
    session: com.bangersoul.aivance.core.common.model.InterviewSession,
    onNewSession: () -> Unit
) {
    val feedback = session.feedback
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Session Review", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        if (feedback != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    ScoreGauge(score = feedback.overallScore, size = 100.dp)
                    Column {
                        Text("Overall Performance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("AI evaluation of your answers", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (feedback.detailedSummary.isNotBlank()) {
                InsightCard(text = feedback.detailedSummary, icon = Icons.Rounded.Lightbulb)
            }
            if (feedback.strengths.isNotEmpty()) {
                Text("Key Strengths", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                feedback.strengths.forEach { strength ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = AivanceTheme.colors.success, modifier = Modifier.size(18.dp))
                        Text(strength, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        } else {
            AivanceEmptyState(
                title = "Analysis in progress",
                description = "Detailed AI feedback will appear once evaluation completes.",
                icon = Icons.Rounded.TrendingUp,
                compact = true
            )
        }
        AivancePrimaryButton(
            text = "Start a New Session",
            onClick = onNewSession,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun HistoryTab(viewModel: InterviewViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val history = (uiState as? InterviewUiState.Idle)?.history.orEmpty()

    if (history.isEmpty()) {
        AivanceEmptyState(
            title = "No sessions yet",
            description = "Complete a practice session and your progress will appear here.",
            icon = Icons.Rounded.History
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(history) { session ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        if (session.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.RecordVoiceOver,
                        null,
                        tint = if (session.isCompleted) AivanceTheme.colors.success else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(Modifier.weight(1f)) {
                        Text(session.targetRole, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            buildString {
                                append(session.type.replace('_', ' '))
                                if (session.companyName.isNotBlank()) append(" · ").append(session.companyName)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    session.feedback?.overallScore?.let {
                        Text("$it", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AivanceTheme.colors.accent)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionBankTab(viewModel: QuestionBankViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val categories = listOf(
        "ALL" to "All",
        "FAVORITES" to "★ Favorites",
        "BEHAVIORAL" to "Behavioral",
        "TECHNICAL" to "Technical",
        "LEADERSHIP" to "Leadership",
        "GENERAL" to "General"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val current = uiState as? QuestionBankUiState.Content
            categories.forEach { (id, label) ->
                FilterChip(
                    selected = current?.category == id,
                    onClick = { viewModel.onEvent(QuestionBankUiEvent.SelectCategory(id)) },
                    label = { Text(label) }
                )
            }
        }

        when (val state = uiState) {
            is QuestionBankUiState.Loading -> LoadingPanel("Loading question bank…")
            is QuestionBankUiState.Error -> AivanceEmptyState(
                title = "Couldn't load questions",
                description = state.message,
                icon = Icons.Rounded.ErrorOutline,
                primaryActionText = "Try Again",
                onPrimaryAction = { viewModel.onEvent(QuestionBankUiEvent.Retry) }
            )
            is QuestionBankUiState.Content -> {
                if (state.questions.isEmpty()) {
                    AivanceEmptyState(
                        title = "No questions yet",
                        description = "Practice sessions and saved questions will appear here.",
                        icon = Icons.Rounded.MenuBook,
                        compact = true
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.questions) { question ->
                            QuestionBankCard(
                                question = question,
                                isFavorite = question.isFavorite,
                                isIdealAnswerLoading = state.isIdealAnswerLoading && state.idealAnswerFor == question.id,
                                idealAnswer = if (state.idealAnswerFor == question.id) state.idealAnswer else null,
                                idealAnswerError = if (state.idealAnswerFor == question.id) state.idealAnswerError else null,
                                onToggleFavorite = { viewModel.onEvent(QuestionBankUiEvent.ToggleFavorite(question.id)) },
                                onViewIdealAnswer = { viewModel.onEvent(QuestionBankUiEvent.ViewIdealAnswer(question.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionBankCard(
    question: com.bangersoul.aivance.core.common.model.InterviewQuestion,
    isFavorite: Boolean,
    isIdealAnswerLoading: Boolean,
    idealAnswer: String?,
    idealAnswerError: String?,
    onToggleFavorite: () -> Unit,
    onViewIdealAnswer: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(question.text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusChip(text = question.category.replace('_', ' '), tone = BannerTone.INFO)
                        StatusChip(
                            text = question.difficulty.replace('_', ' '),
                            tone = if (question.difficulty == "HARD") BannerTone.ERROR else BannerTone.INFO
                        )
                    }
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        if (isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                        contentDescription = if (isFavorite) "Remove favorite" else "Mark favorite",
                        tint = if (isFavorite) AivanceTheme.colors.accent else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isIdealAnswerLoading) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Text("Generating ideal answer…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else if (idealAnswer != null) {
                InsightCard(text = idealAnswer, icon = Icons.Rounded.Lightbulb)
            } else if (idealAnswerError != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    Text(
                        idealAnswerError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                TextButton(onClick = onViewIdealAnswer) {
                    Icon(Icons.Rounded.AutoAwesome, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("View Ideal Answer")
                }
            }
        }
    }
}

@Composable
private fun LearnTab(viewModel: LearningHubViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var targetRole by remember { mutableStateOf("") }

    when (val state = uiState) {
        is LearningHubUiState.Idle, is LearningHubUiState.Loading -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Personalized Learning", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Tell us your target role and we'll recommend skills to master and resources to get you there.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetRole,
                    onValueChange = { targetRole = it },
                    label = { Text("Target role") },
                    placeholder = { Text("e.g. Senior Android Engineer") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                AivancePrimaryButton(
                    text = if (uiState is LearningHubUiState.Loading) "Generating…" else "Generate Recommendations",
                    onClick = {
                        viewModel.onEvent(
                            com.bangersoul.aivance.feature.profile.LearningHubUiEvent.GetRecommendations(
                                currentSkills = "",
                                targetRole = targetRole
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = targetRole.isNotBlank() && uiState !is LearningHubUiState.Loading
                )
            }
        }
        is LearningHubUiState.Error -> AivanceEmptyState(
            title = "Couldn't load resources",
            description = state.message,
            icon = Icons.Rounded.ErrorOutline,
            primaryActionText = "Try Again",
            onPrimaryAction = { viewModel.onEvent(com.bangersoul.aivance.feature.profile.LearningHubUiEvent.Reset) }
        )
        is LearningHubUiState.Success -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.recommendedSkills.isNotEmpty()) {
                    item {
                        Text("Recommended Skills", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(state.recommendedSkills) { skill ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Rounded.School, null, tint = AivanceTheme.colors.accent, modifier = Modifier.size(18.dp))
                                Text(skill, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("Resources", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                if (state.suggestedResources.isEmpty()) {
                    item {
                        AivanceEmptyState(
                            title = "No resources yet",
                            description = "Complete more of your profile to unlock personalized learning paths.",
                            icon = Icons.Rounded.MenuBook,
                            compact = true
                        )
                    }
                } else {
                    items(state.suggestedResources) { resource ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Rounded.MenuBook, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Column {
                                    Text(resource.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Text(resource.type.name.replace('_', ' '), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
        else -> LoadingPanel("Loading resources…")
    }
}
