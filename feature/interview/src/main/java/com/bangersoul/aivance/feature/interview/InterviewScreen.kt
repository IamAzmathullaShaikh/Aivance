package com.bangersoul.aivance.feature.interview

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bangersoul.aivance.core.designsystem.components.AivanceScreen
import com.bangersoul.aivance.core.designsystem.components.ChatBubble
import com.bangersoul.aivance.core.designsystem.components.ChatBubbleRole
import com.bangersoul.aivance.core.designsystem.components.DashboardCard
import com.bangersoul.aivance.core.designsystem.components.TypingIndicator
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme
import com.bangersoul.aivance.feature.interview.domain.InterviewFeedback
import com.bangersoul.aivance.feature.interview.domain.InterviewMessage
import com.bangersoul.aivance.feature.interview.domain.MessageRole
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                title = { Text(text = "Interview AI") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState is InterviewUiState.Chatting) {
                        val sid = (uiState as InterviewUiState.Chatting).sessionId
                        TextButton(onClick = { viewModel.onEvent(InterviewUiEvent.EndSession(sid)) }) {
                            Text(text = "End Session", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) {
        AnimatedContent(
            targetState = uiState,
            transitionSpec = {
                fadeIn().togetherWith(fadeOut())
            },
            label = "InterviewContent"
        ) { state ->
            when (state) {
                is InterviewUiState.Idle -> {
                    SetupView(
                        onStart = { role, difficulty ->
                            val diff = when (difficulty) {
                                "Senior" -> com.bangersoul.aivance.core.common.enums.InterviewDifficulty.HARD
                                "Junior" -> com.bangersoul.aivance.core.common.enums.InterviewDifficulty.EASY
                                else -> com.bangersoul.aivance.core.common.enums.InterviewDifficulty.MEDIUM
                            }
                            viewModel.onEvent(InterviewUiEvent.StartSession(role, diff))
                        }
                    )
                }

                is InterviewUiState.Preparing -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Preparing your interview...")
                    }
                }

                is InterviewUiState.Ready -> {
                    // Auto-transition — call onEvent to start chatting
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        viewModel.onEvent(InterviewUiEvent.GenerateQuestions)
                    }
                }

                is InterviewUiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Loading...")
                    }
                }

                is InterviewUiState.Chatting -> {
                    ChatView(
                        messages = state.messages,
                        isTyping = state.isTyping,
                        onSendMessage = { text -> viewModel.onEvent(InterviewUiEvent.SendMessage(text)) }
                    )
                }

                is InterviewUiState.GeneratingFeedback -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Analyzing your performance...")
                    }
                }

                is InterviewUiState.Feedback -> {
                    FeedbackView(
                        feedback = state.feedback,
                        onRestart = { viewModel.onEvent(InterviewUiEvent.Reset) }
                    )
                }

                is InterviewUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun SetupView(onStart: (String, String) -> Unit) {
    var role by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf("Mid-Level") }
    val difficulties = listOf("Junior", "Mid-Level", "Senior")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ready to practice?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tell us what role you're interviewing for.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = role,
            onValueChange = { role = it },
            label = { Text("Job Role (e.g. Android Developer)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Select Difficulty",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            difficulties.forEach { difficulty ->
                FilterChip(
                    selected = selectedDifficulty == difficulty,
                    onClick = { selectedDifficulty = difficulty },
                    label = { Text(difficulty) }
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = { onStart(role, selectedDifficulty) },
            modifier = Modifier.fillMaxWidth(),
            enabled = role.isNotBlank(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(text = "Start Interview", modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
fun ChatView(
    messages: List<InterviewMessage>,
    isTyping: Boolean,
    onSendMessage: (String) -> Unit
) {
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(messages.size, isTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size + if (isTyping) 1 else 0)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                ChatBubble(
                    text = message.text,
                    timestamp = formatTimestamp(message.timestamp),
                    role = if (message.role == MessageRole.AI) ChatBubbleRole.Interviewer else ChatBubbleRole.Candidate
                )
            }

            if (isTyping) {
                item {
                    TypingIndicator()
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type your answer...") },
                maxLines = 4,
                shape = MaterialTheme.shapes.large
            )
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onSendMessage(inputText)
                        inputText = ""
                    }
                },
                enabled = inputText.isNotBlank()
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.Send,
                    contentDescription = "Send",
                    tint = if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun FeedbackView(
    feedback: InterviewFeedback,
    onRestart: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Interview Summary",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Here's how you did",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            DashboardCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Overall Performance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = feedback.summary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Strengths", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    feedback.strengths.forEach { strength ->
                        DashboardCard(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(
                                text = "• $strength",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Areas to Improve", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    feedback.weaknesses.forEach { weakness ->
                        DashboardCard(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(
                                text = "• $weakness",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        item {
            DashboardCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Pro Tips",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    feedback.tips.forEach { tip ->
                        Text(
                            text = "💡 $tip",
                            modifier = Modifier.padding(vertical = 4.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(text = "Practice Again", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Preview(showBackground = true, device = "id:pixel_7")
@Composable
private fun SetupViewPreview() {
    AivanceTheme(darkTheme = true) {
        SetupView(onStart = { _, _ -> })
    }
}

@Preview(showBackground = true, device = "id:pixel_7")
@Composable
private fun ChatViewPreview() {
    AivanceTheme(darkTheme = true) {
        ChatView(
            messages = listOf(
                InterviewMessage(role = MessageRole.AI, text = "Hello! Tell me about yourself."),
                InterviewMessage(role = MessageRole.User, text = "I am an Android developer with 5 years of experience.")
            ),
            isTyping = true,
            onSendMessage = {}
        )
    }
}

@Preview(showBackground = true, device = "id:pixel_7")
@Composable
private fun FeedbackViewPreview() {
    AivanceTheme(darkTheme = true) {
        FeedbackView(
            feedback = InterviewFeedback(
                summary = "You demonstrated strong technical knowledge and clear communication.",
                strengths = listOf("Deep understanding of Jetpack Compose", "Strong architectural skills"),
                weaknesses = listOf("Could be more concise in some answers"),
                tips = listOf("Practice more on system design questions", "Use the STAR method for behavioral questions")
            ),
            onRestart = {}
        )
    }
}
