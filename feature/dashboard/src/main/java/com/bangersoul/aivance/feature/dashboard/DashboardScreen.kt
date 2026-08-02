package com.bangersoul.aivance.feature.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bangersoul.aivance.core.designsystem.components.*
import com.bangersoul.aivance.core.designsystem.theme.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToResume: () -> Unit,
    onNavigateToTracker: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToInterview: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToJobs: () -> Unit = {},
    onNavigateToAssistant: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        DashboardHeader(
            greeting = uiState.greeting,
            designation = uiState.userDesignation,
            onNavigateToProfile = onNavigateToProfile,
            onNavigateToNotifications = onNavigateToNotifications
        )
        val error = uiState.error
        when {
            uiState.isLoading -> SkeletonDashboard(modifier = Modifier.fillMaxSize())
            error != null -> AivanceError(
                message = error,
                onRetry = { viewModel.onEvent(DashboardUiEvent.Retry) }
            )
            else -> DashboardContent(
                state = uiState,
                onNavigateToResume = onNavigateToResume,
                onNavigateToJobs = onNavigateToJobs,
                onNavigateToInterview = onNavigateToInterview,
                onNavigateToAssistant = onNavigateToAssistant,
                onNavigateToTracker = onNavigateToTracker,
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToAnalytics = onNavigateToAnalytics
            )
        }
    }
}

