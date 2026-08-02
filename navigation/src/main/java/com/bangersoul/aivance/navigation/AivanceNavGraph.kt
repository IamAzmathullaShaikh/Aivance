package com.bangersoul.aivance.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
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
import com.bangersoul.aivance.feature.interview.AiChatScreen
import com.bangersoul.aivance.feature.interview.InterviewScreen
import com.bangersoul.aivance.feature.interview.InterviewViewModel
import com.bangersoul.aivance.feature.jobs.CompanyDetailScreen
import com.bangersoul.aivance.feature.jobs.CompanyDetailViewModel
import com.bangersoul.aivance.feature.jobs.JobDetailsScreen
import com.bangersoul.aivance.feature.jobs.JobsScreen
import com.bangersoul.aivance.feature.jobs.SavedJobsScreen
import com.bangersoul.aivance.feature.profile.*
import com.bangersoul.aivance.feature.recruiter.RecruiterDashboardScreen
import com.bangersoul.aivance.feature.recruiter.RecruiterViewModel
import com.bangersoul.aivance.feature.resume.ResumeDetailScreen
import com.bangersoul.aivance.feature.resume.ResumeDetailViewModel
import com.bangersoul.aivance.feature.resume.ResumeEngineScreen
import com.bangersoul.aivance.feature.tracker.TrackerScreen

