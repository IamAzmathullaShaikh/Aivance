package com.bangersoul.aivance.feature.interview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ClearAll
import androidx.compose.material.icons.rounded.Summarize
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bangersoul.aivance.core.common.enums.MessageRole
import com.bangersoul.aivance.core.designsystem.components.AivanceScreen
import com.bangersoul.aivance.core.designsystem.components.ChatBubble
import com.bangersoul.aivance.core.designsystem.components.ChatBubbleRole
import com.bangersoul.aivance.core.designsystem.components.TypingIndicator
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme
import com.bangersoul.aivance.core.designsystem.theme.DarkAccent
import com.bangersoul.aivance.core.designsystem.theme.Zinc800
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    viewModel: AiChatViewModel,
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(
        (uiState as? AiChatUiState.Chatting)?.messages?.size,
        (uiState as? AiChatUiState.Chatting)?.isTyping
    ) {
        val messages = (uiState as? AiChatUiState.Chatting)?.messages
        if (messages != null && messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size + 1)
        }
    }

    AivanceScreen(
        topBar = {
            TopAppBar(
                title = {
                    Text("AI Assistant", fontWeight = FontWeight.Bold)
                },
                actions = {
                    when (uiState) {
                        is AiChatUiState.Chatting -> {
                            IconButton(onClick = { viewModel.onEvent(AiChatUiEvent.StartNewChat) }) {
                                Icon(Icons.Rounded.Add, contentDescription = "New Chat")
                            }
                            IconButton(onClick = { viewModel.onEvent(AiChatUiEvent.ClearConversation) }) {
                                Icon(Icons.Rounded.ClearAll, contentDescription = "Clear")
                            }
                        }
                        is AiChatUiState.Idle -> {
                            IconButton(onClick = { viewModel.onEvent(AiChatUiEvent.StartNewChat) }) {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = "Start Chat")
                            }
                        }
                        else -> {}
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) {
        when (uiState) {
            is AiChatUiState.Idle, is AiChatUiState.Initializing -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.padding(bottom = 16.dp),
                        tint = DarkAccent
                    )
                    Text(
                        text = "Start a conversation with AI",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ask career questions, get advice, or brainstorm ideas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            is AiChatUiState.Chatting -> {
                val chatting = uiState as AiChatUiState.Chatting
                Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(chatting.messages, key = { it.id.ifEmpty { it.timestamp.toString() } }) { msg ->
                            ChatBubble(
                                text = msg.content,
                                timestamp = SimpleDateFormat("HH:mm", Locale.getDefault())
                                    .format(Date(msg.timestamp)),
                                role = if (msg.role == MessageRole.ASSISTANT || msg.role == MessageRole.SYSTEM)
                                    ChatBubbleRole.Interviewer else ChatBubbleRole.Candidate
                            )
                        }
                        if (chatting.isTyping) {
                            item { TypingIndicator() }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Type a message...") },
                            maxLines = 4,
                            shape = MaterialTheme.shapes.large,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkAccent,
                                unfocusedBorderColor = Zinc800
                            )
                        )
                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    viewModel.onEvent(AiChatUiEvent.SendMessage(inputText))
                                    inputText = ""
                                }
                            },
                            enabled = inputText.isNotBlank()
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.Send,
                                contentDescription = "Send",
                                tint = if (inputText.isNotBlank()) DarkAccent
                                else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
            is AiChatUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        (uiState as AiChatUiState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            else -> {}
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AiChatScreenPreview() {
    AivanceTheme(darkTheme = true) {
        AiChatScreen(
            viewModel = androidx.lifecycle.viewmodel.compose.viewModel()
        )
    }
}
