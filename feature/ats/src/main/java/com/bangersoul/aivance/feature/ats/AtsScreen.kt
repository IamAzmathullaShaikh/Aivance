package com.bangersoul.aivance.feature.ats

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import com.bangersoul.aivance.core.designsystem.components.AvianceScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtsScreen(
    viewModel: AtsViewModel,
    onBack: () -> Unit
) {
    AvianceScreen(
        topBar = {
            TopAppBar(
                title = { Text(text = "ATS Analysis") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        Text(text = "ATS Screen")
    }
}
