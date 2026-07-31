package com.bangersoul.aivance.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme
import kotlinx.coroutines.launch

/**
 * Values that the shell provides to its content — used to show
 * global snackbars, dialogs, or loading overlays.
 */
data class AppShellState(
    val showSnackbar: (String) -> Unit = {},
    val showDialog: (String, String, String) -> Unit = { _, _, _ -> },
    val dismissDialog: () -> Unit = {}
)

val LocalAppShellState = compositionLocalOf { AppShellState() }

/**
 * Global application shell wrapping the navigation graph.
 *
 * Responsibilities:
 * - Theme application (AivanceTheme)
 * - Global snackbar host
 * - Global dialog host
 * - Scaffold edge-to-edge management
 */
@Composable
fun AivanceAppShell(
    content: @Composable () -> Unit
) {
    val themeViewModel: AppThemeViewModel = hiltViewModel()
    val themeState by themeViewModel.themeState.collectAsStateWithLifecycle()

    AivanceTheme(
        themeMode = themeState.themeMode,
        accentSeed = themeState.accentSeed,
        dynamicColor = themeState.dynamicColor
    ) {
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        var dialogState by remember { mutableStateOf<DialogState?>(null) }

        val shellState = remember {
            AppShellState(
                showSnackbar = { message ->
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(
                            message = message,
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                showDialog = { title, message, confirmLabel ->
                    dialogState = DialogState(
                        title = title,
                        message = message,
                        confirmLabel = confirmLabel
                    )
                },
                dismissDialog = { dialogState = null }
            )
        }

        CompositionLocalProvider(LocalAppShellState provides shellState) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = androidx.compose.material3.MaterialTheme.colorScheme.background
            ) {
                Scaffold(
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState) { data ->
                            Snackbar(
                                snackbarData = data,
                                containerColor = com.bangersoul.aivance.core.designsystem.theme.DarkSurface,
                                contentColor = com.bangersoul.aivance.core.designsystem.theme.DarkPrimary
                            )
                        }
                    },
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onBackground
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        content()

                        // Global overlay dialog
                        dialogState?.let { state ->
                            androidx.compose.material3.AlertDialog(
                                onDismissRequest = { dialogState = null },
                                title = { androidx.compose.material3.Text(state.title) },
                                text = { androidx.compose.material3.Text(state.message) },
                                confirmButton = {
                                    androidx.compose.material3.TextButton(onClick = { dialogState = null }) {
                                        androidx.compose.material3.Text(state.confirmLabel)
                                    }
                                },
                                dismissButton = {
                                    androidx.compose.material3.TextButton(onClick = { dialogState = null }) {
                                        androidx.compose.material3.Text("Cancel")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class DialogState(
    val title: String,
    val message: String,
    val confirmLabel: String
)
