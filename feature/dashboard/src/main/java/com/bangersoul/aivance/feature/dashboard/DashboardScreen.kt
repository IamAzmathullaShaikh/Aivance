package com.bangersoul.aivance.feature.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bangersoul.aivance.core.designsystem.components.ActionButton
import com.bangersoul.aivance.core.designsystem.components.AnimatedProgress
import com.bangersoul.aivance.core.designsystem.components.AivanceScreen
import com.bangersoul.aivance.core.designsystem.components.DashboardCard
import com.bangersoul.aivance.core.designsystem.components.EmptyStateCard
import com.bangersoul.aivance.core.designsystem.components.MetricChip
import com.bangersoul.aivance.core.designsystem.components.SectionHeader
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme
import com.bangersoul.aivance.core.designsystem.theme.DarkAccent
import com.bangersoul.aivance.core.designsystem.theme.Zinc800
import com.bangersoul.aivance.core.designsystem.theme.Zinc900
import com.bangersoul.aivance.feature.dashboard.domain.DashboardData
import com.bangersoul.aivance.feature.dashboard.domain.ResumeStatus
import java.time.LocalDate

import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToResume: () -> Unit,
    onNavigateToTracker: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToInterview: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AivanceScreen(
        topBar = {
            val time = LocalTime.now()
            val greeting = when (time.hour) {
                in 0..11 -> "Good Morning,"
                in 12..16 -> "Good Afternoon,"
                else -> "Good Evening,"
            }
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Welcome back.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Notifications placeholder */ }) {
                        Icon(Icons.Rounded.Notifications, contentDescription = "Notifications")
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        isLoading = uiState is DashboardUiState.Loading,
        error = (uiState as? DashboardUiState.Error)?.message
    ) {
        AnimatedContent(
            targetState = uiState,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "DashboardState"
        ) { state ->
            when (state) {
                is DashboardUiState.Success -> {
                    DashboardContent(
                        data = state.dashboardData ?: DashboardData(profileCompletion = 0, resumeStatus = ResumeStatus("", java.time.LocalDate.now()), atsScore = 0, activeApplications = 0, interviewPrepStatus = ""),
                        onNavigateToResume = onNavigateToResume,
                        onNavigateToTracker = onNavigateToTracker,
                        onNavigateToProfile = onNavigateToProfile,
                        onNavigateToInterview = onNavigateToInterview
                    )
                }
                is DashboardUiState.Error, DashboardUiState.Loading, DashboardUiState.Empty -> {
                    Box(modifier = Modifier.fillMaxSize())
                }
            }
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
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(AivanceTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(AivanceTheme.spacing.large)
    ) {
        // Profile Completion
        item {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically()
            ) {
                Column {
                    SectionHeader(title = "Profile Progress")
                    Spacer(Modifier.height(AivanceTheme.spacing.small))
                    DashboardCard {
                        Column(modifier = Modifier.padding(AivanceTheme.spacing.medium)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${data.profileCompletion}% Complete",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                MetricChip(
                                    label = "Action Required",
                                    containerColor = DarkAccent.copy(alpha = 0.1f),
                                    contentColor = DarkAccent
                                )
                            }
                            Spacer(Modifier.height(AivanceTheme.spacing.medium))
                            AnimatedProgress(progress = data.profileCompletion / 100f)
                            Spacer(Modifier.height(AivanceTheme.spacing.medium))
                            ActionButton(
                                text = "Complete Profile",
                                onClick = onNavigateToProfile,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // Resume & ATS Score Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AivanceTheme.spacing.medium)
            ) {
                // Resume Card
                DashboardCard(
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(AivanceTheme.spacing.medium)) {
                        Icon(
                            imageVector = Icons.Rounded.Description,
                            contentDescription = "Resume Icon",
                            tint = DarkAccent
                        )
                        Spacer(Modifier.height(AivanceTheme.spacing.small))
                        Text(
                            text = "Resume Uploaded",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = data.resumeStatus.fileName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(AivanceTheme.spacing.medium))
                        ActionButton(
                            text = "Open",
                            onClick = onNavigateToResume,
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = Zinc800,
                            contentColor = Color.White
                        )
                    }
                }

                // ATS Score Card
                DashboardCard(
                    modifier = Modifier.weight(1f)
                ) {
                    val animatedAtsScore by animateFloatAsState(
                        targetValue = data.atsScore / 100f,
                        animationSpec = tween(1000),
                        label = "AtsScoreAnimation"
                    )
                    Column(
                        modifier = Modifier.padding(AivanceTheme.spacing.medium),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { animatedAtsScore },
                                modifier = Modifier.size(64.dp),
                                color = DarkAccent,
                                trackColor = Zinc900,
                                strokeCap = StrokeCap.Round,
                                strokeWidth = 6.dp
                            )
                            Text(
                                text = "${data.atsScore}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(AivanceTheme.spacing.small))
                        Text(
                            text = "ATS Score",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Applications
        item {
            SectionHeader(
                title = "Job Tracker",
                ctaText = "View All",
                onCtaClick = onNavigateToTracker
            )
            Spacer(Modifier.height(AivanceTheme.spacing.small))
            DashboardCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AivanceTheme.spacing.medium),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${data.activeApplications} Active Applications",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Keep pushing forward!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    ActionButton(
                        text = "Track",
                        onClick = onNavigateToTracker,
                        icon = Icons.Rounded.Add
                    )
                }
            }
        }

        // Interview Prep
        item {
            SectionHeader(title = "Interview Prep")
            Spacer(Modifier.height(AivanceTheme.spacing.small))
            DashboardCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AivanceTheme.spacing.medium),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ready to Practice",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = data.interviewPrepStatus,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    ActionButton(
                        text = "Start Session",
                        onClick = onNavigateToInterview
                    )
                }
            }
        }


        // Job Recommendations
        item {
            SectionHeader(title = "Recommendations")
            Spacer(Modifier.height(AivanceTheme.spacing.small))
            if (data.jobRecommendations.isEmpty()) {
                EmptyStateCard(
                    title = "No recommendations yet",
                    description = "Complete your profile to get personalized job matches.",
                    icon = Icons.Rounded.AutoAwesome
                )
            }
        }

        // Recent Activity
        item {
            SectionHeader(title = "Recent Activity")
            Spacer(Modifier.height(AivanceTheme.spacing.small))
            if (data.recentActivity.isEmpty()) {
                EmptyStateCard(
                    title = "No activity yet",
                    description = "Your actions will appear here once you start using Aivance.",
                    icon = Icons.Rounded.History
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun DashboardContentPreview() {
    AivanceTheme(darkTheme = true) {
        DashboardContent(
            data = DashboardData(
                profileCompletion = 72,
                resumeStatus = ResumeStatus("Resume_2026.pdf", LocalDate.now()),
                atsScore = 85,
                activeApplications = 0,
                interviewPrepStatus = "Ready to Practice"
            ),
            onNavigateToResume = {},
            onNavigateToTracker = {},
            onNavigateToProfile = {},
            onNavigateToInterview = {}
        )
    }
}
