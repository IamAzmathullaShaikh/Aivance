package com.bangersoul.aivance.feature.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
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
import com.bangersoul.aivance.feature.dashboard.domain.*
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToResume: () -> Unit,
    onNavigateToTracker: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToInterview: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToJobs: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        DashboardHeader(onNavigateToProfile = onNavigateToProfile)
        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "DashboardState"
        ) { state ->
            when (state) {
                is DashboardUiState.Loading -> SkeletonDashboard(modifier = Modifier.fillMaxSize())
                is DashboardUiState.Error -> AivanceError(
                    message = state.message ?: "Failed to load dashboard",
                    onRetry = { viewModel.onEvent(DashboardUiEvent.Retry) }
                )
                is DashboardUiState.Success -> DashboardContent(
                    data = state.dashboardData ?: DashboardData(
                        0,
                        ResumeStatus("", LocalDate.now()),
                        0,
                        0,
                        ""
                    ),
                    onNavigateToResume = onNavigateToResume,
                    onNavigateToTracker = onNavigateToTracker,
                    onNavigateToProfile = onNavigateToProfile,
                    onNavigateToInterview = onNavigateToInterview,
                    onNavigateToAnalytics = onNavigateToAnalytics,
                    onNavigateToJobs = onNavigateToJobs
                )
                else -> {}
            }
        }
    }
}

