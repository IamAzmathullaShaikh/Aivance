package com.bangersoul.aivance.feature.jobs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.designsystem.components.*
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme

@Composable
fun JobComparisonScreen(
    jobs: List<JobListing>,
    onBack: () -> Unit
) {
    AivanceWorkspaceScaffold(
        title = "Job Comparison",
        subtitle = "${jobs.size} jobs selected",
        onBack = onBack
    ) {
        if (jobs.isEmpty()) {
            AivanceEmptyState(
                title = "No jobs to compare",
                description = "Select jobs from the search results to see them side-by-side.",
                onPrimaryAction = onBack,
                primaryActionText = "Go back"
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    ComparisonHeader(jobs)
                }

                item {
                    ComparisonRow(
                        title = "Salary Range",
                        values = jobs.map { it.salaryRange ?: "Not specified" }
                    )
                }

                item {
                    ComparisonRow(
                        title = "Match Score",
                        values = jobs.map { "${it.matchScore ?: 0}%" }
                    )
                }

                item {
                    ComparisonRow(
                        title = "Workplace",
                        values = jobs.map { it.remoteType.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
                    )
                }

                item {
                    ComparisonRow(
                        title = "Employment",
                        values = jobs.map { it.employmentType.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
                    )
                }
            }
        }
    }
}

@Composable
private fun ComparisonHeader(jobs: List<JobListing>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        jobs.forEach { job ->
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(job.title, fontWeight = FontWeight.Bold, maxLines = 2, style = MaterialTheme.typography.titleSmall)
                    Text(job.company, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}

@Composable
private fun ComparisonRow(
    title: String,
    values: List<String>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            values.forEach { value ->
                AivanceWorkspaceCard(modifier = Modifier.weight(1f)) {
                    Box(Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun AivanceWorkspaceCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = AivanceTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        content()
    }
}
