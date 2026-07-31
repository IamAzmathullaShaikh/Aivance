package com.bangersoul.aivance.feature.resume

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bangersoul.aivance.core.common.model.ResumeVersion
import com.bangersoul.aivance.core.designsystem.components.AivanceEmptyState
import com.bangersoul.aivance.core.designsystem.components.AivancePrimaryButton
import com.bangersoul.aivance.core.designsystem.components.AivanceScreen
import com.bangersoul.aivance.core.designsystem.components.AivanceSecondaryButton
import com.bangersoul.aivance.core.designsystem.components.BannerTone
import com.bangersoul.aivance.core.designsystem.components.InsightCard
import com.bangersoul.aivance.core.designsystem.components.KeywordChip
import com.bangersoul.aivance.core.designsystem.components.ScoreGauge
import com.bangersoul.aivance.core.designsystem.components.StatusChip
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumeScreen(
    viewModel: ResumeViewModel,
    onNavigateToAts: () -> Unit,
    onNavigateToCoverLetter: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> uri?.let { viewModel.onEvent(ResumeUiEvent.ImportFile(it)) } }
    )

    LaunchedEffect(Unit) {
        viewModel.onEvent(ResumeUiEvent.Refresh)
    }

    AivanceScreen(
        topBar = {
            TopAppBar(
                title = { Text("Resume Intelligence", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(
                        onClick = {
                            filePickerLauncher.launch(
                                arrayOf(
                                    "application/pdf",
                                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                )
                            )
                        }
                    ) {
                        Icon(Icons.Rounded.FileUpload, contentDescription = "Import resume")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        isLoading = uiState is ResumeUiState.Loading,
        error = (uiState as? ResumeUiState.Error)?.message,
        onRetry = { viewModel.onEvent(ResumeUiEvent.Refresh) }
    ) {
        AnimatedContent(
            targetState = uiState,
            label = "ResumeContentTransition",
            transitionSpec = { fadeIn() togetherWith fadeOut() }
        ) { state ->
            when (state) {
                is ResumeUiState.Idle -> ResumeEmptyContent(
                    onImportClick = { filePickerLauncher.launch(arrayOf("application/pdf")) },
                    onExploreAts = onNavigateToAts
                )
                is ResumeUiState.Success -> ResumeEditorContent(
                    versions = state.versions,
                    selectedVersion = state.selectedVersion,
                    atsScore = state.atsScore,
                    analysisResult = state.analysisResult,
                    onVersionSelect = { viewModel.onEvent(ResumeUiEvent.SelectVersion(it)) },
                    onAnalyze = { viewModel.onEvent(ResumeUiEvent.Analyze(it)) },
                    onSaveVersion = { viewModel.onEvent(ResumeUiEvent.SaveVersion(it)) },
                    onExploreAts = onNavigateToAts
                )
                else -> {}
            }
        }
    }
}

@Composable
private fun ResumeEmptyContent(
    onImportClick: () -> Unit,
    onExploreAts: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AivanceEmptyState(
            title = "No Resume Found",
            description = "Upload your resume to get AI-powered insights, ATS optimization, and tailored career recommendations.",
            icon = Icons.Rounded.Description,
            primaryActionText = "Import Resume (PDF/DOCX)",
            onPrimaryAction = onImportClick,
            secondaryActionText = "Explore ATS Intelligence",
            onSecondaryAction = onExploreAts
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ResumeEditorContent(
    versions: List<ResumeVersion>,
    selectedVersion: ResumeVersion?,
    atsScore: Int?,
    analysisResult: com.bangersoul.aivance.feature.resume.domain.model.ResumeAnalysis?,
    onVersionSelect: (Long) -> Unit,
    onAnalyze: (String) -> Unit,
    onSaveVersion: (ResumeVersion) -> Unit,
    onExploreAts: () -> Unit
) {
    var jobDesc by remember { mutableStateOf("") }

    // Local draft of section contents so editing feels instant; committed via SaveVersion.
    var sectionDrafts by remember(selectedVersion?.id) {
        mutableStateOf(selectedVersion?.sections?.map { it.content } ?: emptyList())
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Version Selector
        item {
            Text("Resume Versions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                versions.forEach { v ->
                    FilterChip(
                        selected = v.id == selectedVersion?.id,
                        onClick = { onVersionSelect(v.id) },
                        label = { Text(v.versionName) }
                    )
                }
            }
        }

        if (selectedVersion != null) {
            // ATS Score if available
            if (atsScore != null) {
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ATS Score", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        StatusChip(
                            text = if (atsScore > 80) "Ready to apply" else "Needs improvement",
                            tone = if (atsScore > 80) BannerTone.SUCCESS else BannerTone.WARNING
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    ScoreGauge(score = atsScore, size = 120.dp, modifier = Modifier.fillMaxWidth())
                }
            }

            // Section Editor
            item {
                Text("Edit Sections", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Edits are previewed locally and saved to the selected version.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
            }

            itemsIndexed(selectedVersion.sections) { index, section ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        section.title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = sectionDrafts.getOrNull(index) ?: section.content,
                        onValueChange = { newValue ->
                            sectionDrafts = sectionDrafts.toMutableList().also { list ->
                                if (index < list.size) list[index] = newValue
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 10
                    )
                }
            }

            if (selectedVersion.sections.isNotEmpty()) {
                item {
                    AivancePrimaryButton(
                        text = "Save Changes",
                        onClick = {
                            val updated = selectedVersion.copy(
                                sections = selectedVersion.sections.mapIndexed { i, s ->
                                    if (i < sectionDrafts.size) s.copy(content = sectionDrafts[i]) else s
                                }
                            )
                            onSaveVersion(updated)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Rounded.Save
                    )
                }
            }

            // Analysis results — rendered live once the AI analysis completes.
            if (analysisResult != null) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Match Analysis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        StatusChip(
                            text = when {
                                analysisResult.matchScore > 70 -> "Strong match"
                                analysisResult.matchScore > 40 -> "Moderate match"
                                else -> "Weak match"
                            },
                            tone = when {
                                analysisResult.matchScore > 70 -> BannerTone.SUCCESS
                                analysisResult.matchScore > 40 -> BannerTone.WARNING
                                else -> BannerTone.ERROR
                            }
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    ScoreGauge(score = analysisResult.matchScore, size = 120.dp, modifier = Modifier.fillMaxWidth())
                }

                item {
                    Text("Keywords", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        analysisResult.keywords.forEach { keyword ->
                            KeywordChip(text = keyword.text, isMatched = keyword.isMatched)
                        }
                    }
                }

                items(analysisResult.tips) { tip ->
                    InsightCard(
                        text = tip.description,
                        icon = Icons.Rounded.Lightbulb,
                        iconTint = if (tip.category == "Improvement") AivanceTheme.colors.warning else AivanceTheme.colors.accent
                    )
                }
            }

            // Analysis Trigger
            item {
                Text("Analyze against Job", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = jobDesc,
                    onValueChange = { jobDesc = it },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    placeholder = { Text("Paste Job Description...") }
                )
                Spacer(Modifier.height(16.dp))
                AivancePrimaryButton(
                    text = "Analyze & Score",
                    onClick = { onAnalyze(jobDesc) },
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Rounded.Search,
                    enabled = jobDesc.isNotBlank()
                )
                Spacer(Modifier.height(8.dp))
                AivanceSecondaryButton(
                    text = "Open ATS Intelligence",
                    onClick = onExploreAts,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(48.dp))
            }
        }
    }
}