@Composable
private fun DashboardHeader(
    greeting: String,
    designation: String,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = greeting.ifBlank { stringResource(R.string.dash_greeting_fallback) },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = designation.ifBlank { stringResource(R.string.dash_designation_fallback) },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        // Notification bell with unread badge dot
        Box {
            Surface(
                onClick = onNavigateToNotifications,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Icon(
                    Icons.Rounded.Notifications,
                    contentDescription = stringResource(R.string.dash_notifications_cd),
                    modifier = Modifier.padding(10.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.error, CircleShape)
            )
        }
        Spacer(Modifier.width(10.dp))
        // Avatar → Profile
        Surface(
            onClick = onNavigateToProfile,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                Icons.Rounded.Person,
                contentDescription = stringResource(R.string.dash_profile_cd),
                modifier = Modifier.padding(10.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
internal fun DashboardContent(
    state: DashboardUiState,
    onNavigateToResume: () -> Unit,
    onNavigateToJobs: () -> Unit,
    onNavigateToInterview: () -> Unit,
    onNavigateToAssistant: () -> Unit,
    onNavigateToTracker: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAnalytics: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Career Score hero — large animated circular gauge
        item {
            CareerScoreCard(
                score = state.careerScore,
                onNavigateToAnalytics = onNavigateToAnalytics
            )
        }

        // 2. Quick stats row: ATS | Active Apps | Saved Jobs
        item {
            SectionHeader(title = stringResource(R.string.dash_overview))
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = stringResource(R.string.dash_ats_score),
                    value = "${state.atsScore}",
                    icon = Icons.Rounded.FactCheck,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = stringResource(R.string.dash_active_apps),
                    value = "${state.activeApplications}",
                    icon = Icons.Rounded.Send,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = stringResource(R.string.dash_saved_jobs),
                    value = "${state.savedJobs}",
                    icon = Icons.Rounded.Bookmark,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 2.5 Career breakdown pie chart: ATS | Applied | Saved | Career Score
        item {
            SectionHeader(title = stringResource(R.string.dash_career_breakdown))
            Spacer(Modifier.height(10.dp))
            CareerBreakdownCard(
                atsScore = state.atsScore,
                activeApplications = state.activeApplications,
                savedJobs = state.savedJobs,
                careerScore = state.careerScore
            )
        }

        // 3. Next interview card (with countdown chip)
        state.nextInterview?.let { interview ->
            item {
                SectionHeader(title = stringResource(R.string.dash_next_interview))
                Spacer(Modifier.height(10.dp))
                NextInterviewCard(
                    dateTime = interview,
                    onClick = onNavigateToInterview
                )
            }
        }

        // 4. AI Recommendation card (highlighted, with CTA)
        state.aiRecommendation?.let { recommendation ->
            item {
                AiRecommendationCard(
                    recommendation = recommendation,
                    onClick = onNavigateToAssistant
                )
            }
        }

        // 5. Quick Actions 2x2 grid
        item {
            SectionHeader(title = stringResource(R.string.dash_quick_actions))
            Spacer(Modifier.height(10.dp))
            QuickActionsGrid(
                onResume = onNavigateToResume,
                onJobs = onNavigateToJobs,
                onInterview = onNavigateToInterview,
                onAssistant = onNavigateToAssistant,
                onTracker = onNavigateToTracker,
                onAnalytics = onNavigateToAnalytics
            )
        }

        // 6. Recent Activity feed (last 5)
        item {
            SectionHeader(title = stringResource(R.string.dash_recent_activity), ctaText = stringResource(R.string.dash_view_all), onCtaClick = onNavigateToAnalytics)
            Spacer(Modifier.height(10.dp))
            if (state.recentActivity.isEmpty()) {
                EmptyStateCard(
                    title = stringResource(R.string.dash_no_activity),
                    description = stringResource(R.string.dash_no_activity_detail),
                    icon = Icons.Rounded.History
                )
            } else {
                state.recentActivity.forEach { activity ->
                    ActivityRow(activity)
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun CareerScoreCard(score: Int, onNavigateToAnalytics: () -> Unit) {
    val animated by animateFloatAsState(
        targetValue = score.coerceIn(0, 100) / 100f,
        animationSpec = tween(durationMillis = 1000),
        label = "CareerScore"
    )
    val color = scoreColor(score)
    DashboardCard(onClick = onNavigateToAnalytics, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                CircularProgressIndicator(
                    progress = { animated },
                    modifier = Modifier.fillMaxSize(),
                    color = color,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round,
                    strokeWidth = 10.dp
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$score",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        text = stringResource(R.string.dash_career_score),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = scoreTitle(score),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = scoreMessage(score),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                MetricChip(
                    label = when {
                        score >= 80 -> stringResource(R.string.dash_chip_top_tier)
                        score >= 60 -> stringResource(R.string.dash_chip_strong)
                        score > 0 -> stringResource(R.string.dash_chip_building)
                        else -> stringResource(R.string.dash_chip_not_scored)
                    },
                    containerColor = color.copy(alpha = 0.12f),
                    contentColor = color
                )
            }
        }
    }
}

@Composable
private fun CareerBreakdownCard(
    atsScore: Int,
    activeApplications: Int,
    savedJobs: Int,
    careerScore: Int
) {
    // Raw values — PieChart skips zero sweeps, so a new user (0 ATS, 0 applied,
    // 0 saved) sees an honest empty chart rather than four fake equal slices.
    val segments = listOf(
        PieSegment(stringResource(R.string.dash_pie_career_score), careerScore.toFloat(), AivanceTheme.colors.accent),
        PieSegment(stringResource(R.string.dash_pie_ats_score), atsScore.toFloat(), AivanceTheme.colors.info),
        PieSegment(stringResource(R.string.dash_pie_applied), activeApplications.toFloat(), AivanceTheme.colors.success),
        PieSegment(stringResource(R.string.dash_pie_saved_jobs), savedJobs.toFloat(), AivanceTheme.colors.warning)
    )

    DashboardCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                PieChart(
                    segments = segments,
                    size = 132,
                    centerText = "${careerScore}\n${stringResource(R.string.dash_score_word)}"
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    segments.forEach { segment ->
                        BreakdownLegend(
                            label = segment.label,
                            value = segment.value.toInt(),
                            color = segment.color
                        )
                    }
                }
            }
            Text(
                text = stringResource(R.string.dash_breakdown_caption),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BreakdownLegend(label: String, value: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "$value",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun scoreColor(score: Int): Color = when {
    score >= 80 -> AivanceTheme.colors.success
    score >= 60 -> AivanceTheme.colors.accent
    score > 0 -> AivanceTheme.colors.warning
    else -> MaterialTheme.colorScheme.surfaceVariant
}

@Composable
private fun scoreTitle(score: Int): String = when {
    score >= 80 -> stringResource(R.string.dash_score_title_ready)
    score >= 60 -> stringResource(R.string.dash_score_title_strong)
    score > 0 -> stringResource(R.string.dash_score_title_building)
    else -> stringResource(R.string.dash_score_title_unlock)
}

@Composable
private fun scoreMessage(score: Int): String = when {
    score >= 80 -> stringResource(R.string.dash_score_msg_ready)
    score >= 60 -> stringResource(R.string.dash_score_msg_strong)
    score > 0 -> stringResource(R.string.dash_score_msg_building)
    else -> stringResource(R.string.dash_score_msg_unlock)
}

@Composable
private fun NextInterviewCard(dateTime: String, onClick: () -> Unit) {
    DashboardCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = AivanceTheme.shapes.medium,
                color = AivanceTheme.colors.warning.copy(alpha = 0.15f)
            ) {
                Icon(
                    Icons.Rounded.Event,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp),
                    tint = AivanceTheme.colors.warning
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateTime,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.dash_upcoming_interview),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            MetricChip(
                label = stringResource(R.string.dash_prepare),
                containerColor = AivanceTheme.colors.warning.copy(alpha = 0.12f),
                contentColor = AivanceTheme.colors.warning
            )
        }
    }
}

@Composable
private fun AiRecommendationCard(recommendation: String, onClick: () -> Unit) {
    DashboardCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    AivanceTheme.colors.accent.copy(alpha = 0.08f),
                    RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = AivanceTheme.colors.accent.copy(alpha = 0.15f)
            ) {
                Icon(
                    Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp),
                    tint = AivanceTheme.colors.accent
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.dash_ai_recommendation),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = AivanceTheme.colors.accent
                )
                Text(
                    text = recommendation,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = stringResource(R.string.dash_open_cd),
                tint = AivanceTheme.colors.accent
            )
        }
    }
}

@Composable
private fun QuickActionsGrid(
    onResume: () -> Unit,
    onJobs: () -> Unit,
    onInterview: () -> Unit,
    onAssistant: () -> Unit,
    onTracker: () -> Unit,
    onAnalytics: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickActionTile(
                label = stringResource(R.string.dash_action_resume),
                icon = Icons.Rounded.Description,
                tint = AivanceTheme.colors.accent,
                onClick = onResume,
                modifier = Modifier.weight(1f)
            )
            QuickActionTile(
                label = stringResource(R.string.dash_action_jobs),
                icon = Icons.Rounded.WorkOutline,
                tint = AivanceTheme.colors.info,
                onClick = onJobs,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickActionTile(
                label = stringResource(R.string.dash_action_interview),
                icon = Icons.Rounded.RecordVoiceOver,
                tint = AivanceTheme.colors.warning,
                onClick = onInterview,
                modifier = Modifier.weight(1f)
            )
            QuickActionTile(
                label = stringResource(R.string.dash_action_assistant),
                icon = Icons.Rounded.SmartToy,
                tint = AivanceTheme.colors.success,
                onClick = onAssistant,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickActionTile(
                label = stringResource(R.string.dash_action_pipeline),
                icon = Icons.Rounded.ViewKanban,
                tint = MaterialTheme.colorScheme.primary,
                onClick = onTracker,
                modifier = Modifier.weight(1f)
            )
            QuickActionTile(
                label = stringResource(R.string.dash_action_insights),
                icon = Icons.Rounded.BarChart,
                tint = MaterialTheme.colorScheme.secondary,
                onClick = onAnalytics,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionTile(
    label: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = AivanceTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(shape = CircleShape, color = tint.copy(alpha = 0.12f)) {
                Icon(
                    icon,
                    contentDescription = label,
                    modifier = Modifier.padding(7.dp).size(18.dp),
                    tint = tint
                )
            }
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ActivityRow(activity: ActivityItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Icon(
                Icons.Rounded.EventNote,
                contentDescription = null,
                modifier = Modifier.padding(8.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(activity.description, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            activity.date,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun DashboardContentPreview() {
    AivanceTheme(darkTheme = true) {
        DashboardContent(
            state = DashboardUiState(
                isLoading = false,
                greeting = "Good Morning, Azmath",
                userDesignation = "Software Engineer at TCS",
                careerScore = 78,
                atsScore = 85,
                activeApplications = 6,
                nextInterview = "Fri 10:00",
                savedJobs = 4,
                aiRecommendation = "Tailor your resume for senior Android roles to boost your match rate.",
                recentActivity = listOf(
                    ActivityItem("1", "Applied to Senior Android Engineer at Acme", "Aug 1"),
                    ActivityItem("2", "ATS scan completed — 85% match", "Jul 31")
                )
            ),
            onNavigateToResume = {},
            onNavigateToJobs = {},
            onNavigateToInterview = {},
            onNavigateToAssistant = {},
            onNavigateToTracker = {},
            onNavigateToProfile = {},
            onNavigateToAnalytics = {}
        )
    }
}
