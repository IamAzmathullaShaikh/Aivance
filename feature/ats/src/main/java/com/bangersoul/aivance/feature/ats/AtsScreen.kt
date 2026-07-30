package com.bangersoul.aivance.feature.ats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bangersoul.aivance.core.designsystem.components.AivanceScreen
import com.bangersoul.aivance.core.designsystem.components.DashboardCard
import com.bangersoul.aivance.core.designsystem.components.KeywordChip
import com.bangersoul.aivance.core.designsystem.components.ScoreGauge
import com.bangersoul.aivance.core.designsystem.components.SectionHeader
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme
import com.bangersoul.aivance.feature.ats.domain.AtsResult
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtsScreen(
    viewModel: AtsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val isEmpty = uiState is AtsUiState.Success &&
            (uiState as AtsUiState.Success).latestResult == null &&
            (uiState as AtsUiState.Success).history.isEmpty()

    AivanceScreen(
        topBar = {
            TopAppBar(
                title = { Text(text = "ATS Analysis", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        isLoading = uiState is AtsUiState.Loading,
        error = (uiState as? AtsUiState.Error)?.message,
        isEmpty = isEmpty,
        emptyTitle = "No Analysis Yet",
        emptyDescription = "Upload your resume to get your first ATS score and optimization tips."
    ) {
        when (val state = uiState) {
            is AtsUiState.Success -> {
                AtsContent(
                    latestResult = state.latestResult,
                    history = state.history
                )
            }
            else -> {}
        }
    }
}

@Composable
private fun AtsContent(
    latestResult: AtsResult?,
    history: List<AtsResult>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(AivanceTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(AivanceTheme.spacing.medium)
    ) {
        if (latestResult != null) {
            item {
                LatestScoreSection(latestResult)
            }

            item {
                SuggestionsSection(latestResult)
            }
        }

        if (history.isNotEmpty()) {
            item {
                SectionHeader(title = "History")
            }

            items(history) { result ->
                HistoryItem(result)
            }
        }
    }
}

@Composable
private fun LatestScoreSection(result: AtsResult) {
    DashboardCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(AivanceTheme.spacing.large)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Current Score",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Resume Analysis",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(AivanceTheme.spacing.small))
                Text(
                    text = "Analyzed on ${formatDate(result.date)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            ScoreGauge(score = result.score, size = 120.dp, strokeWidth = 10.dp)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SuggestionsSection(result: AtsResult) {
    Column(verticalArrangement = Arrangement.spacedBy(AivanceTheme.spacing.small)) {
        SectionHeader(title = "Improvements")
        DashboardCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(AivanceTheme.spacing.medium)) {
                Text(
                    text = "Feedback",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = result.feedback,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (result.missingKeywords.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(AivanceTheme.spacing.medium))
                    Text(
                        text = "Missing Keywords",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(AivanceTheme.spacing.small))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        result.missingKeywords.forEach { keyword ->
                            KeywordChip(text = keyword, isMatched = false)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(result: AtsResult) {
    DashboardCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(AivanceTheme.spacing.medium)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScoreGauge(score = result.score, size = 48.dp, strokeWidth = 4.dp)
            Spacer(modifier = Modifier.width(AivanceTheme.spacing.medium))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Resume Analysis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatDate(result.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

private fun formatDate(instant: Instant): String {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(formatter)
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun AtsScreenPreview() {
    AivanceTheme(darkTheme = true) {
        AtsContent(
            latestResult = AtsResult(
                resumeId = 1,
                jobDescription = "Android Engineer",
                score = 85,
                date = Instant.now(),
                matchedKeywords = listOf("Java", "Kotlin"),
                missingKeywords = listOf("Coroutines", "Dagger Hilt"),
                feedback = "Great resume! Consider adding more details about your contributions in the 'Experience' section."
            ),
            history = listOf(
                AtsResult(
                    resumeId = 1,
                    jobDescription = "Android Dev",
                    score = 70,
                    date = Instant.now().minusSeconds(86400 * 2),
                    matchedKeywords = emptyList(),
                    missingKeywords = listOf("Compose"),
                    feedback = ""
                ),
                AtsResult(
                    resumeId = 1,
                    jobDescription = "Mobile Dev",
                    score = 65,
                    date = Instant.now().minusSeconds(86400 * 5),
                    matchedKeywords = emptyList(),
                    missingKeywords = listOf("Compose", "MVI"),
                    feedback = ""
                )
            )
        )
    }
}
