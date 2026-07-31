package com.bangersoul.aivance.feature.analytics

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bangersoul.aivance.core.common.model.AnalyticsSnapshot
import com.bangersoul.aivance.core.common.model.CareerRecommendation
import com.bangersoul.aivance.core.designsystem.components.*
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        AivanceTopBar(title = "Career Intelligence", onBack = onBack)
        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "AnalyticsTransition"
        ) { state ->
            when (state) {
                is AnalyticsUiState.Loading -> SkeletonDashboard(modifier = Modifier.fillMaxSize())
                is AnalyticsUiState.Error -> AivanceError(
                    message = state.message,
                    onRetry = { viewModel.refresh() },
                    title = "Analytics unavailable",
                    detail = "Snapshot data could not be loaded from the local database."
                )
                is AnalyticsUiState.Success -> AnalyticsDashboardContent(
                    snapshot = state.latestSnapshot,
                    historicalSnapshots = state.historicalSnapshots,
                    recommendations = state.recommendations,
                    onDismissRecommendation = { viewModel.dismissRecommendation(it) }
                )
                else -> {}
            }
        }
    }
}

@Composable
private fun AnalyticsDashboardContent(
    snapshot: AnalyticsSnapshot?,
    historicalSnapshots: List<AnalyticsSnapshot>,
    recommendations: List<CareerRecommendation>,
    onDismissRecommendation: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Career Score Hub
        item {
            DashboardCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Current Career Score",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    DonutChart(
                        fraction = (snapshot?.careerScore ?: 0) / 100f,
                        size = 150,
                        color = AivanceTheme.colors.accent,
                        centerText = "${snapshot?.careerScore ?: 0}"
                    )
                    Text(
                        text = when {
                            (snapshot?.careerScore ?: 0) >= 80 -> "Top-tier candidate readiness"
                            (snapshot?.careerScore ?: 0) >= 60 -> "Strong momentum — keep optimizing"
                            else -> "Every action moves the score — start with a resume scan"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = AivanceTheme.colors.accent
                    )
                }
            }
        }

        val sortedSnapshots = historicalSnapshots.sortedBy { it.timestamp }

        // Career Score Trend
        if (sortedSnapshots.size >= 2) {
            item {
                SectionHeader(title = "Score Trend")
                Spacer(Modifier.height(10.dp))
                DashboardCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        LineChart(
                            values = sortedSnapshots.map { it.careerScore.toFloat() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Dimension Scores
        val dimensions = snapshot?.dimensionScores
        if (dimensions != null && dimensions.isNotEmpty()) {
            item {
                SectionHeader(title = "Performance Dimensions")
                Spacer(Modifier.height(10.dp))
                DashboardCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        BarChart(
                            data = dimensions.entries
                                .sortedByDescending { it.value }
                                .map { it.key.replace('_', ' ') to it.value / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Activity Heat Map
        if (sortedSnapshots.isNotEmpty()) {
            item {
                SectionHeader(title = "Score History")
                Spacer(Modifier.height(10.dp))
                DashboardCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        HeatMapGrid(
                            values = buildHeatMapData(sortedSnapshots),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Application Funnel
        item {
            SectionHeader(title = "Application Funnel")
            Spacer(Modifier.height(10.dp))
            DashboardCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val kpis = snapshot?.kpis ?: emptyMap()
                    val funnel = listOf(
                        "Applications" to (kpis.entries.firstOrNull { it.key.equals("applications", true) }?.value?.toInt() ?: 0),
                        "Interviews" to (kpis.entries.firstOrNull { it.key.equals("interviews", true) }?.value?.toInt() ?: 0),
                        "Offers" to (kpis.entries.firstOrNull { it.key.equals("offers", true) }?.value?.toInt() ?: 0)
                    )
                    FunnelChart(stages = funnel, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        // Recommendations
        item {
            SectionHeader(title = "Priority Recommendations")
            Spacer(Modifier.height(10.dp))
            if (recommendations.isEmpty()) {
                EmptyStateCard(
                    title = "No recommendations yet",
                    description = "As you use the app, the intelligence engine will surface prioritized actions.",
                    icon = Icons.Rounded.AutoAwesome
                )
            }
        }

        items(recommendations) { rec ->
            RecommendationCard(rec, onDismiss = { onDismissRecommendation(rec.id) })
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
/**
 * Builds a simple 7xN heat map from snapshot career scores,
 * one column per snapshot week.
 */
private fun buildHeatMapData(sortedSnapshots: List<AnalyticsSnapshot>): List<List<Float>> {
    if (sortedSnapshots.isEmpty()) return emptyList()
    val rows = 7
    return (0 until rows).map { row ->
        sortedSnapshots.mapIndexed { index, snap ->
            val base = snap.careerScore / 100f
            // Offset each row slightly so the grid reads as depth/activity.
            val rowFactor = ((index + row) % 5) * 0.03f
            (base - rowFactor).coerceIn(0f, 1f)
        }
    }
}

@Composable
private fun RecommendationCard(rec: CareerRecommendation, onDismiss: () -> Unit) {
    val tone = if (rec.priority == "HIGH") BannerTone.WARNING else BannerTone.INFO
    DashboardCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = when (rec.priority) {
                    "HIGH" -> AivanceTheme.colors.warning
                    else -> AivanceTheme.colors.info
                },
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    rec.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(rec.description, style = MaterialTheme.typography.bodyMedium)
            }
            StatusChip(text = rec.priority, tone = tone)
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.Top)) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = "Dismiss", modifier = Modifier.size(18.dp))
            }
        }
    }
}
