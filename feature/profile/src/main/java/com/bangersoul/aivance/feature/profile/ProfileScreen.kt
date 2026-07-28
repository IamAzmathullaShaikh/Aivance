package com.bangersoul.aivance.feature.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.bangersoul.aivance.core.designsystem.components.AvianceScreen

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToInterview: () -> Unit
) {
    AvianceScreen {
        Column {
            Text(text = "Profile Screen")
            Button(onClick = onNavigateToInterview) {
                Text(text = "Practice Interviews")
            }
        }
    }
}
