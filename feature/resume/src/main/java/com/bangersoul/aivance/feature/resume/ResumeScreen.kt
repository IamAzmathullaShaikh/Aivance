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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bangersoul.aivance.core.designsystem.components.ActionButton
import com.bangersoul.aivance.core.designsystem.components.AivanceScreen
import com.bangersoul.aivance.core.designsystem.components.DashboardCard
import com.bangersoul.aivance.core.designsystem.components.KeywordChip
import com.bangersoul.aivance.core.designsystem.components.ScoreGauge
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme
import com.bangersoul.aivance.core.util.PdfTextExtractor
import com.bangersoul.aivance.feature.resume.domain.model.KeywordInfo
import com.bangersoul.aivance.feature.resume.domain.model.OptimizationTip
import com.bangersoul.aivance.feature.resume.domain.model.ResumeAnalysis

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumeScreen(
    viewModel: ResumeViewModel,
    onNavigateToAts: () -> Unit,
    onNavigateToCoverLetter: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val resumeText by viewModel.resumeText.collectAsState()
    val jobDescription by viewModel.jobDescription.collectAsState()
    val context = LocalContext.current

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                val extractedText = PdfTextExtractor.extractTextFromPdf(context, it)
                viewModel.updateResumeText(extractedText)
            }
        }
    )

    var showTrackDialog by rememberSaveable { mutableStateOf(false) }
    var trackCompany by rememberSaveable { mutableStateOf("") }
    var trackRole by rememberSaveable { mutableStateOf("") }

    if (showTrackDialog) {
        TrackJobDialog(
            company = trackCompany,
            onCompanyChange = { trackCompany = it },
            role = trackRole,
            onRoleChange = { trackRole = it },
            onConfirm = {
                viewModel.addJobToTracker(trackCompany, trackRole)
                showTrackDialog = false
            },
            onDismiss = { showTrackDialog = false }
        )
    }

    AivanceScreen(
        topBar = {
            TopAppBar(
                title = { Text("Resume Optimizer", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        isLoading = uiState is ResumeUiState.Analyzing,
        error = (uiState as? ResumeUiState.Error)?.message,
        onRetry = { viewModel.analyzeResume(resumeText, jobDescription) }
    ) {
        AnimatedContent(
            targetState = uiState,
            label = "ResumeContentTransition",
            transitionSpec = {
                fadeIn().togetherWith(fadeOut())
            }
        ) { state ->
            when (state) {
                is ResumeUiState.Success -> {
                    AnalysisResultContent(
                        analysis = state.analysis,
                        onReset = { viewModel.resetState() },
                        onSave = { viewModel.saveResult("Resume_${System.currentTimeMillis()}.pdf") },
                        onTrackClick = {
                            trackCompany = "" // Or try to extract from jobDescription
                            trackRole = ""    // Or try to extract from jobDescription
                            showTrackDialog = true
                        },
                        onNavigateToAts = onNavigateToAts,
                        onNavigateToCoverLetter = onNavigateToCoverLetter
                    )
                }
                else -> {
                    ResumeInputContent(
                        resumeText = resumeText,
                        onResumeChange = { viewModel.updateResumeText(it) },
                        jobDescription = jobDescription,
                        onJobDescriptionChange = { viewModel.updateJobDescription(it) },
                        onAnalyze = { viewModel.analyzeResume(resumeText, jobDescription) },
                        onPdfUploadClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ResumeInputContent(
    resumeText: String,
    onResumeChange: (String) -> Unit,
    jobDescription: String,
    onJobDescriptionChange: (String) -> Unit,
    onAnalyze: () -> Unit,
    onPdfUploadClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Resume Optimizer",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Your Resume",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
                value = resumeText,
                onValueChange = onResumeChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                placeholder = { Text("Paste your resume text here...") },
                leadingIcon = { Icon(Icons.Rounded.Description, contentDescription = null) }
            )

            DashboardCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onPdfUploadClick
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Rounded.FileUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = "Upload PDF",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Job Description",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
                value = jobDescription,
                onValueChange = onJobDescriptionChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                placeholder = { Text("Paste the job description here...") },
                leadingIcon = { Icon(Icons.Rounded.HistoryEdu, contentDescription = null) }
            )
        }

        ActionButton(
            text = "Analyze Resume",
            onClick = onAnalyze,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(40.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AnalysisResultContent(
    analysis: ResumeAnalysis,
    onReset: () -> Unit,
    onSave: () -> Unit,
    onTrackClick: () -> Unit,
    onNavigateToAts: () -> Unit,
    onNavigateToCoverLetter: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Analysis Results",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton(
                    text = "Track this Job",
                    onClick = onTrackClick,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
                ActionButton(
                    text = "Save",
                    onClick = onSave,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
                ActionButton(
                    text = "New Scan",
                    onClick = onReset,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        DashboardCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                ScoreGauge(score = analysis.matchScore, size = 120.dp)
                Column {
                    Text(
                        text = "Match Score",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when {
                            analysis.matchScore >= 80 -> "Excellent match!"
                            analysis.matchScore >= 60 -> "Good potential"
                            else -> "Needs significant optimization"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Keywords Analysis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                analysis.keywords.forEach { keyword ->
                    KeywordChip(text = keyword.text, isMatched = keyword.isMatched)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Optimization Tips",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            analysis.tips.forEach { tip ->
                OptimizationTipItem(tip.category, tip.description)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Next Steps",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton(
                    text = "ATS Check",
                    onClick = onNavigateToAts,
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ActionButton(
                    text = "Cover Letter",
                    onClick = onNavigateToCoverLetter,
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun OptimizationTipItem(category: String, description: String) {
    DashboardCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Lightbulb,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = category,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun TrackJobDialog(
    company: String,
    onCompanyChange: (String) -> Unit,
    role: String,
    onRoleChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Track this Job") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Confirm details to add to your Job Tracker.")
                OutlinedTextField(
                    value = company,
                    onValueChange = onCompanyChange,
                    label = { Text("Company") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = role,
                    onValueChange = onRoleChange,
                    label = { Text("Role") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            ActionButton(
                text = "Add to Tracker",
                onClick = onConfirm,
                enabled = company.isNotBlank() && role.isNotBlank()
            )
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun ResumeScreenInputPreview() {
    AivanceTheme(darkTheme = true) {
        ResumeInputContent(
            resumeText = "",
            onResumeChange = {},
            jobDescription = "",
            onJobDescriptionChange = {},
            onAnalyze = {},
            onPdfUploadClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun ResumeScreenResultPreview() {
    AivanceTheme(darkTheme = true) {
        AnalysisResultContent(
            analysis = ResumeAnalysis(
                matchScore = 85,
                keywords = listOf(
                    KeywordInfo("Kotlin", isMatched = true),
                    KeywordInfo("Jetpack Compose", isMatched = true),
                    KeywordInfo("MVVM", isMatched = true),
                    KeywordInfo("Dagger Hilt", isMatched = false),
                    KeywordInfo("Coroutines", isMatched = true)
                ),
                tips = listOf(
                    OptimizationTip("Experience", "Quantify your achievements with data points."),
                    OptimizationTip("Skills", "Add 'Dagger Hilt' to your skills section.")
                )
            ),
            onReset = {},
            onSave = {},
            onTrackClick = {},
            onNavigateToAts = {},
            onNavigateToCoverLetter = {}
        )
    }
}
