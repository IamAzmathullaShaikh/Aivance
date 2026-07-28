package com.bangersoul.aivance.feature.jobs

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.bangersoul.aivance.core.designsystem.components.AvianceScreen

@Composable
fun JobsScreen(
    viewModel: JobsViewModel,
    onNavigateToTracker: () -> Unit
) {
    AvianceScreen {
        Column {
            Text(text = "Jobs Screen")
            Button(onClick = onNavigateToTracker) {
                Text(text = "Go to Application Tracker")
            }
        }
    }
}
