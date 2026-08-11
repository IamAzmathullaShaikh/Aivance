package com.bangersoul.aivance.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.rememberNavBackStack
import com.bangersoul.aivance.feature.analytics.AnalyticsScreen
import com.bangersoul.aivance.feature.assistant.AssistantScreen
import com.bangersoul.aivance.feature.assistant.AssistantViewModel
import com.bangersoul.aivance.feature.ats.AtsScreen
import com.bangersoul.aivance.feature.coverletter.CoverLetterScreen
import com.bangersoul.aivance.feature.dashboard.DashboardScreen
import com.bangersoul.aivance.feature.interview.InterviewViewModel
import com.bangersoul.aivance.feature.interview.ui.PrepStudioScreen
import com.bangersoul.aivance.feature.jobs.CompanyDetailScreen
import com.bangersoul.aivance.feature.jobs.CompanyDetailViewModel
import com.bangersoul.aivance.feature.jobs.JobComparisonScreen
import com.bangersoul.aivance.feature.jobs.JobDetailsScreen
import com.bangersoul.aivance.feature.jobs.JobsScreen
import com.bangersoul.aivance.feature.jobs.SavedJobsScreen
import com.bangersoul.aivance.core.common.model.AssistantJobContext
import com.bangersoul.aivance.core.designsystem.shell.LocalAppShellState
import com.bangersoul.aivance.feature.profile.*
import com.bangersoul.aivance.feature.recruiter.RecruiterDashboardScreen
import com.bangersoul.aivance.feature.recruiter.RecruiterViewModel
import com.bangersoul.aivance.feature.resume.IntelligenceHubScreen
import com.bangersoul.aivance.feature.resume.IntelligenceHubViewModel
import com.bangersoul.aivance.feature.resume.ResumeDetailScreen
import com.bangersoul.aivance.feature.resume.ResumeDetailViewModel
import com.bangersoul.aivance.feature.resume.ResumeEngineScreen
import com.bangersoul.aivance.feature.resume.ResumeEngineViewModel
import com.bangersoul.aivance.feature.tracker.TrackerScreen
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Top-level navigation for AiVance v2 — Workflow-Driven.
 *
 * Each primary hub (Dashboard, Intelligence, Discovery, Pipeline, PrepStudio)
 * maintains its own independent backstack to ensure zero progress loss during
 * workspace context switching.
 */
@Composable
fun AivanceNavGraph() {
    val authViewModel: AuthenticationViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val deepLinkDestination = remember { DeepLinkHandler.consumePending() }

    val initialDestination = remember {
        when {
            deepLinkDestination != null -> {
                if (deepLinkDestination.isAuthenticatedDestination() &&
                    authState !is AuthenticationUiState.Authenticated
                ) Destination.Splash else deepLinkDestination
            }
            authState is AuthenticationUiState.Authenticated -> Destination.Dashboard
            else -> Destination.Splash
        }
    }

    AivanceAppShell {
        AivanceWorkflowNavGraph(initialDestination, authViewModel)
    }
}

