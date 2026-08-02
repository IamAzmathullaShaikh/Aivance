package com.bangersoul.aivance.feature.jobs

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.PersonSearch
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.designsystem.components.ActionButton
import com.bangersoul.aivance.core.designsystem.components.AivanceScreen
import com.bangersoul.aivance.core.designsystem.components.DashboardCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailsScreen(
    viewModel: JobDetailsViewModel,
    jobId: String,
    onNavigateBack: () -> Unit,
    onNavigateToRecruiters: (String) -> Unit = {},
    onNavigateToCoverLetter: () -> Unit = {},
    onNavigateToPipeline: () -> Unit = {},
    onNavigateToAts: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // The custom back stack passes the destination's job ID here directly (it does
    // not populate SavedStateHandle), so drive the load from the destination arg.
    LaunchedEffect(jobId) {
        viewModel.load(jobId)
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is JobDetailsUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is JobDetailsUiEffect.OpenExternalUrl -> openUrl(context, effect.url)
                is JobDetailsUiEffect.NavigateToRecruiters -> onNavigateToRecruiters(effect.jobId)
                JobDetailsUiEffect.NavigateToCoverLetter -> onNavigateToCoverLetter()
                JobDetailsUiEffect.NavigateToPipeline -> onNavigateToPipeline()
            }
        }
    }

    AivanceScreen(
        topBar = {
            TopAppBar(
                title = { Text("Job Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val isBookmarked = (uiState as? JobDetailsUiState.Success)?.isBookmarked ?: false
                    IconButton(onClick = { viewModel.onEvent(JobDetailsUiEvent.ToggleBookmark) }) {
                        Icon(
                            if (isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                            contentDescription = "Bookmark"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        isLoading = uiState is JobDetailsUiState.Loading,
        error = (uiState as? JobDetailsUiState.Error)?.message,
        onRetry = { viewModel.onEvent(JobDetailsUiEvent.Reload) }
    ) {
        Box(Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "JobDetailsTransition"
            ) { state ->
                when (state) {
                    is JobDetailsUiState.Success -> JobDetailsContent(
                        job = state.job,
                        onApplyClick = { viewModel.onEvent(JobDetailsUiEvent.OpenUrl) },
                        onApplyAndTrack = { viewModel.onEvent(JobDetailsUiEvent.ApplyAndTrack) },
                        onFindRecruiters = { viewModel.onEvent(JobDetailsUiEvent.FindRecruiters) },
                        onGenerateCoverLetter = { viewModel.onEvent(JobDetailsUiEvent.GenerateCoverLetter) },
                        onOpenAts = onNavigateToAts
                    )
                    else -> {}
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

private fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    }
}

@Composable
private fun JobDetailsContent(
    job: JobListing,
    onApplyClick: () -> Unit,
    onApplyAndTrack: () -> Unit,
    onFindRecruiters: () -> Unit,
    onGenerateCoverLetter: () -> Unit,
    onOpenAts: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(job.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Business, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(job.company, style = MaterialTheme.typography.titleMedium)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.LocationOn, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(8.dp))
                Text(job.location, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
            }
        }

        // Primary actions
        ActionButton(
            text = "Apply on Provider Site",
            onClick = onApplyClick,
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Rounded.Public
        )
        ActionButton(
            text = "Apply & Track in Pipeline",
            onClick = onApplyAndTrack,
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Rounded.PlaylistAdd,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            ActionButton(
                text = "Find Recruiters",
                onClick = onFindRecruiters,
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.PersonSearch,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ActionButton(
                text = "Cover Letter",
                onClick = onGenerateCoverLetter,
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.HistoryEdu,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ATS Match Banner
        DashboardCard(modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("AI Compatibility Check", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text("Check how well your resume matches this job.", style = MaterialTheme.typography.bodySmall)
                }
                ActionButton(text = "Check", onClick = onOpenAts)
            }
        }

        // Description
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Job Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(job.description, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(80.dp))
    }
}
