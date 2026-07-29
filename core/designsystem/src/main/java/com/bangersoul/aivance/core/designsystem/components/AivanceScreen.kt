package com.bangersoul.aivance.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme

@Composable
fun AivanceScreen(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    error: String? = null,
    isEmpty: Boolean = false,
    emptyTitle: String? = null,
    emptyDescription: String? = null,
    onRetry: () -> Unit = {},
    contentWindowInsets: WindowInsets = WindowInsets(0, 0, 0, 0),
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        contentWindowInsets = contentWindowInsets
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

@Preview(showBackground = true)
@Composable
private fun AivanceScreenContentPreview() {
    AivanceTheme(darkTheme = true) {
        AivanceScreen {
            Text(
                text = "Hello, Aivance!",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AivanceScreenLoadingPreview() {
    AivanceTheme(darkTheme = true) {
        AivanceScreen(isLoading = true) {}
    }
}
