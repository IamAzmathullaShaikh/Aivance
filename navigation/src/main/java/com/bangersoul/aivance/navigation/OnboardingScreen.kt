package com.bangersoul.aivance.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.bangersoul.aivance.core.designsystem.components.ActionButton
import com.bangersoul.aivance.core.designsystem.components.AivanceScreen
import com.bangersoul.aivance.core.designsystem.theme.DarkAccent
import com.bangersoul.aivance.feature.profile.OnboardingUiEvent
import com.bangersoul.aivance.feature.profile.OnboardingUiState
import com.bangersoul.aivance.feature.profile.OnboardingViewModel
import com.bangersoul.aivance.sdk.core.ConfigField
import com.bangersoul.aivance.sdk.core.FieldType
import com.bangersoul.aivance.sdk.core.ProviderMetadata

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    AnimatedContent(
        targetState = uiState,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "OnboardingTransition"
    ) { state ->
        when (state) {
            OnboardingUiState.Welcome -> WelcomeStep(onStart = { viewModel.onEvent(OnboardingUiEvent.Start) })

            is OnboardingUiState.ChooseAiProvider -> ProviderSelectionStep(
                title = "Choose AI Provider",
                description = "Select the intelligence that will power your resume analysis and interview prep.",
                providers = state.providers,
                onSelect = { viewModel.onEvent(OnboardingUiEvent.SelectAiProvider(it)) }
            )

            is OnboardingUiState.ConfigureAiProvider -> ProviderConfigStep(
                title = "Configure ${state.provider.name}",
                provider = state.provider,
                config = state.config,
                isValidating = state.isValidating,
                error = state.error,
                onUpdate = { k, v -> viewModel.onEvent(OnboardingUiEvent.UpdateAiConfig(k, v)) },
                onValidate = { viewModel.onEvent(OnboardingUiEvent.ValidateAiProvider) },
                onBack = { viewModel.onEvent(OnboardingUiEvent.Back) }
            )

            is OnboardingUiState.ChooseJobProvider -> ProviderSelectionStep(
                title = "Choose Job Provider",
                description = "Select where you want to fetch job listings and recruiter details from.",
                providers = state.providers,
                onSelect = { viewModel.onEvent(OnboardingUiEvent.SelectJobProvider(it)) }
            )

            is OnboardingUiState.ConfigureJobProvider -> ProviderConfigStep(
                title = "Configure ${state.provider.name}",
                provider = state.provider,
                config = state.config,
                isValidating = state.isValidating,
                error = state.error,
                onUpdate = { k, v -> viewModel.onEvent(OnboardingUiEvent.UpdateJobConfig(k, v)) },
                onValidate = { viewModel.onEvent(OnboardingUiEvent.ValidateJobProvider) },
                onBack = { viewModel.onEvent(OnboardingUiEvent.Back) }
            )

            is OnboardingUiState.ChooseEnrichmentProvider -> ProviderSelectionStep(
                title = "Enrichment Service (Optional)",
                description = "Find verified recruiter contacts and company intelligence. Highly recommended for outreach.",
                providers = state.providers,
                onSelect = { viewModel.onEvent(OnboardingUiEvent.SelectEnrichmentProvider(it)) },
                onSkip = { viewModel.onEvent(OnboardingUiEvent.SkipEnrichment) }
            )

            is OnboardingUiState.ConfigureEnrichmentProvider -> ProviderConfigStep(
                title = "Configure ${state.provider.name}",
                provider = state.provider,
                config = state.config,
                isValidating = state.isValidating,
                error = state.error,
                onUpdate = { k, v -> viewModel.onEvent(OnboardingUiEvent.UpdateEnrichmentConfig(k, v)) },
                onValidate = { viewModel.onEvent(OnboardingUiEvent.ValidateEnrichmentProvider) },
                onBack = { viewModel.onEvent(OnboardingUiEvent.Back) }
            )

            is OnboardingUiState.Summary -> OnboardingSummaryStep(
                aiProvider = state.aiProvider,
                jobProvider = state.jobProvider,
                enrichmentProvider = state.enrichmentProvider,
                onFinish = { viewModel.onEvent(OnboardingUiEvent.Finish) }
            )

            OnboardingUiState.Complete -> {
                LaunchedEffect(Unit) { onComplete() }
                Box(Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun WelcomeStep(onStart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Rounded.AutoAwesome, null, Modifier.size(80.dp), tint = DarkAccent)
        Spacer(Modifier.height(32.dp))
        Text("Welcome to Aivance", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(
            "Your AI-powered Career Operating System. Let's get your providers set up to begin.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Get Started", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ProviderSelectionStep(
    title: String,
    description: String,
    providers: List<ProviderMetadata>,
    onSelect: (String) -> Unit,
    onSkip: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(providers) { provider ->
                Card(
                    onClick = { onSelect(provider.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Settings, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(provider.name, fontWeight = FontWeight.Bold)
                            Text(provider.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Rounded.NavigateNext, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        if (onSkip != null) {
            TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
                Text("Skip for now")
            }
        }
    }
}

@Composable
private fun ProviderConfigStep(
    title: String,
    provider: ProviderMetadata,
    config: Map<String, String>,
    isValidating: Boolean,
    error: String?,
    onUpdate: (String, String) -> Unit,
    onValidate: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        provider.configFields.forEach { field ->
            DynamicField(
                field = field,
                value = config[field.key] ?: "",
                onValueChange = { onUpdate(field.key, it) }
            )
            Spacer(Modifier.height(16.dp))
        }

        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onValidate,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !isValidating,
            shape = MaterialTheme.shapes.medium
        ) {
            if (isValidating) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Validate & Continue", fontWeight = FontWeight.Bold)
            }
        }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

@Composable
private fun DynamicField(
    field: ConfigField,
    value: String,
    onValueChange: (String) -> Unit
) {
    val isPassword = field.fieldType == FieldType.PASSWORD
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(field.label) },
        placeholder = { field.hint?.let { Text(it) } },
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            // Password keyboard type + no autocorrect/capitalization so API keys
            // (which contain mixed case, digits and underscores) are typed exactly
            // as entered instead of being silently rewritten by the IME.
            // autoCorrectEnabled is the non-deprecated constructor parameter for
            // Compose foundation 1.7.x (BOM 2024.09.00); autoCorrect is deprecated.
            keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text,
            autoCorrectEnabled = false,
            capitalization = KeyboardCapitalization.None
        ),
        singleLine = true
    )
}

@Composable
private fun OnboardingSummaryStep(
    aiProvider: String,
    jobProvider: String,
    enrichmentProvider: String?,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Rounded.CheckCircle, null, Modifier.size(80.dp), tint = Color(0xFF4CAF50))
        Spacer(Modifier.height(32.dp))
        Text("All Set!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        SummaryItem(label = "AI Engine", value = aiProvider)
        Spacer(Modifier.height(12.dp))
        SummaryItem(label = "Job Source", value = jobProvider)
        Spacer(Modifier.height(12.dp))
        SummaryItem(label = "Enrichment", value = enrichmentProvider ?: "Not Configured")

        Spacer(Modifier.height(48.dp))
        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Go to Dashboard", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                Text(value, fontWeight = FontWeight.Bold)
            }
            Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
        }
    }
}
