package com.bangersoul.aivance.feature.assistant

import android.app.Activity
import android.content.Intent
import android.provider.OpenableColumns
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bangersoul.aivance.core.designsystem.components.*
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme
import java.util.Calendar

/**
 * The AI Career Assistant — an operating-system style command surface.
 *
 * v2: personalized greeting header, intent quick-action chips, live provider
 * status bar, and an input bar with voice/document/photo affordances.
 */
@Composable
fun AssistantScreen(
    viewModel: AssistantViewModel,
    onSwitchProvider: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val providerStatus by viewModel.providerStatus.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        AssistantHeader(
            userName = userName,
            providerStatus = providerStatus,
            onSwitchProvider = onSwitchProvider
        )

        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "AssistantTransition"
            ) { state ->
                when (state) {
                    is AssistantUiState.Idle -> AssistantWelcomeContent(
                        providerReady = providerStatus.isReady,
                        onPromptClick = { prompt -> viewModel.sendMessage(prompt) },
                        onConfigureProvider = onSwitchProvider
                    )
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

private fun greetingForTime(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 0..11 -> "Good Morning"
    in 12..16 -> "Good Afternoon"
    else -> "Good Evening"
}

@Composable
private fun AssistantHeader(
    userName: String,
    providerStatus: ProviderStatusUi,
    onSwitchProvider: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${greetingForTime()}, ${userName.ifBlank { "there" }}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "What do you want to achieve today?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            ProviderStatusChip(providerStatus, onSwitchProvider)
        }
    }
}

@Composable
private fun ProviderStatusChip(status: ProviderStatusUi, onSwitchProvider: () -> Unit) {
    val (dotColor, label) = if (status.isReady) {
        AivanceTheme.colors.success to (status.providerName?.let { "$it · ${status.statusLabel}" } ?: "Active")
    } else {
        AivanceTheme.colors.warning to "No provider"
    }
    Surface(
        onClick = onSwitchProvider,
        shape = RoundedCornerShape(100.dp),
        color = dotColor.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, dotColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(dotColor, CircleShape)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = dotColor,
                maxLines = 1
            )
        }
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
private fun AssistantWelcomeContent(
    providerReady: Boolean,
    onPromptClick: (String) -> Unit,
    onConfigureProvider: () -> Unit
) {
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

        // Inline provider setup card when nothing is configured yet
        if (!providerReady) {
            item {
                ProviderSetupCard(onConfigureProvider)
            }
        }

        // Intent quick-action chips
        item {
            SectionHeader(title = "Quick Actions")
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    QuickActionChip("Improve Resume", Icons.Rounded.Description, AivanceTheme.colors.accent) {
                        onPromptClick("Analyze my resume and suggest improvements")
                    }
                }
                item {
                    QuickActionChip("Find Jobs", Icons.Rounded.WorkOutline, AivanceTheme.colors.info) {
                        onPromptClick("Find jobs matching my profile")
                    }
                }
                item {
                    QuickActionChip("Interview Prep", Icons.Rounded.RecordVoiceOver, AivanceTheme.colors.warning) {
                        onPromptClick("Start a mock interview for my target role")
                    }
                }
                item {
                    QuickActionChip("Cover Letter", Icons.Rounded.Edit, AivanceTheme.colors.success) {
                        onPromptClick("Generate a cover letter for my target role")
                    }
                }
            }
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
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
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
    }
}

@Composable
private fun ProviderSetupCard(onConfigureProvider: () -> Unit) {
    DashboardCard(onClick = onConfigureProvider, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = AivanceTheme.colors.warning.copy(alpha = 0.15f)
            ) {
                Icon(
                    Icons.Rounded.Key,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp),
                    tint = AivanceTheme.colors.warning
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "No AI provider configured",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Connect an AI provider to unlock the full assistant.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AivanceTertiaryButton(text = "Configure", onClick = onConfigureProvider)
        }
    }
}

@Composable
private fun QuickActionChip(label: String, icon: ImageVector, tint: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
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
            Surface(shape = CircleShape, color = tint.copy(alpha = 0.12f)) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.padding(6.dp).size(16.dp),
                    tint = tint
                )
            }
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
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
                RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
            } else {
                RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun AssistantInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: (String) -> Unit
) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(android.Manifest.permission.RECORD_AUDIO)

    // ── Voice input via SpeechRecognizer ────────────────────────────────────
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val results = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val transcript = results?.firstOrNull()?.takeIf { it.isNotBlank() }
            if (transcript != null) {
                onValueChange(if (value.isBlank()) transcript else "$value $transcript")
            }
        }
    }

    // ── Document / file attach ───────────────────────────────────────────────
    val documentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            // Resolve display name for context without reading file bytes
            val name = context.contentResolver.query(
                uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            } ?: "document"
            val prefix = "[Attached: $name]\n"
            onValueChange(prefix + value)
        }
    }

    // ── Photo / image attach ─────────────────────────────────────────────────
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val marker = "[Photo attached]\n"
            onValueChange(marker + value)
        }
    }

    Surface(tonalElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mic — launches Android SpeechRecognizer
            IconButton(onClick = {
                if (permissionState.status.isGranted) {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your career question…")
                        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    }
                    speechLauncher.launch(intent)
                } else {
                    permissionState.launchPermissionRequest()
                }
            }) {
                Icon(
                    Icons.Rounded.Mic,
                    contentDescription = "Voice input",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Attach — opens any document (PDF, DOCX, TXT…)
            IconButton(onClick = {
                documentLauncher.launch(
                    arrayOf(
                        "application/pdf",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "text/plain"
                    )
                )
            }) {
                Icon(
                    Icons.Rounded.AttachFile,
                    contentDescription = "Attach document",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Photo — opens system image picker
            IconButton(onClick = {
                photoLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }) {
                Icon(
                    Icons.Rounded.PhotoCamera,
                    contentDescription = "Attach photo",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

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
