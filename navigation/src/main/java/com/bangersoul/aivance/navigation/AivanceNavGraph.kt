package com.bangersoul.aivance.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.bangersoul.aivance.feature.interview.InterviewScreen
import com.bangersoul.aivance.feature.interview.InterviewViewModel
import com.bangersoul.aivance.feature.jobs.JobsScreen
import com.bangersoul.aivance.feature.jobs.JobsViewModel
import com.bangersoul.aivance.feature.profile.ProfileScreen
import com.bangersoul.aivance.feature.profile.ProfileViewModel
import com.bangersoul.aivance.feature.resume.ResumeScreen
import com.bangersoul.aivance.feature.resume.ResumeViewModel
import com.bangersoul.aivance.feature.tracker.TrackerScreen
import com.bangersoul.aivance.feature.tracker.TrackerViewModel

@Composable
fun AivanceNavGraph() {
    val backStack = rememberNavBackStack(Destination.Dashboard)
    val currentDestination = backStack.last() as Destination

    val onNavigate: (Destination) -> Unit = remember(backStack) {
        { destination ->
            if (destination in Destination.rootDestinations) {
                // For root destinations, we typically want to swap the root
                while (backStack.isNotEmpty()) {
                    backStack.removeAt(backStack.lastIndex)
                }
                backStack.add(destination)
            } else {
                backStack.add(destination)
            }
        }
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            Destination.rootDestinations.forEach { destination ->
                item(
                    selected = currentDestination == destination,
                    onClick = { onNavigate(destination) },
                    icon = { Icon(destination.icon, contentDescription = destination.label) },
                    label = { Text(destination.label) }
                )
            }
        }
    ) {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                entry<Destination.Dashboard> {
                    val viewModel = hiltViewModel<DashboardViewModel>()
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToResume = { onNavigate(Destination.Resume) },
                        onNavigateToTracker = { onNavigate(Destination.Tracker) },
                        onNavigateToProfile = { onNavigate(Destination.Profile) },
                        onNavigateToInterview = { onNavigate(Destination.Interview) }
                    )
                }
                entry<Destination.Resume> {
                    val viewModel = hiltViewModel<ResumeViewModel>()
                    ResumeScreen(
                        viewModel = viewModel,
                        onNavigateToAts = { onNavigate(Destination.Ats) },
                        onNavigateToCoverLetter = { onNavigate(Destination.CoverLetter) }
                    )
                }
                entry<Destination.Ats> {
                    val viewModel = hiltViewModel<AtsViewModel>()
                    AtsScreen(
                        viewModel = viewModel,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
                entry<Destination.CoverLetter> {
                    val viewModel = hiltViewModel<CoverLetterViewModel>()
                    CoverLetterScreen(
                        viewModel = viewModel,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
                entry<Destination.Interview> {
                    val viewModel = hiltViewModel<InterviewViewModel>()
                    InterviewScreen(
                        viewModel = viewModel,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
                entry<Destination.Jobs> {
                    val viewModel = hiltViewModel<JobsViewModel>()
                    JobsScreen(
                        viewModel = viewModel,
                        onNavigateToTracker = { onNavigate(Destination.Tracker) }
                    )
                }
                entry<Destination.Profile> {
                    val viewModel = hiltViewModel<ProfileViewModel>()
                    ProfileScreen(
                        viewModel = viewModel,
                        onNavigateToInterview = { onNavigate(Destination.Interview) }
                    )
                }
                entry<Destination.Tracker> {
                    val viewModel = hiltViewModel<TrackerViewModel>()
                    TrackerScreen(
                        viewModel = viewModel,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
            }
        )
    }
}