/**
 * Top-level navigation for AiVance v2.
 *
 * Two independent routing surfaces:
 *  1. Auth graph — Splash → Welcome → Auth → ProviderSetup (no bottom nav).
 *  2. Main graph — the 5-tab Career Operating System loop
 *     (Dashboard · Assistant · Resume · Jobs · Pipeline) plus detail screens.
 *
 * The app uses Navigation-3-lite: a single typed [NavBackStack] with manual
 * AnimatedContent dispatch. State is preserved across tab switches because
 * ViewModels are scoped to the activity and the back stack only rebuilds on
 * actual destination changes.
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
        AivanceMainNavGraph(initialDestination, authViewModel)
    }
}

@Composable
private fun AivanceMainNavGraph(
    initialDestination: Destination,
    authViewModel: AuthenticationViewModel
) {
    @Suppress("UNCHECKED_CAST")
    val backStack = rememberNavBackStack(initialDestination) as NavBackStack<Destination>
    val currentDestination = if (backStack.isNotEmpty()) backStack.last() else initialDestination
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    val onNavigate: (Destination) -> Unit = remember(backStack, authState) {
        { destination ->
            val isAuthed = authState is AuthenticationUiState.Authenticated
            if (destination.isAuthenticatedDestination() && !isAuthed) {
                // Protected destination while signed out → drop everything and
                // show the auth flow.
                while (backStack.isNotEmpty()) {
                    backStack.removeAt(backStack.lastIndex)
                }
                backStack.add(Destination.Auth)
            } else {
                // Root tabs and auth-graph destinations replace the stack so
                // back never returns to a stale screen (e.g. Dashboard → Login).
                if (destination in Destination.rootDestinations ||
                    destination in Destination.authDestinations
                ) {
                    while (backStack.isNotEmpty()) {
                        backStack.removeAt(backStack.lastIndex)
                    }
                }
                backStack.add(destination)
            }
        }
    }

    // Route the user into the correct surface whenever the auth state changes.
    LaunchedEffect(authState) {
        val authed = authState is AuthenticationUiState.Authenticated
        if (authed && currentDestination in Destination.authDestinations &&
            currentDestination != Destination.Dashboard
        ) {
            onNavigate(Destination.Dashboard)
        } else if (!authed && currentDestination.isAuthenticatedDestination()) {
            onNavigate(Destination.Auth)
        }
    }

    if (currentDestination in Destination.rootDestinations) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                Destination.rootDestinations.forEach { destination ->
                    item(
                        selected = currentDestination == destination,
                        onClick = { onNavigate(destination) },
                        icon = { destination.icon?.let { Icon(it, null) } },
                        label = { Text(destination.label) }
                    )
                }
            }
        ) {
            NavHostContent(backStack, onNavigate, authViewModel)
        }
    } else {
        NavHostContent(backStack, onNavigate, authViewModel)
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
                if (backStack.size > 1) backStack.removeLastOrNull()
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
    when (destination) {
        // ── Auth graph ────────────────────────────────────────────────
        Destination.Splash -> {
            val splashScope = rememberCoroutineScope()
            SplashScreen(onSplashComplete = {
                // P0: never hang on a slow session check. Wait up to 3s for the
                // auth state to settle; on timeout, route to Welcome anyway.
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
                // Re-read prefs so the auth guard sees the freshly persisted
                // session; the LaunchedEffect(authState) below routes to the
                // Dashboard once Authenticated is set. (Direct navigation would
                // bounce off the authenticated-destination guard.)
                authViewModel.onEvent(AuthenticationUiEvent.CheckAuth)
            },
            onBackToWelcome = { onNavigate(Destination.Welcome) }
        )
        Destination.ProviderSetup -> OnboardingScreen(
            viewModel = hiltViewModel(),
            onComplete = {
                // Re-read prefs so the auth guard sees the freshly persisted
                // session; LaunchedEffect(authState) then routes to Dashboard.
                authViewModel.onEvent(AuthenticationUiEvent.CheckAuth)
            }
        )

        // Legacy auth destinations kept for backward compatibility.
        Destination.Login -> LoginScreen(
            onLogin = { authViewModel.onEvent(AuthenticationUiEvent.Login(it)) },
            onSkip = { onNavigate(Destination.Onboarding) }
        )
        Destination.Onboarding -> OnboardingScreen(
            viewModel = hiltViewModel(),
            onComplete = { authViewModel.onEvent(AuthenticationUiEvent.CheckAuth) }
        )

        // ── Main graph: root tabs ─────────────────────────────────────
        Destination.Dashboard -> DashboardScreen(
            viewModel = hiltViewModel(),
            onNavigateToResume = { onNavigate(Destination.Resume) },
            onNavigateToTracker = { onNavigate(Destination.Pipeline) },
            onNavigateToProfile = { onNavigate(Destination.Profile) },
            onNavigateToInterview = { onNavigate(Destination.PrepStudio) },
            onNavigateToAnalytics = { onNavigate(Destination.Analytics) },
            onNavigateToJobs = { onNavigate(Destination.Jobs) },
            onNavigateToAssistant = { onNavigate(Destination.Assistant) },
            onNavigateToNotifications = { onNavigate(Destination.Notifications) }
        )
        Destination.Assistant -> AssistantScreen(
            viewModel = hiltViewModel<AssistantViewModel>(),
            onSwitchProvider = { onNavigate(Destination.ProviderSetup) }
        )
        Destination.Resume -> ResumeEngineScreen(
            viewModel = hiltViewModel(),
            onBack = onBack
        )
        Destination.Jobs -> JobsScreen(
            viewModel = hiltViewModel(),
            onNavigateToDetails = { onNavigate(Destination.JobDetails(it)) },
            onNavigateToSavedJobs = { onNavigate(Destination.SavedJobs) }
        )
        Destination.Pipeline -> TrackerScreen(
            viewModel = hiltViewModel(),
            onBack = onBack
        )

        // ── Main graph: profile & settings hub ────────────────────────
        Destination.Profile -> ProfileScreen(
            viewModel = hiltViewModel(),
            onBack = onBack,
            onNavigateToSettings = { onNavigate(Destination.SettingsHub) },
            onNavigateToAiSettings = { onNavigate(Destination.AiSettings) },
            onNavigateToProviders = { onNavigate(Destination.ProviderManagement) },
            onNavigateToNotifications = { onNavigate(Destination.Notifications) },
            onNavigateToAnalytics = { onNavigate(Destination.Analytics) },
            onNavigateToRoadmap = { onNavigate(Destination.CareerRoadmap) },
            onNavigateToLearning = { onNavigate(Destination.PrepStudio) },
            onNavigateToSavedJobs = { onNavigate(Destination.SavedJobs) },
            onNavigateToAiChat = { onNavigate(Destination.AiChat) },
            onNavigateToInterview = { onNavigate(Destination.PrepStudio) }
        )
        Destination.SettingsHub -> SettingsScreen(
            viewModel = hiltViewModel(),
            onBack = onBack,
            onNavigateToAiSettings = { onNavigate(Destination.AiSettings) },
            onNavigateToProviders = { onNavigate(Destination.ProviderManagement) },
            onNavigateToAnalytics = { onNavigate(Destination.Analytics) },
            onNavigateToPrivacy = { onNavigate(Destination.PrivacyCenter) },
            onNavigateToAppearance = { onNavigate(Destination.Appearance) },
            // Logout sets Unauthenticated directly — CheckAuth would still see
            // onboardingCompleted == true and keep the user authenticated.
            onSignOut = { authViewModel.onEvent(AuthenticationUiEvent.Logout) }
        )
        Destination.Analytics -> AnalyticsScreen(
            viewModel = hiltViewModel<com.bangersoul.aivance.feature.analytics.AnalyticsViewModel>(),
            onBack = onBack
        )

        // ── Main graph: intelligence engines ──────────────────────────
        Destination.PrepStudio -> PrepStudioScreen(
            interviewViewModel = hiltViewModel<InterviewViewModel>(),
            onBack = onBack
        )
        Destination.Ats -> AtsScreen(
            viewModel = hiltViewModel(),
            onNavigateBack = onBack,
            onNavigateToCoverLetter = { onNavigate(Destination.CoverLetter) }
        )
        Destination.CoverLetter -> CoverLetterScreen(
            viewModel = hiltViewModel(),
            onNavigateBack = onBack,
            onFindJobs = { onNavigate(Destination.Jobs) }
        )
        Destination.Interview -> InterviewScreen(viewModel = hiltViewModel(), onBack = onBack)
        Destination.AiChat -> AiChatScreen(viewModel = hiltViewModel(), onBack = onBack)
        Destination.SavedJobs -> SavedJobsScreen(
            viewModel = hiltViewModel(),
            onBack = onBack,
            onJobClick = { onNavigate(Destination.JobDetails(it)) }
        )

        // ── Main graph: detail screens ────────────────────────────────
        is Destination.JobDetails -> JobDetailsScreen(
            viewModel = hiltViewModel(),
            jobId = destination.jobId,
            onNavigateBack = onBack,
            onNavigateToRecruiters = { onNavigate(Destination.RecruiterDashboard(it)) },
            onNavigateToCoverLetter = { onNavigate(Destination.CoverLetter) },
            onNavigateToPipeline = { onNavigate(Destination.Pipeline) },
            onNavigateToAts = { onNavigate(Destination.Ats) }
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

        // ── Settings sub-screens (reached from SettingsHub) ───────────
        Destination.Appearance -> AppearanceScreen(viewModel = hiltViewModel(), onBack = onBack)
        Destination.AiSettings -> AiSettingsScreen(viewModel = hiltViewModel(), onBack = onBack)
        Destination.ProviderManagement -> ProviderManagementScreen(viewModel = hiltViewModel(), onBack = onBack)
        Destination.Notifications -> NotificationsScreen(viewModel = hiltViewModel(), onBack = onBack)
        Destination.PrivacyCenter -> PrivacyCenterScreen(
            viewModel = hiltViewModel<PrivacyViewModel>(),
            onBack = onBack
        )

        // ── Legacy/secondary destinations ─────────────────────────────
        Destination.Tracker -> TrackerScreen(viewModel = hiltViewModel(), onBack = onBack)
        Destination.Settings -> SettingsScreen(
            viewModel = hiltViewModel(),
            onBack = onBack,
            onNavigateToAiSettings = { onNavigate(Destination.AiSettings) },
            onNavigateToProviders = { onNavigate(Destination.ProviderManagement) },
            onNavigateToAnalytics = { onNavigate(Destination.Analytics) },
            onNavigateToPrivacy = { onNavigate(Destination.PrivacyCenter) },
            onNavigateToAppearance = { onNavigate(Destination.Appearance) },
            onSignOut = { authViewModel.onEvent(AuthenticationUiEvent.Logout) }
        )
        Destination.AnalyticsDashboard -> AnalyticsScreen(
            viewModel = hiltViewModel<com.bangersoul.aivance.feature.analytics.AnalyticsViewModel>(),
            onBack = onBack
        )
        Destination.CareerRoadmap -> CareerRoadmapScreen(viewModel = hiltViewModel(), onBack = onBack)
        Destination.LearningHub -> LearningHubScreen(viewModel = hiltViewModel(), onBack = onBack)

        else -> InvalidRouteScreen(onBack)
    }
}
