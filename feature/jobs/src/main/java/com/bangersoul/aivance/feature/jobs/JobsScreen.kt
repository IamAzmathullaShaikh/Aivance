package com.bangersoul.aivance.feature.jobs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bangersoul.aivance.core.common.enums.RemoteType
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.designsystem.components.*
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun JobsScreen(
    viewModel: JobsViewModel,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToSavedJobs: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        AivanceTopBar(title = "Job Discovery", subtitle = "Unified search across providers")

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.onEvent(JobsUiEvent.Search(it))
                },
                placeholder = { Text("Search roles, skills, or companies") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            viewModel.onEvent(JobsUiEvent.Search(""))
                        }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = AivanceTheme.shapes.large
            )

            Spacer(Modifier.height(12.dp))

            FilterChipsRow(
                onFilterClick = { remote ->
                    viewModel.onEvent(
                        JobsUiEvent.UpdateFilter(
                            (uiState as? JobsUiState.Success)?.filter?.copy(remoteType = remote)
                                ?: com.bangersoul.aivance.core.common.model.JobSearchFilter(remoteType = remote)
                        )
                    )
                }
            )

            Spacer(Modifier.height(12.dp))

            AnimatedContent(
                targetState = uiState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "JobsListTransition"
            ) { state ->
                when (state) {
                    is JobsUiState.Loading -> SkeletonList(itemCount = 6, showAvatar = true)
                    is JobsUiState.Error -> AivanceError(
                        message = state.message,
                        onRetry = { viewModel.onEvent(JobsUiEvent.Refresh) },
                        title = "Jobs unavailable"
                    )
                    is JobsUiState.Success -> JobDiscoveryList(
                        jobs = state.jobs,
                        isSearching = state.isSearching,
                        onJobClick = onNavigateToDetails,
                        onBookmarkClick = { viewModel.onEvent(JobsUiEvent.ToggleBookmark(it)) },
                        onRefresh = { viewModel.onEvent(JobsUiEvent.Refresh) },
                        onSavedJobs = onNavigateToSavedJobs
                    )
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun FilterChipsRow(onFilterClick: (RemoteType) -> Unit) {
    val options = listOf(
        RemoteType.ON_SITE to "On-site",
        RemoteType.HYBRID to "Hybrid",
        RemoteType.REMOTE to "Remote"
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(options) { (type, label) ->
            FilterChip(
                selected = false,
                onClick = { onFilterClick(type) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun JobDiscoveryList(
    jobs: List<JobListing>,
    isSearching: Boolean,
    onJobClick: (String) -> Unit,
    onBookmarkClick: (String) -> Unit,
    onRefresh: () -> Unit,
    onSavedJobs: () -> Unit
) {
    if (jobs.isEmpty()) {
        AivanceEmptyState(
            title = if (isSearching) "No matches found" else "No jobs yet",
            description = if (isSearching) {
                "Try a different keyword or filter — or refresh to pull the latest roles."
            } else {
                "Configure a job provider to start discovering roles matched to your profile."
            },
            icon = Icons.Rounded.WorkOff,
            primaryActionText = "Refresh",
            onPrimaryAction = onRefresh,
            secondaryActionText = "Saved Jobs",
            onSecondaryAction = onSavedJobs
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(jobs, key = { it.id }) { job ->
                JobDiscoveryCard(
                    job = job,
                    onClick = { onJobClick(job.id) },
                    onBookmarkClick = { onBookmarkClick(job.id) }
                )
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun JobDiscoveryCard(
    job: JobListing,
    onClick: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    DashboardCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = AivanceTheme.shapes.medium,
                color = AivanceTheme.colors.accent.copy(alpha = 0.12f),
                modifier = Modifier.size(44.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(
                        Icons.Rounded.Business,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = AivanceTheme.colors.accent
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    job.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetaText(job.company)
                    if (job.location.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(Icons.Rounded.LocationOn, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
                            MetaText(job.location)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (job.remoteType == RemoteType.REMOTE || job.isRemote) {
                        StatusChip(text = "Remote", tone = BannerTone.INFO)
                    }
                    job.salaryRange?.let {
                        StatusChip(text = it, tone = BannerTone.SUCCESS)
                    }
                    if (job.matchScore != null) {
                        StatusChip(text = "ATS Match ${job.matchScore}%", tone = BannerTone.WARNING)
                    }
                }
            }

            IconButton(onClick = onBookmarkClick) {
                Icon(
                    Icons.Rounded.BookmarkBorder,
                    contentDescription = "Save job",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MetaText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
