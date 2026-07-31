package com.bangersoul.aivance.feature.ats

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.bangersoul.aivance.core.common.model.AtsReport
import com.bangersoul.aivance.core.common.model.Resume
import com.bangersoul.aivance.core.common.model.ResumeVersion
import com.bangersoul.aivance.core.designsystem.components.ActionButton
import com.bangersoul.aivance.core.designsystem.components.AivanceScreen
import com.bangersoul.aivance.core.designsystem.components.DashboardCard
import com.bangersoul.aivance.core.designsystem.components.KeywordChip
import com.bangersoul.aivance.core.designsystem.components.ScoreGauge
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtsScreen(
    viewModel: AtsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val resumes by viewModel.resumes.collectAsState()

    AivanceScreen(
        topBar = {
            TopAppBar(
                title = { Text("ATS Intelligence", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        isLoading = uiState is AtsUiState.Analyzing,
        error = (uiState as? AtsUiState.Error)?.message,
        onRetry = { viewModel.onEvent(AtsUiEvent.Reset) }
    ) {
        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "AtsStateTransition"
        ) { state ->
            when (state) {
                AtsUiState.SelectingResume -> ResumeSelectionStep(
                    resumes = resumes,
                    onSelect = { r, v -> viewModel.onEvent(AtsUiEvent.SelectResumeVersion(r, v)) }
                )
                is AtsUiState.InputJobDescription -> JobDescriptionInputStep(
                    resumeName = state.resume.name,
                    versionName = state.selectedVersion.versionName,
                    onAnalyze = { viewModel.onEvent(AtsUiEvent.Analyze(it)) },
                    onBack = { viewModel.onEvent(AtsUiEvent.Reset) }
                )
                is AtsUiState.DisplayReport -> AtsReportContent(
                    report = state.report,
                    onNewScan = { viewModel.onEvent(AtsUiEvent.Reset) }
                )
                else -> {}
            }
        }
    }
}

@Composable
private fun ResumeSelectionStep(
    resumes: List<Resume>,
    onSelect: (Resume, ResumeVersion) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Select Resume to Analyze", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(resumes) { resume ->
                resume.versions.forEach { version ->
                    Card(
                        onClick = { onSelect(resume, version) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Description, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(resume.name, fontWeight = FontWeight.Bold)
                                Text(version.versionName, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JobDescriptionInputStep(
    resumeName: String,
    versionName: String,
    onAnalyze: (String) -> Unit,
    onBack: () -> Unit
) {
    var jdText by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Target Job", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Analyzing against: $resumeName ($versionName)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = jdText,
            onValueChange = { jdText = it },
            label = { Text("Paste Job Description") },
            modifier = Modifier.fillMaxWidth().height(300.dp),
            placeholder = { Text("Company requirements, skills, responsibilities...") }
        )
        Spacer(Modifier.weight(1f))
        ActionButton(
            text = "Start Match Analysis",
            onClick = { onAnalyze(jdText) },
            modifier = Modifier.fillMaxWidth(),
            enabled = jdText.length > 50
        )
        ActionButton(
            text = "Back",
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AtsReportContent(
    report: AtsReport,
    onNewScan: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Match Report", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                ActionButton(text = "New Scan", onClick = onNewScan, containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item {
            DashboardCard(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    ScoreGauge(score = report.overallScore, size = 100.dp)
                    Column {
                        Text("Overall ATS Score", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${report.matchPercentage}% Match Probability", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        item {
            Text("Section Scores", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            report.sectionScores.forEach { (name, score) ->
                Column(Modifier.padding(vertical = 4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(name, style = MaterialTheme.typography.labelLarge)
                        Text("$score%", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { score / 100f },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = if (score > 70) Color(0xFF4CAF50) else if (score > 40) Color(0xFFFFC107) else Color(0xFFF44336),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        item {
            Text("Keyword Gap Analysis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                report.matchedKeywords.forEach { KeywordChip(text = it, isMatched = true) }
                report.missingKeywords.forEach { KeywordChip(text = it, isMatched = false) }
            }
        }

        item {
            Text("Optimization Suggestions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(report.optimizationTips) { tip ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(
                        imageVector = if (tip.priority == "HIGH") Icons.Rounded.PriorityHigh else Icons.Rounded.Lightbulb,
                        contentDescription = null,
                        tint = if (tip.priority == "HIGH") Color(0xFFF44336) else Color(0xFFFFC107)
                    )
                    Column {
                        Text(tip.category, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(tip.description, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(32.dp))
            ActionButton(text = "Generate Tailored Cover Letter", onClick = { /* TODO */ }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(48.dp))
        }
    }
}
