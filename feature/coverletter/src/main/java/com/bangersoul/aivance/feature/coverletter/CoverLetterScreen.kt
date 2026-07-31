package com.bangersoul.aivance.feature.coverletter

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import com.bangersoul.aivance.core.common.model.CoverLetterVersion
import com.bangersoul.aivance.core.designsystem.components.ActionButton
import com.bangersoul.aivance.core.designsystem.components.AivanceScreen
import com.bangersoul.aivance.core.designsystem.components.DashboardCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoverLetterScreen(
    viewModel: CoverLetterViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onEvent(CoverLetterUiEvent.Load)
    }

    AivanceScreen(
        topBar = {
            TopAppBar(
                title = { Text("Cover Letter Intelligence", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        isLoading = uiState is CoverLetterUiState.Loading,
        error = (uiState as? CoverLetterUiState.Error)?.message
    ) {
        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "CoverLetterTransition"
        ) { state ->
            when (state) {
                CoverLetterUiState.Idle -> CoverLetterEmptyContent()
                is CoverLetterUiState.Success -> CoverLetterEditorContent(
                    version = state.selectedVersion ?: state.coverLetter?.versions?.firstOrNull(),
                    isGenerating = state.isGenerating,
                    onRegenerate = { viewModel.onEvent(CoverLetterUiEvent.RegenerateSection(it, "BODY")) }
                )
                else -> {}
            }
        }
    }
}

@Composable
private fun CoverLetterEmptyContent() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Rounded.Description, null, Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        Spacer(Modifier.height(24.dp))
        Text("No Cover Letter Yet", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Start an application from the Jobs tab to generate a tailored cover letter.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun CoverLetterEditorContent(
    version: CoverLetterVersion?,
    isGenerating: Boolean,
    onRegenerate: (Long) -> Unit
) {
    if (version == null) return

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(version.versionName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = { /* TODO: Copy all */ }) {
                Icon(Icons.Rounded.ContentCopy, null)
            }
        }

        Spacer(Modifier.height(16.dp))

        if (isGenerating) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(version.sections) { section ->
                SectionCard(
                    title = section.title,
                    content = section.content,
                    onRegenerate = { onRegenerate(version.id) }
                )
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: String,
    onRegenerate: () -> Unit
) {
    DashboardCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Row {
                    IconButton(onClick = { /* TODO: Edit */ }) {
                        Icon(Icons.Rounded.Edit, null, Modifier.size(18.dp))
                    }
                    IconButton(onClick = onRegenerate) {
                        Icon(Icons.Rounded.AutoAwesome, null, Modifier.size(18.dp))
                    }
                }
            }
            Text(content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
