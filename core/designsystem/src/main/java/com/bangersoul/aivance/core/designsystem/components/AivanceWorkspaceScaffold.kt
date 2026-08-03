package com.bangersoul.aivance.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme

/**
 * A standardized scaffold for all Hub and Spoke screens within a Workspace.
 * Automatically integrates with the Career Intelligence Orchestrator signals.
 */
@Composable
fun AivanceWorkspaceScaffold(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    isLoading: Boolean = false,
    error: String? = null,
    isEmpty: Boolean = false,
    emptyTitle: String? = null,
    emptyDescription: String? = null,
    onRetry: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    showAssistantAction: Boolean = true,
    onAssistantClick: () -> Unit = {},
    topBarActions: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AivanceTopBar(
                title = title,
                subtitle = subtitle,
                onBack = onBack,
                actions = {
                    topBarActions()
                    if (showAssistantAction) {
                        IconButton(onClick = onAssistantClick) {
                            Icon(
                                Icons.Rounded.AutoAwesome,
                                contentDescription = "AI Assistant",
                                tint = AivanceTheme.colors.accent
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = floatingActionButton,
        snackbarHost = snackbarHost,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isEmpty && emptyTitle != null && emptyDescription != null -> {
                    AivanceEmptyState(
                        title = emptyTitle,
                        description = emptyDescription
                    )
                }
                else -> content()
            }

            if (isLoading) {
                AivanceLoading()
            }

            if (error != null) {
                AivanceError(message = error, onRetry = onRetry)
            }
        }
    }
}
