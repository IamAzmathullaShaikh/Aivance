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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.bangersoul.aivance.feature.ats.AtsScreen
import com.bangersoul.aivance.feature.ats.AtsViewModel
import com.bangersoul.aivance.feature.coverletter.CoverLetterScreen
import com.bangersoul.aivance.feature.coverletter.CoverLetterViewModel
import com.bangersoul.aivance.feature.dashboard.DashboardScreen
import com.bangersoul.aivance.feature.dashboard.DashboardViewModel
import com.bangersoul.aivance.feature.interview.AiChatScreen
import com.bangersoul.aivance.feature.interview.AiChatViewModel
import com.bangersoul.aivance.feature.interview.InterviewScreen
import com.bangersoul.aivance.feature.interview.InterviewViewModel
import com.bangersoul.aivance.feature.jobs.JobDetailsScreen
import com.bangersoul.aivance.feature.jobs.JobDetailsViewModel
import com.bangersoul.aivance.feature.jobs.JobsScreen
import com.bangersoul.aivance.feature.jobs.JobsViewModel
import com.bangersoul.aivance.feature.jobs.SavedJobsScreen
import com.bangersoul.aivance.feature.jobs.SavedJobsViewModel
import com.bangersoul.aivance.feature.profile.*
import com.bangersoul.aivance.feature.resume.ResumeScreen
import com.bangersoul.aivance.feature.resume.ResumeViewModel
import com.bangersoul.aivance.feature.tracker.TrackerScreen
import com.bangersoul.aivance.feature.tracker.TrackerViewModel

/**
 * Root composable that drives the entire navigation tree.
 *
 * Uses Navigation 3's [rememberNavBackStack] for type-safe state-driven navigation
 * and [NavigationSuiteScaffold] for adaptive bottom-bar / navigation rail.
 *
 * Supported phases:
 * 1. **Auth flow** – Splash → Welcome → Login/Onboarding → Dashboard
 * 2. **Main app** – Bottom-nav screens + pushed detail screens
 * 3. **Deep links** – Parameterized routes (aivance://jobs/{id}, etc.)
 */
@Composable
fun AivanceNavGraph() {
    // Single auth ViewModel instance shared across all auth screens
    val authViewModel: AuthenticationViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    // Check for deep link from cold start
    val deepLinkDestination = remember { DeepLinkHandler.consumePending() }

    val initialDestination: Destination = when {
        deepLinkDestination != null -> deepLinkDestination
        authState is AuthenticationUiState.Authenticated -> Destination.Dashboard
        else -> Destination.Splash
    }

    AivanceAppShell(
        isAuthenticated = authState is AuthenticationUiState.Authenticated
    ) {
        AivanceMainNavGraph(
            initialDestination = initialDestination,
            authViewModel = authViewModel
        )
    }
}

/**
 * Inner navigation graph — separated from the shell so the Scaffold in
 * [AivanceAppShell] wraps the entire navigation area.
 */
@Composable
private fun AivanceMainNavGraph(
    initialDestination: Destination,
    authViewModel: AuthenticationViewModel
) {
    val backStack = rememberNavBackStack(initialDestination)
    val currentDestination = if (backStack.isNotEmpty()) {
        backStack.last() as Destination
    } else initialDestination

    val onNavigate: (Destination) -> Unit = remember(backStack) {
        { destination ->
            when {
                destination in Destination.rootDestinations || destination in Destination.authDestinations -> {
                    // Root/auth destinations: clear backstack and swap
                    while (backStack.isNotEmpty()) {
                        backStack.removeAt(backStack.lastIndex)
                    }
                    backStack.add(destination)
                }
                else -> {
                    // Detail screens: push onto backstack
                    backStack.add(destination)
                }
            }
        }
    }

    // Reactively navigate to Dashboard once authenticated
    val authState by authViewModel.uiState.collectAsState()
    LaunchedEffect(authState, currentDestination) {
        if (authState is AuthenticationUiState.Authenticated &&
            currentDestination in Destination.authDestinations &&
            currentDestination != Destination.Dashboard
        ) {
            onNavigate(Destination.Dashboard)
        }
    }

    val isRootDestination = currentDestination in Destination.rootDestinations

    if (isRootDestination && currentDestination is Destination) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                Destination.rootDestinations.forEach { destination ->
                    item(
                        selected = currentDestination == destination,
                        onClick = { onNavigate(destination) },
                        icon = {
                            destination.icon?.let {
                                Icon(it, contentDescription = destination.label)
                            }
                        },
                        label = { Text(destination.label) }
                    )
                }
            }
        ) {
            NavHostContent(
                backStack = backStack,
                onNavigate = onNavigate,
                authViewModel = authViewModel
            )
        }
    } else {
        NavHostContent(
            backStack = backStack,
            onNavigate = onNavigate,
            authViewModel = authViewModel
        )
    }
}

