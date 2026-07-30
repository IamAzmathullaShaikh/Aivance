package com.bangersoul.aivance.feature.jobs

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.designsystem.components.ActionButton
import com.bangersoul.aivance.core.designsystem.components.AivanceScreen
import com.bangersoul.aivance.core.designsystem.components.DashboardCard
import com.bangersoul.aivance.core.designsystem.components.MetricChip
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobsScreen(
    viewModel: JobsViewModel,
    onNavigateToTracker: () -> Unit,
    onNavigateToJobDetails: (String) -> Unit = {}
) {
    val query by viewModel.query.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    AivanceScreen(
        topBar = {
            TopAppBar(
                title = { Text("Job Search", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        isLoading = uiState is JobsUiState.Loading,
        error = (uiState as? JobsUiState.Error)?.message,
        onRetry = { viewModel.onEvent(JobsUiEvent.Retry) },
        isEmpty = (uiState as? JobsUiState.Success)?.jobs?.isEmpty() == true,
        emptyTitle = "No jobs found",
        emptyDescription = "Try adjusting your search or filters."
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AivanceTheme.spacing.medium)
        ) {
            // Search Bar
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.onQueryChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search jobs, companies...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                },
                shape = AivanceTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(AivanceTheme.spacing.medium))

            // Filters
            Row(
                horizontalArrangement = Arrangement.spacedBy(AivanceTheme.spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.FilterList,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )

                listOf("Remote", "Full-time").forEach { filter ->
                    val selected = filter.lowercase() == "remote" && (uiState as? JobsUiState.Success)?.isRemoteOnly == true
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.toggleFilter(filter) },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                            labelColor = MaterialTheme.colorScheme.secondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(AivanceTheme.spacing.medium))

            // Job Results
            if (uiState is JobsUiState.Success) {
                val jobs = (uiState as JobsUiState.Success).jobs
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(AivanceTheme.spacing.medium),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(jobs, key = { it.id }) { job ->
                        JobItem(
                            job = job,
                            onApplyClick = { onNavigateToJobDetails(job.id) },
                            onTrackClick = {
                                viewModel.addJobToTracker(job)
                                onNavigateToTracker()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JobItem(
    job: JobListing,
    onApplyClick: () -> Unit,
    onTrackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DashboardCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(AivanceTheme.spacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = job.company,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                job.salaryRange?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(AivanceTheme.spacing.small))

            Row(horizontalArrangement = Arrangement.spacedBy(AivanceTheme.spacing.extraSmall)) {
                MetricChip(label = job.location)
                MetricChip(label = job.employmentType.name)
            }

            Spacer(modifier = Modifier.height(AivanceTheme.spacing.medium))

            Text(
                text = job.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(AivanceTheme.spacing.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AivanceTheme.spacing.small)
            ) {
                ActionButton(
                    text = "Apply",
                    onClick = onApplyClick,
                    modifier = Modifier.weight(1f)
                )
                ActionButton(
                    text = "Track",
                    onClick = onTrackClick,
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun JobItemPreview() {
    AivanceTheme(darkTheme = true) {
        JobItem(
            job = JobListing(
                id = "1",
                company = "Google",
                title = "Android Engineer",
                location = "Mountain View, CA",
                description = "Lead the development of next-gen Android experiences.",
                url = "https://google.com/jobs/1",
                sourceProvider = "test"
            ),
            onApplyClick = {},
            onTrackClick = {}
        )
    }
}
