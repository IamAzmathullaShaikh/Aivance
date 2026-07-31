package com.bangersoul.aivance.feature.assistant

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bangersoul.aivance.core.designsystem.components.*
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme

/**
 * The AI Career Assistant — an operating-system style command surface.
 */
@Composable
fun AssistantScreen(
    viewModel: AssistantViewModel,
    onSwitchProvider: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        AivanceTopBar(title = "Assistant", subtitle = "Your AI career copilot")

        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "AssistantTransition"
            ) { state ->
                when (state) {
                    is AssistantUiState.Idle -> AssistantWelcomeContent(onPromptClick = { prompt ->
                        viewModel.sendMessage(prompt)
                    })
                    is AssistantUiState.Loading -> SkeletonDashboard(modifier = Modifier.fillMaxSize())
                    is AssistantUiState.Chatting -> ChatContent(
                        messages = state.messages,
                        isTyping = state.isTyping
                    )
                    is AssistantUiState.Error -> AivanceError(
                        message = state.message,
                        onRetry = { viewModel.retry() },
                        title = "Provider unavailable",
                        detail = "The AI provider could not be reached. Check your provider configuration or try again.",
                        primaryActionText = "Retry",
                        secondaryActionText = "Switch Provider",
                        onSecondaryAction = onSwitchProvider
                    )
                    else -> {}
                }
            }
        }

        AssistantInputBar(
            value = inputText,
            onValueChange = { inputText = it },
            onSend = {
                viewModel.sendMessage(it)
                inputText = ""
            }
        )
    }
}

private val suggestedPrompts = listOf(
    "Optimize my resume for a Senior Android Engineer role",
    "Write a follow-up email after an interview",
    "Generate a cold outreach message to a recruiter",
    "What should I improve to raise my ATS score?",
    "Draft a career roadmap for the next 6 months",
    "Prepare me for a behavioral interview question"
)

@Composable
private fun AssistantWelcomeContent(onPromptClick: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = AivanceTheme.shapes.extraLarge,
                    color = AivanceTheme.colors.accent.copy(alpha = 0.14f),
                    modifier = Modifier.size(72.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(
                            Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = AivanceTheme.colors.accent
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "Your AI Career Assistant",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Optimize resumes, find recruiters, prepare for interviews — all in one command surface.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(0.85f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // Provider status
        item {
            InsightCard(
                text = "AI Provider: Ready",
                icon = Icons.Rounded.CloudDone,
                iconTint = AivanceTheme.colors.success,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            SectionHeader(title = "Try a prompt")
        }

        items(suggestedPrompts.chunked(2)) { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEach { prompt ->
                    Surface(
                        onClick = { onPromptClick(prompt) },
                        shape = AivanceTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            prompt,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(14.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        item {
            SectionHeader(title = "Quick Commands")
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    QuickCommandChip("Resume Scan", Icons.Rounded.Description) { onPromptClick("Analyze my resume for ATS gaps") }
                }
                item {
                    QuickCommandChip("Job Match", Icons.Rounded.WorkOutline) { onPromptClick("Find jobs matching my profile") }
                }
                item {
                    QuickCommandChip("Mock Interview", Icons.Rounded.RecordVoiceOver) { onPromptClick("Start a mock interview") }
                }
                item {
                    QuickCommandChip("Cover Letter", Icons.Rounded.Edit) { onPromptClick("Draft a cover letter for my target role") }
                }
            }
        }
    }
}

@Composable
private fun QuickCommandChip(label: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = AivanceTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = AivanceTheme.colors.accent)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun ChatContent(messages: List<AssistantChatMessage>, isTyping: Boolean) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(messages) { msg ->
            AssistantBubble(msg)
        }
        if (isTyping) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TypingIndicator()
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Assistant is thinking",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AssistantBubble(msg: AssistantChatMessage) {
    val isUser = msg.role == "USER"
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = if (isUser) {
                androidx.compose.foundation.shape.RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
            } else {
                androidx.compose.foundation.shape.RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)
            },
            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Text(
                msg.content,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = if (isUser) "You" else "Assistant",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 6.dp)
        )
    }
}

@Composable
private fun AssistantInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: (String) -> Unit
) {
    Surface(tonalElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask anything about your career...") },
                shape = AivanceTheme.shapes.large,
                maxLines = 4,
                trailingIcon = {
                    AnimatedVisibility(
                        visible = value.isNotBlank(),
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        IconButton(onClick = { onSend(value) }) {
                            Icon(
                                Icons.AutoMirrored.Rounded.Send,
                                contentDescription = "Send",
                                tint = AivanceTheme.colors.accent
                            )
                        }
                    }
                }
            )
        }
    }
}
