package com.bangersoul.aivance.feature.resume

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.bangersoul.aivance.core.designsystem.components.AvianceScreen

@Composable
fun ResumeScreen(
    viewModel: ResumeViewModel,
    onNavigateToAts: () -> Unit,
    onNavigateToCoverLetter: () -> Unit
) {
    AvianceScreen {
        Column {
            Text(text = "Resume Screen")
            Button(onClick = onNavigateToAts) {
                Text(text = "Go to ATS Analysis")
            }
            Button(onClick = onNavigateToCoverLetter) {
                Text(text = "Create Cover Letter")
            }
        }
    }
}