/**
 * Renders the current navigation entry with animated transitions.
 */
@Composable
private fun NavHostContent(
    backStack: androidx.navigation3.runtime.NavBackStack<Destination>,
    onNavigate: (Destination) -> Unit,
    authViewModel: AuthenticationViewModel
) {
    val currentDestination = if (backStack.isNotEmpty()) {
        backStack.last() as Destination
    } else return

    AnimatedContent(
        targetState = currentDestination,
        transitionSpec = {
            (slideInHorizontally { it / 4 } + fadeIn())
                .togetherWith(slideOutHorizontally { -it / 4 } + fadeOut())
        },
        label = "NavTransition"
    ) { destination ->
        Box(modifier = Modifier.fillMaxSize()) {
            ScreenContent(
                destination = destination,
                onNavigate = onNavigate,
                authViewModel = authViewModel,
                onBack = {
                    if (backStack.size > 1) {
                        backStack.removeLastOrNull()
                    }
                }
            )
        }
    }
}

/**
 * Maps each [Destination] to its corresponding screen composable.
 *
 * ViewModels are resolved via Hilt's [hiltViewModel] through Navigation 3's
 * [rememberViewModelStoreNavEntryDecorator] (applied in NavDisplay entries).
 */
@Suppress("UNUSED_PARAMETER")
@Composable
private fun ScreenContent(
    destination: Destination?,
    onNavigate: (Destination) -> Unit,
    authViewModel: AuthenticationViewModel,
    onBack: () -> Unit
) {
    when (destination) {
        // ── Auth Flow ─────────────────────────────────
        is Destination.Splash -> {
            SplashScreen(
                onSplashComplete = {
                    val state = authViewModel.uiState.value
                    if (state is AuthenticationUiState.Authenticated) {
                        onNavigate(Destination.Dashboard)
                    } else {
                        onNavigate(Destination.Welcome)
                    }
                }
            )
        }

        is Destination.Welcome -> {
            WelcomeScreen(
                onGetStarted = { onNavigate(Destination.Login) },
                onSkip = { onNavigate(Destination.Dashboard) }
            )
        }

        is Destination.Login -> {
            // Use the shared authViewModel — do NOT create a second instance
            LoginScreen(
                onLogin = { apiKey ->
                    authViewModel.onEvent(AuthenticationUiEvent.Login(apiKey))
                },
                onSkip = { onNavigate(Destination.Onboarding) }
            )
        }

        is Destination.Onboarding -> {
            val onboardingVm: OnboardingViewModel = hiltViewModel()
            OnboardingScreen(
                viewModel = onboardingVm,
                onComplete = { onNavigate(Destination.Dashboard) },
                onSkip = { onNavigate(Destination.Dashboard) }
            )
        }

        // ── Root Destinations ────────────────────────
        is Destination.Dashboard -> {
            val vm: DashboardViewModel = hiltViewModel()
            DashboardScreen(
                viewModel = vm,
                onNavigateToResume = { onNavigate(Destination.Resume) },
                onNavigateToTracker = { onNavigate(Destination.Tracker) },
                onNavigateToProfile = { onNavigate(Destination.Profile) },
                onNavigateToInterview = { onNavigate(Destination.Interview) }
            )
        }

        is Destination.Resume -> {
            val vm: ResumeViewModel = hiltViewModel()
            ResumeScreen(
                viewModel = vm,
                onNavigateToAts = { onNavigate(Destination.Ats) },
                onNavigateToCoverLetter = { onNavigate(Destination.CoverLetter) }
            )
        }

        is Destination.Tracker -> {
            val vm: TrackerViewModel = hiltViewModel()
            TrackerScreen(viewModel = vm, onBack = onBack)
        }

        is Destination.Jobs -> {
            val vm: JobsViewModel = hiltViewModel()
            JobsScreen(
                viewModel = vm,
                onNavigateToTracker = { onNavigate(Destination.Tracker) },
                onNavigateToJobDetails = { jobId -> onNavigate(Destination.JobDetails(jobId)) }
            )
        }

        is Destination.Profile -> {
            val vm: ProfileViewModel = hiltViewModel()
            ProfileScreen(
                viewModel = vm,
                onNavigateToInterview = { onNavigate(Destination.Interview) },
                onNavigateToSettings = { onNavigate(Destination.Settings) },
                onNavigateToAiSettings = { onNavigate(Destination.AiSettings) },
                onNavigateToProviders = { onNavigate(Destination.ProviderManagement) },
                onNavigateToNotifications = { onNavigate(Destination.Notifications) },
                onNavigateToAnalytics = { onNavigate(Destination.AnalyticsDashboard) },
                onNavigateToRoadmap = { onNavigate(Destination.CareerRoadmap) },
                onNavigateToLearning = { onNavigate(Destination.LearningHub) },
                onNavigateToSavedJobs = { onNavigate(Destination.SavedJobs) },
                onNavigateToAiChat = { onNavigate(Destination.AiChat) }
            )
        }

        // ── Detail Screens ───────────────────────────
        is Destination.Ats -> {
            val vm: AtsViewModel = hiltViewModel()
            AtsScreen(viewModel = vm, onBack = onBack)
        }

        is Destination.CoverLetter -> {
            val vm: CoverLetterViewModel = hiltViewModel()
            CoverLetterScreen(viewModel = vm, onBack = onBack)
        }

        is Destination.Interview -> {
            val vm: InterviewViewModel = hiltViewModel()
            InterviewScreen(viewModel = vm, onBack = onBack)
        }

        is Destination.AiChat -> {
            val vm: AiChatViewModel = hiltViewModel()
            AiChatScreen(viewModel = vm, onBack = onBack)
        }

        is Destination.JobDetails -> {
            val vm: JobDetailsViewModel = hiltViewModel()
            JobDetailsScreen(viewModel = vm, onBack = onBack)
        }

        is Destination.SavedJobs -> {
            val vm: SavedJobsViewModel = hiltViewModel()
            SavedJobsScreen(
                viewModel = vm,
                onBack = onBack,
                onJobClick = { jobId -> onNavigate(Destination.JobDetails(jobId)) }
            )
        }

        is Destination.Settings -> {
            val vm: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = vm,
                onBack = onBack,
                onNavigateToAiSettings = { onNavigate(Destination.AiSettings) },
                onNavigateToProviders = { onNavigate(Destination.ProviderManagement) },
                onNavigateToAnalytics = { onNavigate(Destination.AnalyticsDashboard) }
            )
        }

        is Destination.AiSettings -> {
            val vm: AiSettingsViewModel = hiltViewModel()
            AiSettingsScreen(viewModel = vm, onBack = onBack)
        }

        is Destination.ProviderManagement -> {
            val vm: ProviderManagementViewModel = hiltViewModel()
            ProviderManagementScreen(viewModel = vm, onBack = onBack)
        }

        is Destination.Notifications -> {
            val vm: NotificationsViewModel = hiltViewModel()
            NotificationsScreen(viewModel = vm, onBack = onBack)
        }

        is Destination.AnalyticsDashboard -> {
            val vm: AnalyticsDashboardViewModel = hiltViewModel()
            AnalyticsDashboardScreen(viewModel = vm, onBack = onBack)
        }

        is Destination.CareerRoadmap -> {
            val vm: CareerRoadmapViewModel = hiltViewModel()
            CareerRoadmapScreen(viewModel = vm, onBack = onBack)
        }

        is Destination.LearningHub -> {
            val vm: LearningHubViewModel = hiltViewModel()
            LearningHubScreen(viewModel = vm, onBack = onBack)
        }

        // ── Fallback ─────────────────────────────────
        null -> InvalidRouteScreen(onBack = onBack)
    }
}