@Composable
private fun DashboardHeader(onNavigateToProfile: () -> Unit) {
    val time = LocalTime.now()
    val greeting = when (time.hour) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Your career command center",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Surface(
            onClick = onNavigateToProfile,
            shape = AivanceTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
            Icon(
                Icons.Rounded.Person,
                contentDescription = "Profile",
                modifier = Modifier.padding(10.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
internal fun DashboardContent(
    data: DashboardData,
    onNavigateToResume: () -> Unit,
    onNavigateToTracker: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToInterview: () -> Unit,
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToJobs: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Quick Actions
        item {
            QuickActionsRow(
                onResume = onNavigateToResume,
                onJobs = onNavigateToJobs,
                onInterview = onNavigateToInterview,
                onTracker = onNavigateToTracker,
                onAnalytics = onNavigateToAnalytics
            )
        }

        // Career Score + Pipeline hero
        item {
            CareerScoreHero(
                data = data,
                onNavigateToAnalytics = onNavigateToAnalytics
            )
        }

        // Pipeline Progress
        if (data.pipelineProgress.isNotEmpty()) {
            item {
                SectionHeader(title = "Pipeline Progress", ctaText = "View Pipeline", onCtaClick = onNavigateToTracker)
                Spacer(Modifier.height(10.dp))
                PipelineProgressCard(progress = data.pipelineProgress)
            }
        }

        // Upcoming Interviews
        if (data.upcomingInterviews.isNotEmpty()) {
            item {
                SectionHeader(title = "Upcoming Interviews")
                Spacer(Modifier.height(10.dp))
                InterviewList(interviews = data.upcomingInterviews)
            }
        }

        // Profile Progress
        item {
            SectionHeader(title = "Profile Progress")
            Spacer(Modifier.height(10.dp))
            ProgressCard(
                title = "Profile completion",
                progress = data.profileCompletion / 100f,
                valueLabel = "${data.profileCompletion}% Complete",
                subtitle = if (data.profileCompletion >= 80) "Ready to apply — strong profile!" else "Complete the remaining steps to unlock full match power.",
                progressColor = AivanceTheme.colors.accent,
                onClick = onNavigateToProfile
            )
        }

        // Resume & ATS Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ResumeCardItem(
                    fileName = data.resumeStatus.fileName,
                    onClick = onNavigateToResume,
                    modifier = Modifier.weight(1f)
                )
                AtsScoreCardItem(
                    score = data.atsScore,
                    onClick = onNavigateToAnalytics,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Weekly Goals
        if (data.weeklyGoals.isNotEmpty()) {
            item {
                SectionHeader(title = "Weekly Goals")
                Spacer(Modifier.height(10.dp))
                data.weeklyGoals.forEach { goal ->
                    ProgressCard(
                        title = goal.title,
                        progress = goal.target.let { if (it == 0) 0f else goal.current.toFloat() / it },
                        valueLabel = "${goal.current}/${goal.target}",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        // Today's Priorities
        if (data.tasks.isNotEmpty()) {
            item {
                SectionHeader(title = "Today's Priorities")
                Spacer(Modifier.height(10.dp))
                PriorityList(tasks = data.tasks)
            }
        }

        // Career Insights
        if (data.insights.isNotEmpty()) {
            item {
                SectionHeader(title = "Career Insights", ctaText = "See All", onCtaClick = onNavigateToAnalytics)
                Spacer(Modifier.height(10.dp))
                data.insights.forEach { insight ->
                    InsightCard(text = insight.text, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        // Recommendations
        item {
            SectionHeader(title = "Recommendations")
            Spacer(Modifier.height(10.dp))
            if (data.jobRecommendations.isEmpty()) {
                EmptyStateCard(
                    title = "No recommendations yet",
                    description = "Complete your profile and upload a resume to unlock AI-matched opportunities.",
                    icon = Icons.Rounded.AutoAwesome,
                    actionText = "Complete Profile",
                    onActionClick = onNavigateToProfile
                )
            } else {
                data.jobRecommendations.forEach { rec ->
                    ActionCard(
                        title = rec.title,
                        subtitle = rec.company,
                        icon = Icons.Rounded.WorkOutline,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        // Recent Activity
        item {
            SectionHeader(title = "Recent Activity")
            Spacer(Modifier.height(10.dp))
            if (data.recentActivity.isEmpty()) {
                EmptyStateCard(
                    title = "No activity yet",
                    description = "Actions like applications, scans, and outreach will appear here.",
                    icon = Icons.Rounded.History
                )
            } else {
                data.recentActivity.forEach { activity ->
                    ActivityRow(activity)
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun QuickActionsRow(
    onResume: () -> Unit,
    onJobs: () -> Unit,
    onInterview: () -> Unit,
    onTracker: () -> Unit,
    onAnalytics: () -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            QuickAction(
                label = "Resume",
                icon = Icons.Rounded.Description,
                tint = AivanceTheme.colors.accent,
                onClick = onResume
            )
        }
        item {
            QuickAction(
                label = "Jobs",
                icon = Icons.Rounded.WorkOutline,
                tint = AivanceTheme.colors.info,
                onClick = onJobs
            )
        }
        item {
            QuickAction(
                label = "Interview",
                icon = Icons.Rounded.RecordVoiceOver,
                tint = AivanceTheme.colors.warning,
                onClick = onInterview
            )
        }
        item {
            QuickAction(
                label = "Pipeline",
                icon = Icons.Rounded.ViewKanban,
                tint = AivanceTheme.colors.success,
                onClick = onTracker
            )
        }
        item {
            QuickAction(
                label = "Insights",
                icon = Icons.Rounded.BarChart,
                tint = MaterialTheme.colorScheme.primary,
                onClick = onAnalytics
            )
        }
    }
}

@Composable
private fun QuickAction(
    label: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = AivanceTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp), tint = tint)
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun CareerScoreHero(
    data: DashboardData,
    onNavigateToAnalytics: () -> Unit
) {
    DashboardCard(onClick = onNavigateToAnalytics, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ScoreGauge(score = data.atsScore, size = 96.dp)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Career Score", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = when {
                        data.atsScore >= 80 -> "Ready to apply — top-tier profile"
                        data.atsScore >= 60 -> "Strong profile with room to grow"
                        data.atsScore > 0 -> "Keep building — every point counts"
                        else -> "Upload a resume to unlock scoring"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusChip(text = "${data.activeApplications} active", tone = BannerTone.INFO)
                    if (data.atsScore > 0) {
                        StatusChip(text = "Top 5%", tone = BannerTone.SUCCESS)
                    }
                }
            }
        }
    }
}

@Composable
private fun PipelineProgressCard(progress: Map<String, Int>) {
    DashboardCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val total = progress.values.sum().coerceAtLeast(1)
            progress.toList().forEach { (stage, count) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stage.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelMedium)
                    Text("$count", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
                AnimatedProgress(
                    progress = count.toFloat() / total,
                    color = AivanceTheme.colors.accent,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun InterviewList(interviews: List<UpcomingInterview>) {
    interviews.forEach { interview ->
        DashboardCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(14.dp),
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
                    Text(interview.role, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(interview.company, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    interview.dateTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ResumeCardItem(fileName: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    DashboardCard(onClick = onClick, modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(Icons.Rounded.Description, null, tint = AivanceTheme.colors.accent, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(10.dp))
            Text("Resume Uploaded", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Text(
                fileName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            AivanceTertiaryButton(text = "Open", onClick = onClick)
        }
    }
}

@Composable
private fun AtsScoreCardItem(score: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    DashboardCard(onClick = onClick, modifier = modifier) {
        val animated by animateFloatAsState(targetValue = score / 100f, label = "score")
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { animated },
                    modifier = Modifier.size(52.dp),
                    color = AivanceTheme.colors.accent,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round,
                    strokeWidth = 5.dp
                )
                Text("$score", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            Text("ATS Score", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun PriorityList(tasks: List<DashboardTask>) {
    tasks.forEach { task ->
        DashboardCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val (tint, icon) = when (task.priority) {
                    "HIGH" -> AivanceTheme.colors.warning to Icons.Rounded.PriorityHigh
                    "LOW" -> AivanceTheme.colors.success to Icons.Rounded.LowPriority
                    else -> AivanceTheme.colors.info to Icons.Rounded.Flag
                }
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = tint)
                Text(
                    task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (task.priority == "HIGH") FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ActivityRow(activity: RecentActivity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = AivanceTheme.shapes.medium,
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
            activity.date.toString(),
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
            data = DashboardData(
                profileCompletion = 72,
                resumeStatus = ResumeStatus("Resume.pdf", LocalDate.now()),
                atsScore = 85,
                activeApplications = 6,
                interviewPrepStatus = "Ready",
                pipelineProgress = mapOf("Saved" to 4, "Applied" to 6, "Interview" to 2, "Offer" to 1),
                upcomingInterviews = listOf(UpcomingInterview("1", "Google", "Senior Android Engineer", "Fri 10:00")),
                tasks = listOf(DashboardTask("1", "Optimize resume for 3 roles", "HIGH"), DashboardTask("2", "Follow up with Acme recruiter", "MEDIUM")),
                weeklyGoals = listOf(WeeklyGoal("1", "Applications this week", 5, 3)),
                insights = listOf(CareerInsight("1", "Your ATS score improved 8% this week — keep tailoring!"))
            ),
            onNavigateToResume = {},
            onNavigateToTracker = {},
            onNavigateToProfile = {},
            onNavigateToInterview = {},
            onNavigateToAnalytics = {}
        )
    }
}