@Composable
private fun AivanceWorkflowNavGraph(
    initialDestination: Destination,
    authViewModel: AuthenticationViewModel
) {
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val isAuthed = authState is AuthenticationUiState.Authenticated

    // ── Workspace State Management ──────────────────────────────────────────

    // We maintain independent backstacks for each primary workspace hub.
    val backstacks = Destination.rootDestinations.associateWith { root ->
        @Suppress("UNCHECKED_CAST")
        rememberNavBackStack(root) as NavBackStack<Destination>
    }

    // A separate backstack for the non-authenticated/onboarding flow.
    @Suppress("UNCHECKED_CAST")
    val authBackstack = rememberNavBackStack(
        if (initialDestination in Destination.authDestinations) initialDestination else Destination.Splash
    ) as NavBackStack<Destination>

    // The current active root workspace. Defaults to Dashboard.
    var activeWorkspace by remember { mutableStateOf<Destination>(Destination.Dashboard) }

    val currentBackstack = if (isAuthed) {
        backstacks[activeWorkspace] ?: backstacks[Destination.Dashboard]!!
    } else {
        authBackstack
    }

    val currentDestination = currentBackstack.last()

    // ── Navigation Logic ──────────────────────────────────────────────────

    val onNavigate: (Destination) -> Unit = remember(currentBackstack, isAuthed, backstacks, authBackstack) {
        { destination ->
            if (destination.isAuthenticatedDestination() && !isAuthed) {
                authBackstack.add(Destination.Auth)
            } else if (destination in Destination.rootDestinations) {
                activeWorkspace = destination
            } else if (destination in Destination.authDestinations) {
                authBackstack.add(destination)
            } else {
                // ── Workflow-Aware Hub Switching ──────────────────────────────

                val targetWorkspace = when {
                    destination == Destination.Discovery -> Destination.Discovery

                    destination is Destination.Ats ||
                    destination is Destination.ResumeDetail ||
                    destination == Destination.Intelligence ||
                    destination is Destination.ResumeEngine -> Destination.Intelligence

                    destination is Destination.CoverLetter ||
                    destination == Destination.JobComparison ||
                    destination is Destination.RecruiterDashboard -> Destination.Discovery

                    destination == Destination.PrepStudio -> Destination.PrepStudio
                    destination == Destination.Pipeline ||
                    destination is Destination.TrackApplication -> Destination.Pipeline
                    else -> null
                }

                if (targetWorkspace != null && activeWorkspace != targetWorkspace) {
                    activeWorkspace = targetWorkspace
                }

                val updatedBackstack = if (isAuthed) {
                    backstacks[targetWorkspace ?: activeWorkspace] ?: currentBackstack
                } else {
                    authBackstack
                }
                updatedBackstack.add(destination)
            }
        }
    }

    LaunchedEffect(initialDestination) {
        if (initialDestination in Destination.rootDestinations) {
            activeWorkspace = initialDestination
        }
    }

    // ── System Back Logic ────────────────────────────────────────────────

    val isAuthSurface = !isAuthed || currentDestination in Destination.authDestinations
    BackHandler(enabled = currentBackstack.size > 1 || (!isAuthSurface && activeWorkspace != Destination.Dashboard)) {
        if (currentBackstack.size > 1) {
            currentBackstack.removeAt(currentBackstack.lastIndex)
        } else if (!isAuthSurface && activeWorkspace != Destination.Dashboard) {
            activeWorkspace = Destination.Dashboard
        }
    }

    // ── Adaptive UI Shell ────────────────────────────────────────────────

    if (isAuthed && !isAuthSurface) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                Destination.rootDestinations.forEach { workspace ->
                    item(
                        selected = activeWorkspace == workspace,
                        onClick = { activeWorkspace = workspace },
                        icon = {
                            val tint = if (activeWorkspace == workspace)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                            workspace.icon?.let { Icon(it, null, tint = tint) }
                        },
                        label = { Text(stringResource(workspace.labelRes)) }
                    )
                }
            }
        ) {
            NavHostContent(currentBackstack, onNavigate, authViewModel)
        }
    } else {
        NavHostContent(currentBackstack, onNavigate, authViewModel)
    }
}

@Composable
private fun NavHostContent(
    backStack: NavBackStack<Destination>,
    onNavigate: (Destination) -> Unit,
    authViewModel: AuthenticationViewModel
) {
    val currentDestination = if (backStack.isNotEmpty()) backStack.last() else return
    AnimatedContent(
        targetState = currentDestination,
        transitionSpec = {
            (slideInHorizontally { it / 4 } + fadeIn()).togetherWith(slideOutHorizontally { -it / 4 } + fadeOut())
        },
        label = "NavTransition"
    ) { destination ->
        Box(Modifier.fillMaxSize()) {
            ScreenContent(destination, onNavigate, authViewModel) {
                if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
            }
        }
    }
}

