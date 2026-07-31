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
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.rememberNavBackStack
import com.bangersoul.aivance.feature.assistant.*
import com.bangersoul.aivance.feature.ats.*
import com.bangersoul.aivance.feature.coverletter.*
import com.bangersoul.aivance.feature.dashboard.*
import com.bangersoul.aivance.feature.interview.*
import com.bangersoul.aivance.feature.jobs.*
import com.bangersoul.aivance.feature.profile.*
import com.bangersoul.aivance.feature.recruiter.*
import com.bangersoul.aivance.feature.resume.*
import com.bangersoul.aivance.feature.tracker.*
import com.bangersoul.aivance.feature.analytics.*

@Composable
fun AivanceNavGraph() {
    val authViewModel: AuthenticationViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val deepLinkDestination = remember { DeepLinkHandler.consumePending() }

    val initialDestination = remember {
        when {
            deepLinkDestination != null -> {
                if (deepLinkDestination in Destination.authenticatedDestinations &&
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
            if (destination in Destination.authenticatedDestinations && !isAuthed) {
                while (backStack.isNotEmpty()) {
                    backStack.removeAt(backStack.lastIndex)
                }
                backStack.add(Destination.Login)
            } else {
                if (destination in Destination.rootDestinations || destination in Destination.authDestinations) {
                    while (backStack.isNotEmpty()) {
                        backStack.removeAt(backStack.lastIndex)
                    }
                }
                backStack.add(destination)
            }
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthenticationUiState.Authenticated && currentDestination in Destination.authDestinations && currentDestination != Destination.Dashboard) {
            onNavigate(Destination.Dashboard)
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
        transitionSpec = { (slideInHorizontally { it / 4 } + fadeIn()).togetherWith(slideOutHorizontally { -it / 4 } + fadeOut()) },
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
        Destination.Splash -> SplashScreen(onSplashComplete = {
            if (authViewModel.uiState.value is AuthenticationUiState.Authenticated) onNavigate(Destination.Dashboard)
            else onNavigate(Destination.Welcome)
        })
        Destination.Welcome -> WelcomeScreen(onGetStarted = { onNavigate(Destination.Login) }, onSkip = { onNavigate(Destination.Dashboard) })
        Destination.Login -> LoginScreen(onLogin = { authViewModel.onEvent(AuthenticationUiEvent.Login(it)) }, onSkip = { onNavigate(Destination.Onboarding) })
        Destination.Onboarding -> OnboardingScreen(viewModel = hiltViewModel(), onComplete = { onNavigate(Destination.Dashboard) })

        Destination.Dashboard -> DashboardScreen(hiltViewModel(), { onNavigate(Destination.Resume) }, { onNavigate(Destination.Tracker) }, { onNavigate(Destination.Profile) }, { onNavigate(Destination.Interview) }, { onNavigate(Destination.AnalyticsDashboard) }, { onNavigate(Destination.Jobs) })
        Destination.Assistant -> AssistantScreen(hiltViewModel<AssistantViewModel>(), { onNavigate(Destination.ProviderManagement) })
        Destination.Resume -> ResumeScreen(hiltViewModel(), { onNavigate(Destination.Ats) }, { onNavigate(Destination.CoverLetter) })
        Destination.Tracker -> TrackerScreen(hiltViewModel(), onBack)
        Destination.Jobs -> JobsScreen(hiltViewModel()) { onNavigate(Destination.JobDetails(it)) }
        Destination.Profile -> ProfileScreen(hiltViewModel(), { onNavigate(Destination.Interview) }, { onNavigate(Destination.Settings) }, { onNavigate(Destination.AiSettings) }, { onNavigate(Destination.ProviderManagement) }, { onNavigate(Destination.Notifications) }, { onNavigate(Destination.AnalyticsDashboard) }, { onNavigate(Destination.CareerRoadmap) }, { onNavigate(Destination.LearningHub) }, { onNavigate(Destination.SavedJobs) }, { onNavigate(Destination.AiChat) })

        Destination.Ats -> AtsScreen(hiltViewModel(), onBack)
        Destination.CoverLetter -> CoverLetterScreen(hiltViewModel(), onBack)
        Destination.Interview -> InterviewScreen(hiltViewModel(), onBack)
        Destination.AiChat -> AiChatScreen(hiltViewModel(), onBack)
        is Destination.JobDetails -> JobDetailsScreen(hiltViewModel(), onBack)
        is Destination.RecruiterDashboard -> RecruiterDashboardScreen(hiltViewModel<RecruiterViewModel>(), onBack)
        Destination.SavedJobs -> SavedJobsScreen(hiltViewModel(), onBack) { onNavigate(Destination.JobDetails(it)) }
        Destination.Settings -> SettingsScreen(hiltViewModel(), onBack, { onNavigate(Destination.AiSettings) }, { onNavigate(Destination.ProviderManagement) }, { onNavigate(Destination.AnalyticsDashboard) }, { onNavigate(Destination.PrivacyCenter) }, { onNavigate(Destination.Appearance) })
        Destination.Appearance -> AppearanceScreen(hiltViewModel(), onBack)
        Destination.AiSettings -> AiSettingsScreen(hiltViewModel(), onBack)
        Destination.ProviderManagement -> ProviderManagementScreen(hiltViewModel(), onBack)
        Destination.Notifications -> NotificationsScreen(hiltViewModel(), onBack)
        Destination.AnalyticsDashboard -> AnalyticsScreen(hiltViewModel<AnalyticsViewModel>(), onBack)
        Destination.PrivacyCenter -> PrivacyCenterScreen(hiltViewModel<PrivacyViewModel>(), onBack)
        Destination.CareerRoadmap -> CareerRoadmapScreen(hiltViewModel(), onBack)
        Destination.LearningHub -> LearningHubScreen(hiltViewModel(), onBack)
        else -> InvalidRouteScreen(onBack)
    }
}
