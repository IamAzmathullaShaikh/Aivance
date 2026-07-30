package com.bangersoul.aivance.feature.coverletter

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bangersoul.aivance.core.designsystem.components.ActionButton
import com.bangersoul.aivance.core.designsystem.components.AivanceScreen
import com.bangersoul.aivance.core.designsystem.components.DashboardCard
import com.bangersoul.aivance.core.designsystem.components.AivanceError
import com.bangersoul.aivance.core.designsystem.components.AivanceLoading
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme
import com.bangersoul.aivance.core.designsystem.theme.DarkAccent
import com.bangersoul.aivance.core.designsystem.theme.Zinc800
import com.bangersoul.aivance.core.designsystem.theme.Zinc900
import com.bangersoul.aivance.feature.coverletter.domain.model.LetterTone
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoverLetterScreen(
    viewModel: CoverLetterViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val resumeText by viewModel.resumeText.collectAsState()
    val jobDescription by viewModel.jobDescription.collectAsState()
    val selectedTone by viewModel.selectedTone.collectAsState()
    val scrollState = rememberScrollState()

    AivanceScreen(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cover Letter AI",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (uiState is CoverLetterUiState.Success) {
                        IconButton(onClick = { viewModel.onEvent(CoverLetterUiEvent.Reset) }) {
                            Icon(Icons.Rounded.Refresh, contentDescription = "Reset")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) {
        AnimatedContent(
            targetState = uiState,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "CoverLetterStateTransition"
        ) { state ->
            when (state) {
                is CoverLetterUiState.Idle -> {
                    CoverLetterInputForm(
                        resumeText = resumeText,
                        onUpdateResume = viewModel::updateResumeText,
                        jobDescription = jobDescription,
                        onUpdateJobDescription = viewModel::updateJobDescription,
                        selectedTone = selectedTone,
                        onUpdateTone = viewModel::updateTone,
                        onGenerate = { viewModel.onEvent(CoverLetterUiEvent.Generate("", "", "", selectedTone)) },
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(AivanceTheme.spacing.medium)
                    )
                }

                is CoverLetterUiState.Generating -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        AivanceLoading()
                        Spacer(modifier = Modifier.height(AivanceTheme.spacing.medium))
                        Text(
                            text = "AI is crafting your cover letter...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                is CoverLetterUiState.Success -> {
                    CoverLetterResult(
                        content = state.content,
                        onCopy = { viewModel.onEvent(CoverLetterUiEvent.CopyToClipboard) },
                        onSave = { viewModel.onEvent(CoverLetterUiEvent.CopyToClipboard) },
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(AivanceTheme.spacing.medium)
                    )
                }

                is CoverLetterUiState.Error -> {
                    AivanceError(
                        message = state.message,
                        onRetry = { viewModel.onEvent(CoverLetterUiEvent.Generate("", "", "", selectedTone)) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CoverLetterInputForm(
    resumeText: String,
    onUpdateResume: (String) -> Unit,
    jobDescription: String,
    onUpdateJobDescription: (String) -> Unit,
    selectedTone: LetterTone,
    onUpdateTone: (LetterTone) -> Unit,
    onGenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AivanceTheme.spacing.large)
    ) {
        DashboardCard {
            Column(
                modifier = Modifier.padding(AivanceTheme.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(AivanceTheme.spacing.medium)
            ) {
                Text(
                    text = "Application Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = resumeText,
                    onValueChange = onUpdateResume,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Resume / Experience") },
                    placeholder = { Text("Paste your resume content...") },
                    leadingIcon = {
                        Icon(Icons.Rounded.Description, contentDescription = null)
                    },
                    minHeight = 120.dp,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkAccent,
                        unfocusedBorderColor = Zinc800,
                        focusedContainerColor = Zinc900,
                        unfocusedContainerColor = Zinc900
                    )
                )

                OutlinedTextField(
                    value = jobDescription,
                    onValueChange = onUpdateJobDescription,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Job Description") },
                    placeholder = { Text("Paste the job description...") },
                    leadingIcon = {
                        Icon(Icons.Rounded.HistoryEdu, contentDescription = null)
                    },
                    minHeight = 120.dp,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkAccent,
                        unfocusedBorderColor = Zinc800,
                        focusedContainerColor = Zinc900,
                        unfocusedContainerColor = Zinc900
                    )
                )
            }
        }

        DashboardCard {
            Column(
                modifier = Modifier.padding(AivanceTheme.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(AivanceTheme.spacing.small)
            ) {
                Text(
                    text = "Select Tone",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(AivanceTheme.spacing.small),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LetterTone.entries.forEach { tone ->
                        FilterChip(
                            selected = selectedTone == tone,
                            onClick = { onUpdateTone(tone) },
                            label = { 
                                Text(tone.name.lowercase().replaceFirstChar { 
                                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
                                }) 
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DarkAccent,
                                selectedLabelColor = Color.Black,
                                containerColor = Zinc800,
                                labelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedTone == tone,
                                borderColor = Zinc800,
                                selectedBorderColor = DarkAccent
                            )
                        )
                    }
                }
            }
        }

        ActionButton(
            text = "Generate Cover Letter",
            onClick = onGenerate,
            icon = Icons.Rounded.AutoAwesome,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CoverLetterResult(
    content: String,
    onCopy: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AivanceTheme.spacing.large)
    ) {
        DashboardCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(AivanceTheme.spacing.medium)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Generated Content",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row {
                        IconButton(onClick = onCopy) {
                            Icon(
                                imageVector = Icons.Rounded.ContentCopy,
                                contentDescription = "Copy",
                                tint = DarkAccent
                            )
                        }
                        IconButton(onClick = onSave) {
                            Icon(
                                imageVector = Icons.Rounded.Save,
                                contentDescription = "Save",
                                tint = DarkAccent
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(AivanceTheme.spacing.medium))
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AivanceTheme.spacing.medium)
        ) {
            ActionButton(
                text = "Copy",
                onClick = onCopy,
                icon = Icons.Rounded.ContentCopy,
                modifier = Modifier.weight(1f),
                containerColor = Zinc800,
                contentColor = Color.White
            )
            ActionButton(
                text = "Save",
                onClick = onSave,
                icon = Icons.Rounded.Save,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun OutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    minHeight: Dp = 56.dp,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors()
) {
    androidx.compose.material3.OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.heightIn(min = minHeight),
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        colors = colors,
        shape = MaterialTheme.shapes.medium
    )
}