@Composable
private fun ScreenContent(
    destination: Destination?,
    onNavigate: (Destination) -> Unit,
    authViewModel: AuthenticationViewModel,
    onBack: () -> Unit
) {
    val shellState = LocalAppShellState.current
    when (destination) {
        Destination.Splash -> {
            val splashScope = rememberCoroutineScope()
            SplashScreen(onSplashComplete = {
                splashScope.launch {
                    val settled = withTimeoutOrNull(3_000L) {
                        authViewModel.uiState
                            .filter { it !is AuthenticationUiState.Loading }
                            .first()
                    }
                    val isAuthed =
                        (settled ?: authViewModel.uiState.value) is AuthenticationUiState.Authenticated
                    onNavigate(if (isAuthed) Destination.Dashboard else Destination.Welcome)
                }
            })
        }
        Destination.Welcome -> WelcomeScreen(
            onGetStarted = { onNavigate(Destination.Auth) },
            onSkip = { onNavigate(Destination.Dashboard) }
        )
        Destination.Auth -> AuthScreen(
            viewModel = hiltViewModel(),
            onNewUser = { onNavigate(Destination.ProviderSetup) },
            onReturningUser = {
                authViewModel.onEvent(AuthenticationUiEvent.CheckAuth)
            },
            onBackToWelcome = { onNavigate(Destination.Welcome) }
        )

        Destination.Onboarding, Destination.ProviderSetup -> OnboardingScreen(
            viewModel = hiltViewModel(),
            onComplete = {
                authViewModel.onEvent(AuthenticationUiEvent.CheckAuth)
            }
        )

        Destination.Dashboard -> DashboardScreen(
            viewModel = hiltViewModel(),
            onNavigateToResume = { onNavigate(Destination.Intelligence) },
            onNavigateToTracker = { onNavigate(Destination.Pipeline) },
            onNavigateToProfile = { onNavigate(Destination.IdentityHub) },
            onNavigateToInterview = { onNavigate(Destination.PrepStudio) },
            onNavigateToAnalytics = { onNavigate(Destination.Analytics) },
            onNavigateToJobs = { onNavigate(Destination.Discovery) },
            onNavigateToAssistant = { onNavigate(Destination.Assistant) },
            onNavigateToNotifications = { onNavigate(Destination.Notifications) }
        )
        Destination.Assistant -> AssistantScreen(
            viewModel = hiltViewModel<AssistantViewModel>(),
            onSwitchProvider = { onNavigate(Destination.ProviderSetup) }
        )
        Destination.Intelligence -> IntelligenceHubScreen(
            viewModel = hiltViewModel<IntelligenceHubViewModel>(),
            onNavigateToEngine = { onNavigate(Destination.ResumeEngine()) },
            onNavigateToAts = { reportId -> onNavigate(Destination.Ats(reportId = reportId)) },
            onBack = onBack
        )
        is Destination.ResumeEngine -> ResumeEngineScreen(
            viewModel = hiltViewModel(),
            initialJobDescription = destination.jobDescription,
            onBack = onBack
        )
        Destination.Discovery -> JobsScreen(
            viewModel = hiltViewModel(),
            onNavigateToDetails = { onNavigate(Destination.JobDetails(it)) },
            onNavigateToSavedJobs = { onNavigate(Destination.SavedJobs) }
        )
        Destination.Pipeline -> TrackerScreen(
            viewModel = hiltViewModel(),
            onBack = onBack
        )
        is Destination.TrackApplication -> TrackerScreen(
            viewModel = hiltViewModel(),
            initialJobId = destination.jobId,
            onBack = onBack
        )

        Destination.IdentityHub -> IdentityHubScreen(
            viewModel = hiltViewModel(),
            onBack = onBack,
            onNavigateToAbout = { onNavigate(Destination.About) },
            onNavigateToResources = { onNavigate(Destination.Resources) },
            onNavigateToProviderManagement = { onNavigate(Destination.ProviderManagement) },
            // Route through the auth ViewModel's full logout: it clears the
            // session, API key AND the onboarding-completed gate (otherwise the
            // next cold start would silently log the user back in), signs out of
            // Firebase, flips auth state to Unauthenticated — which switches the
            // nav graph back to the auth backstack automatically.
            onSignedOut = { authViewModel.onEvent(AuthenticationUiEvent.Logout) }
        )
        Destination.About -> AboutScreen(
            onBack = onBack,
            onNavigateToResources = { onNavigate(Destination.Resources) }
        )
        Destination.Resources -> RemoteResourcesScreen(onBack = onBack)
        Destination.Analytics -> AnalyticsScreen(
            viewModel = hiltViewModel<com.bangersoul.aivance.feature.analytics.AnalyticsViewModel>(),
            onBack = onBack
        )

        Destination.PrepStudio -> PrepStudioScreen(
            interviewViewModel = hiltViewModel<InterviewViewModel>(),
            onBack = onBack
        )
        is Destination.Ats -> AtsScreen(
            viewModel = hiltViewModel(),
            onNavigateBack = onBack,
            initialJobDescription = destination.jobDescription,
            initialReportId = destination.reportId,
            onNavigateToCoverLetter = { onNavigate(Destination.CoverLetter(jobId = null)) }
        )
        is Destination.CoverLetter -> CoverLetterScreen(
            viewModel = hiltViewModel(),
            onNavigateBack = onBack,
            jobId = destination.jobId,
            onFindJobs = { onNavigate(Destination.Discovery) }
        )
        Destination.SavedJobs -> SavedJobsScreen(
            viewModel = hiltViewModel(),
            onBack = onBack,
            onJobClick = { onNavigate(Destination.JobDetails(it)) },
            onCreateResume = { job ->
                onNavigate(Destination.ResumeEngine(jobDescription = job.description))
            },
            onTrackApplication = { job -> onNavigate(Destination.TrackApplication(job.id)) },
            onAssistantForJob = { job ->
                shellState.setAssistantJobContext(
                    AssistantJobContext(
                        jobId = job.id,
                        title = job.title,
                        company = job.company,
                        description = job.description
                    )
                )
                shellState.toggleAssistant(true)
            }
        )
        Destination.JobComparison -> JobComparisonScreen(
            jobs = emptyList(), // In a real app, this would come from a WorkspaceManager or shared VM
            onBack = onBack
        )

        is Destination.JobDetails -> JobDetailsScreen(
            viewModel = hiltViewModel(),
            jobId = destination.jobId,
            onNavigateBack = onBack,
            onNavigateToRecruiters = { onNavigate(Destination.RecruiterDashboard(it)) },
            onNavigateToCoverLetter = { jobId -> onNavigate(Destination.CoverLetter(jobId = jobId)) },
            onNavigateToPipeline = { onNavigate(Destination.Pipeline) },
            onNavigateToAts = { description -> onNavigate(Destination.Ats(jobDescription = description)) }
        )
        is Destination.RecruiterDashboard -> RecruiterDashboardScreen(
            viewModel = hiltViewModel<RecruiterViewModel>(),
            jobId = destination.jobId,
            onBack = onBack
        )
        is Destination.CompanyDetail -> CompanyDetailScreen(
            viewModel = hiltViewModel<CompanyDetailViewModel>(),
            companyId = destination.companyId,
            onBack = onBack,
            onNavigateToRecruiters = { onNavigate(Destination.RecruiterDashboard(it)) }
        )
        is Destination.ResumeDetail -> ResumeDetailScreen(
            viewModel = hiltViewModel<ResumeDetailViewModel>(),
            resumeId = destination.resumeId,
            onBack = onBack
        )

        Destination.Appearance -> AppearanceScreen(viewModel = hiltViewModel(), onBack = onBack)
        Destination.ProviderManagement -> ProviderManagementScreen(viewModel = hiltViewModel(), onBack = onBack)
        Destination.Notifications -> NotificationsScreen(viewModel = hiltViewModel(), onBack = onBack)
        Destination.PrivacyCenter -> PrivacyCenterScreen(
            viewModel = hiltViewModel<PrivacyViewModel>(),
            onBack = onBack
        )

        else -> InvalidRouteScreen(onBack)
    }
}
