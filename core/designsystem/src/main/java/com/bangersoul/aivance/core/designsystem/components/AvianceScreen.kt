package com.bangersoul.aivance.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.ui.unit.dp

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import com.bangersoul.aivance.core.designsystem.theme.AvianceTheme

@Composable
fun AvianceScreen(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    contentWindowInsets: WindowInsets = WindowInsets(0, 0, 0, 0),
    isLoading: Boolean = false,
    error: String? = null,
    onRetry: () -> Unit = {},
    isEmpty: Boolean = false,
    emptyTitle: String = "No data",
    emptyDescription: String = "Check back later",
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        contentWindowInsets = contentWindowInsets
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when {
                isLoading -> AvianceLoading()
                error != null -> AvianceError(message = error, onRetry = onRetry)
                isEmpty -> EmptyStateUI(title = emptyTitle, description = emptyDescription)
                else -> content(PaddingValues(0.dp))
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun AvianceScreenContentPreview() {
    AvianceTheme(darkTheme = true) {
        AvianceScreen {
            Text(
                text = "Hello, Aviance!",
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun AvianceScreenLoadingPreview() {
    AvianceTheme(darkTheme = true) {
        AvianceScreen(isLoading = true) {}
    }
}
