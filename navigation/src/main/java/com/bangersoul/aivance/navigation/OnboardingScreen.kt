package com.bangersoul.aivance.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme
import com.bangersoul.aivance.core.designsystem.theme.DarkAccent
import com.bangersoul.aivance.feature.profile.OnboardingViewModel

/**
 * Multi-step onboarding screen.
 *
 * Uses the existing [OnboardingViewModel] from the :feature:profile module.
 */
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var apiKeyInput by remember { mutableStateOf("") }

    when (uiState) {
        is com.bangersoul.aivance.feature.profile.OnboardingUiState.Welcome -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Rounded.AutoAwesome, contentDescription = null,
                    modifier = Modifier.size(64.dp), tint = DarkAccent
                )
                Spacer(Modifier.height(24.dp))
                Text("Welcome to Aivance!",
                    style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Let's get you set up in just a few steps.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(32.dp))
                Button(onClick = { viewModel.onEvent(com.bangersoul.aivance.feature.profile.OnboardingUiEvent.NextStep) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = MaterialTheme.shapes.medium) {
                    Text("Get Started", fontWeight = FontWeight.SemiBold)
                }
                TextButton(onClick = onSkip) {
                    Text("Skip setup", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        is com.bangersoul.aivance.feature.profile.OnboardingUiState.Permissions -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Rounded.Notifications, null, Modifier.size(64.dp), tint = DarkAccent)
                Spacer(Modifier.height(24.dp))
                Text("Notifications", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Get notified about application updates and interview reminders.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(32.dp))
                Button(onClick = { viewModel.onEvent(com.bangersoul.aivance.feature.profile.OnboardingUiEvent.NextStep) },
                    modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                    Text("Enable Notifications")
                }
                TextButton(onClick = { viewModel.onEvent(com.bangersoul.aivance.feature.profile.OnboardingUiEvent.NextStep) }) {
                    Text("Skip")
                }
            }
        }

        is com.bangersoul.aivance.feature.profile.OnboardingUiState.ProviderSelection -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Rounded.Tune, null, Modifier.size(64.dp), tint = DarkAccent)
                Spacer(Modifier.height(24.dp))
                Text("AI Provider", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Select your preferred AI provider. Gemini is the default.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(32.dp))
                Button(onClick = { viewModel.onEvent(com.bangersoul.aivance.feature.profile.OnboardingUiEvent.NextStep) },
                    modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                    Text("Use Gemini (Default)")
                }
                TextButton(onClick = { viewModel.onEvent(com.bangersoul.aivance.feature.profile.OnboardingUiEvent.NextStep) }) {
                    Text("Skip")
                }
            }
        }

        is com.bangersoul.aivance.feature.profile.OnboardingUiState.ApiKeySetup -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Rounded.Key, null, Modifier.size(64.dp), tint = DarkAccent)
                Spacer(Modifier.height(24.dp))
                Text("API Key", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Enter your Gemini API key to unlock all AI features.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = {
                        apiKeyInput = it
                        viewModel.onEvent(com.bangersoul.aivance.feature.profile.OnboardingUiEvent.SetApiKey(it))
                    },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(24.dp))
                Button(onClick = { viewModel.onEvent(com.bangersoul.aivance.feature.profile.OnboardingUiEvent.NextStep) },
                    modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                    Text("Continue")
                }
                TextButton(onClick = { viewModel.onEvent(com.bangersoul.aivance.feature.profile.OnboardingUiEvent.NextStep) }) {
                    Text("Skip")
                }
            }
        }

        is com.bangersoul.aivance.feature.profile.OnboardingUiState.ProfileSetup -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Rounded.Person, null, Modifier.size(64.dp), tint = DarkAccent)
                Spacer(Modifier.height(24.dp))
                Text("Profile Setup", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("You can always set up your profile later from the Profile tab.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(32.dp))
                Button(onClick = {
                    viewModel.onEvent(com.bangersoul.aivance.feature.profile.OnboardingUiEvent.CompleteOnboarding)
                    onComplete()
                }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                    Text("Complete Setup", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        is com.bangersoul.aivance.feature.profile.OnboardingUiState.Complete -> {
            onComplete()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    AivanceTheme(darkTheme = true) {
        OnboardingScreen(
            viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
            onComplete = {},
            onSkip = {}
        )
    }
}
