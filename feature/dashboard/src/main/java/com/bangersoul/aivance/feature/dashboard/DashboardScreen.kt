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
                text = greeting.ifBlank { "Good Morning 👋" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = designation.ifBlank { "Your career command center" },
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
                    contentDescription = "Notifications",
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
                contentDescription = "Profile",
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
            SectionHeader(title = "Overview")
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "ATS Score",
                    value = "${state.atsScore}",
                    icon = Icons.Rounded.FactCheck,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Active Apps",
                    value = "${state.activeApplications}",
                    icon = Icons.Rounded.Send,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Saved Jobs",
                    value = "${state.savedJobs}",
                    icon = Icons.Rounded.Bookmark,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Next interview card (with countdown chip)
        state.nextInterview?.let { interview ->
            item {
                SectionHeader(title = "Next Interview")
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
            SectionHeader(title = "Quick Actions")
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
            SectionHeader(title = "Recent Activity", ctaText = "View All", onCtaClick = onNavigateToAnalytics)
            Spacer(Modifier.height(10.dp))
            if (state.recentActivity.isEmpty()) {
                EmptyStateCard(
                    title = "No activity yet",
                    description = "Applications, ATS scans and AI sessions will appear here.",
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
                        text = "Career Score",
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
                        score >= 80 -> "Top tier"
                        score >= 60 -> "Strong"
                        score > 0 -> "Building"
                        else -> "Not scored yet"
                    },
                    containerColor = color.copy(alpha = 0.12f),
                    contentColor = color
                )
            }
        }
    }
}

@Composable
private fun scoreColor(score: Int): Color = when {
    score >= 80 -> AivanceTheme.colors.success
    score >= 60 -> AivanceTheme.colors.accent
    score > 0 -> AivanceTheme.colors.warning
    else -> MaterialTheme.colorScheme.surfaceVariant
}

private fun scoreTitle(score: Int): String = when {
    score >= 80 -> "Ready to apply"
    score >= 60 -> "Strong profile"
    score > 0 -> "Keep building"
    else -> "Unlock your score"
}

private fun scoreMessage(score: Int): String = when {
    score >= 80 -> "Top-tier profile — you're in great shape for applications."
    score >= 60 -> "Solid foundation with clear room to grow."
    score > 0 -> "Every point counts — upload & analyze a resume to level up."
    else -> "Upload a resume and run an ATS scan to unlock scoring."
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
                    text = "Upcoming interview",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            MetricChip(
                label = "Prepare",
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
                    text = "AI Recommendation",
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
                contentDescription = "Open",
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
                label = "Resume",
                icon = Icons.Rounded.Description,
                tint = AivanceTheme.colors.accent,
                onClick = onResume,
                modifier = Modifier.weight(1f)
            )
            QuickActionTile(
                label = "Jobs",
                icon = Icons.Rounded.WorkOutline,
                tint = AivanceTheme.colors.info,
                onClick = onJobs,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickActionTile(
                label = "Interview",
                icon = Icons.Rounded.RecordVoiceOver,
                tint = AivanceTheme.colors.warning,
                onClick = onInterview,
                modifier = Modifier.weight(1f)
            )
            QuickActionTile(
                label = "Assistant",
                icon = Icons.Rounded.SmartToy,
                tint = AivanceTheme.colors.success,
                onClick = onAssistant,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickActionTile(
                label = "Pipeline",
                icon = Icons.Rounded.ViewKanban,
                tint = MaterialTheme.colorScheme.primary,
                onClick = onTracker,
                modifier = Modifier.weight(1f)
            )
            QuickActionTile(
                label = "Insights",
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
