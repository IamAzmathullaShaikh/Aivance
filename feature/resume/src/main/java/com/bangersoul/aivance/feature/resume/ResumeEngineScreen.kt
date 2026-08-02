package com.bangersoul.aivance.feature.resume

import android.net.Uri
import android.provider.OpenableColumns
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bangersoul.aivance.core.common.model.ResumeAnalysis
import com.bangersoul.aivance.core.common.model.ResumeVersion
import com.bangersoul.aivance.core.designsystem.components.AivancePrimaryButton
import com.bangersoul.aivance.core.designsystem.components.AivanceSecondaryButton
import com.bangersoul.aivance.core.designsystem.components.BannerTone
import com.bangersoul.aivance.core.designsystem.components.KeywordChip
import com.bangersoul.aivance.core.designsystem.components.ScoreGauge
import com.bangersoul.aivance.core.designsystem.components.StatusChip
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024L

private val ENGINE_STEPS = listOf(
    "Import", "Parsing", "Preview", "ATS", "Optimize", "Save", "Export"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumeEngineScreen(
    viewModel: ResumeEngineViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ResumeEngineEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is ResumeEngineEffect.ExportResult -> shareResumeFile(context, effect.text, effect.fileName)
                is ResumeEngineEffect.ExportPdf -> shareResumePdf(context, effect.uri)
                ResumeEngineEffect.Finished -> onBack()
            }
        }
    }

    // BackHandler-aware step navigation — one step back per press.
    BackHandler(enabled = true) {
        when (state) {
            is ResumeEngineState.Import -> onBack()
            else -> viewModel.onEvent(ResumeEngineEvent.Back)
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Column {
                        Text("Resume Engine", fontWeight = FontWeight.Bold)
                        Text(
                            "Import → Optimize → Export",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state is ResumeEngineState.Import) onBack()
                        else viewModel.onEvent(ResumeEngineEvent.Back)
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            EngineStepper(currentStep = state.stepIndex())

            AnimatedContent(
                targetState = state,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ResumeEngineTransition"
            ) { current ->
                when (current) {
                    is ResumeEngineState.Import -> ImportStep(
                        onFileImported = { viewModel.onEvent(ResumeEngineEvent.ImportFile(it)) },
                        onOcrTextExtracted = { viewModel.onEvent(ResumeEngineEvent.ImportOcrText(it)) },
                        onExit = onBack
                    )
                    is ResumeEngineState.Parsing -> ParsingStep(current.progress)
                    is ResumeEngineState.Preview -> PreviewStep(
                        version = current.version,
                        onContinue = { viewModel.onEvent(ResumeEngineEvent.ContinueFromPreview) },
                        onEdit = { title, content ->
                            viewModel.onEvent(ResumeEngineEvent.UpdateSectionContent(title, content))
                        }
                    )
                    is ResumeEngineState.AtsScanning -> AtsScanStep(
                        jdText = current.jdText,
                        onJdTextChange = { viewModel.onEvent(ResumeEngineEvent.UpdateJdText(it)) },
                        onRunScan = { viewModel.onEvent(ResumeEngineEvent.RunAtsScan) },
                        onSkip = { viewModel.onEvent(ResumeEngineEvent.SkipAts) }
                    )
                    is ResumeEngineState.AtsResult -> AtsResultStep(
                        score = current.score,
                        analysis = current.analysis,
                        onContinue = { viewModel.onEvent(ResumeEngineEvent.SkipAts) }
                    )
                    is ResumeEngineState.Optimizing -> OptimizingStep(
                        version = current.version,
                        sectionInProgress = current.sectionInProgress,
                        suggestions = current.suggestions,
                        onImprove = { viewModel.onEvent(ResumeEngineEvent.ImproveSection(it)) },
                        onAccept = { viewModel.onEvent(ResumeEngineEvent.AcceptSuggestion(it)) },
                        onDiscard = { viewModel.onEvent(ResumeEngineEvent.DiscardSuggestion(it)) },
                        onSave = { viewModel.onEvent(ResumeEngineEvent.SaveVersion(it)) }
                    )
                    is ResumeEngineState.Saving -> SavingStep()
                    is ResumeEngineState.Exporting -> ExportStep(
                        versionName = current.version.versionName,
                        onExportPdf = { viewModel.onEvent(ResumeEngineEvent.ExportPdf) },
                        onExportDocx = { viewModel.onEvent(ResumeEngineEvent.ExportDocx) },
                        onDone = { viewModel.onEvent(ResumeEngineEvent.Finish) }
                    )
                    is ResumeEngineState.Error -> ErrorStep(
                        step = current.step,
                        message = current.message,
                        // Retry only re-imports for Import/Parsing failures; for
                        // ATS/Optimization/Save errors it steps back to the prior
                        // step, so only advertise it as a retry in those cases.
                        canRetry = current.canRetry,
                        onRetry = { viewModel.onEvent(ResumeEngineEvent.Retry) },
                        onBack = { viewModel.onEvent(ResumeEngineEvent.Back) }
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/** Maps a state to its 0-based step index for the stepper header. */
private fun ResumeEngineState.stepIndex(): Int = when (this) {
    is ResumeEngineState.Import -> 0
    is ResumeEngineState.Parsing -> 1
    is ResumeEngineState.Preview -> 2
    is ResumeEngineState.AtsScanning, is ResumeEngineState.AtsResult -> 3
    is ResumeEngineState.Optimizing -> 4
    is ResumeEngineState.Saving -> 5
    is ResumeEngineState.Exporting -> 6
    is ResumeEngineState.Error -> 0
}

@Composable
private fun EngineStepper(currentStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ENGINE_STEPS.forEachIndexed { index, label ->
            val isActive = index == currentStep
            val isDone = index < currentStep
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(
                            color = when {
                                isActive -> MaterialTheme.colorScheme.primary
                                isDone -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isDone -> Icon(
                            Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        isActive -> Icon(
                            Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        else -> Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color = if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

// ── Step 1: Import ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportStep(
    onFileImported: (Uri) -> Unit,
    onOcrTextExtracted: (String) -> Unit,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedName by remember { mutableStateOf<String?>(null) }
    var selectedSize by remember { mutableStateOf<Long?>(null) }
    var sizeError by remember { mutableStateOf<String?>(null) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    var cameraNote by remember { mutableStateOf<String?>(null) }

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                val (name, size) = resolveFileMeta(context, uri)
                if (size != null && size > MAX_FILE_SIZE_BYTES) {
                    sizeError = "File exceeds the 10MB limit."
                } else {
                    sizeError = null
                    cameraNote = null
                    selectedUri = uri
                    selectedName = name
                    selectedSize = size
                }
            }
        }
    )
    val docxLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                val (name, size) = resolveFileMeta(context, uri)
                if (size != null && size > MAX_FILE_SIZE_BYTES) {
                    sizeError = "File exceeds the 10MB limit."
                } else {
                    sizeError = null
                    cameraNote = null
                    selectedUri = uri
                    selectedName = name
                    selectedSize = size
                }
            }
        }
    )
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                val uri = cameraUri ?: return@rememberLauncherForActivityResult
                try {
                    val image = InputImage.fromFilePath(context, uri)
                    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                        .process(image)
                        .addOnSuccessListener { visionText ->
                            val extracted = visionText.text.trim()
                            if (extracted.isNotBlank()) {
                                onOcrTextExtracted(extracted)
                            } else {
                                cameraNote = "No text found in the photo. Try a clearer image or use PDF/DOCX."
                            }
                        }
                        .addOnFailureListener {
                            cameraNote = "OCR failed: ${it.message ?: "unknown error"}. Use PDF/DOCX instead."
                        }
                } catch (e: Exception) {
                    cameraNote = "Failed to load camera image for OCR: ${e.message}"
                }
            }
        }
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Step 1 — Import", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Pick a PDF or DOCX resume, or scan a new one. Files up to 10MB.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
        }
        item {
            ImportOptionCard(
                icon = Icons.Rounded.Description,
                title = "Pick PDF",
                subtitle = "application/pdf",
                onClick = {
                    sizeError = null
                    pdfLauncher.launch(arrayOf("application/pdf"))
                }
            )
        }
        item {
            ImportOptionCard(
                icon = Icons.Rounded.DocumentScanner,
                title = "Pick DOCX",
                subtitle = "Word documents",
                onClick = {
                    sizeError = null
                    docxLauncher.launch(
                        arrayOf(
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                        )
                    )
                }
            )
        }
        item {
            ImportOptionCard(
                icon = Icons.Rounded.PhotoCamera,
                title = "Scan with Camera",
                subtitle = "Capture a resume with your camera",
                onClick = {
                    sizeError = null
                    val file = File(context.cacheDir, "camera_resume_${System.currentTimeMillis()}.jpg")
                    val uri = androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    cameraUri = uri
                    cameraLauncher.launch(uri)
                }
            )
        }

        if (selectedName != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(selectedName ?: "", fontWeight = FontWeight.SemiBold)
                            Text(
                                if (selectedSize != null) formatFileSize(selectedSize!!) else "Size unknown",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = {
                            selectedUri = null
                            selectedName = null
                            selectedSize = null
                            cameraNote = null
                        }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Remove file")
                        }
                    }
                }
            }
        }

        if (sizeError != null) {
            item {
                Text(sizeError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }

        if (cameraNote != null) {
            item {
                Text(
                    cameraNote!!,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            AivancePrimaryButton(
                text = "Continue",
                onClick = { selectedUri?.let(onFileImported) },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Rounded.FileUpload,
                enabled = selectedUri != null && sizeError == null
            )
            Spacer(Modifier.height(8.dp))
            AivanceSecondaryButton(
                text = "Cancel",
                onClick = onExit,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(48.dp))
        }
    }
}

private data class FileMeta(val name: String, val size: Long?)

private fun resolveFileMeta(context: android.content.Context, uri: Uri): FileMeta {
    var name: String? = null
    var size: Long? = null
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (cursor.moveToFirst()) {
            if (nameIdx >= 0) name = cursor.getString(nameIdx)
            if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) size = cursor.getLong(sizeIdx)
        }
    }
    return FileMeta(
        name ?: uri.lastPathSegment ?: "Imported resume",
        size
    )
}

private fun formatFileSize(bytes: Long): String = when {
    bytes >= 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0))
    else -> String.format(Locale.getDefault(), "%.0f KB", bytes / 1024.0)
}

@Composable
private fun ImportOptionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ── Step 2: Parsing ──────────────────────────────────────────────────────────

@Composable
private fun ParsingStep(progress: Float) {
    val animated = animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600, easing = LinearEasing),
        label = "parsingProgress"
    )
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(56.dp),
            strokeWidth = 6.dp,
            progress = { animated.value }
        )
        Spacer(Modifier.height(24.dp))
        Text("Parsing your resume…", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Extracting sections and structure",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { animated.value },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ── Step 3: Preview ──────────────────────────────────────────────────────────

@Composable
private fun PreviewStep(
    version: ResumeVersion,
    onContinue: () -> Unit,
    onEdit: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf<Set<String>>(emptySet()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Step 3 — Preview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Found ${version.sections.size} sections. Tap a section to expand and edit its raw text.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            StatusChip(
                text = "${version.sections.size} sections detected",
                tone = BannerTone.SUCCESS
            )
            Spacer(Modifier.height(8.dp))
        }

        items(version.sections) { section ->
            val isExpanded = section.title in expanded
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(section.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        IconButton(onClick = {
                            expanded = if (isExpanded) expanded - section.title else expanded + section.title
                        }) {
                            Icon(
                                if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = if (isExpanded) "Collapse" else "Expand"
                            )
                        }
                    }
                    if (isExpanded) {
                        OutlinedTextField(
                            value = section.content,
                            onValueChange = { onEdit(section.title, it) },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            minLines = 4
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            AivancePrimaryButton(
                text = "Looks good, continue",
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(48.dp))
        }
    }
}

// ── Step 4: ATS Scan ─────────────────────────────────────────────────────────

@Composable
private fun AtsScanStep(
    jdText: String,
    onJdTextChange: (String) -> Unit,
    onRunScan: () -> Unit,
    onSkip: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Step 4 — ATS Scan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            "Paste the job description to check your match.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = jdText,
            onValueChange = onJdTextChange,
            label = { Text("Job Description") },
            modifier = Modifier.fillMaxWidth().height(280.dp),
            placeholder = { Text("Company requirements, skills, responsibilities…") }
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Needs at least 50 characters. ATS score recalculates on demand.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.weight(1f))
        AivancePrimaryButton(
            text = "Run ATS Scan",
            onClick = onRunScan,
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Rounded.Search,
            enabled = jdText.length > 50
        )
        Spacer(Modifier.height(8.dp))
        AivanceSecondaryButton(
            text = "Skip ATS Scan",
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(48.dp))
    }
}

// ── Step 4b: ATS Result ──────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AtsResultStep(
    score: Int,
    analysis: ResumeAnalysis,
    onContinue: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("ATS Match Report", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    Modifier.padding(24.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    ScoreGauge(score = score, size = 100.dp)
                    Column {
                        Text("Overall ATS Score", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        StatusChip(
                            text = when {
                                score > 80 -> "Ready to apply"
                                score > 50 -> "Needs improvement"
                                else -> "Weak match"
                            },
                            tone = when {
                                score > 80 -> BannerTone.SUCCESS
                                score > 50 -> BannerTone.WARNING
                                else -> BannerTone.ERROR
                            }
                        )
                    }
                }
            }
        }

        item {
            Text("Keywords", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            if (analysis.matchingKeywords.isEmpty() && analysis.missingKeywords.isEmpty()) {
                Text(
                    "No keyword analysis available.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    analysis.matchingKeywords.forEach { KeywordChip(text = it, isMatched = true) }
                    analysis.missingKeywords.forEach { KeywordChip(text = it, isMatched = false) }
                }
            }
        }

        if (analysis.suggestions.isNotEmpty()) {
            item {
                Text("Improvement Suggestions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
            }
            items(analysis.suggestions) { suggestion ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Text(
                        suggestion,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            AivancePrimaryButton(
                text = "Continue to Optimization",
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Rounded.AutoAwesome
            )
            Spacer(Modifier.height(48.dp))
        }
    }
}

// ── Step 5: AI Optimization ──────────────────────────────────────────────────

@Composable
private fun OptimizingStep(
    version: ResumeVersion,
    sectionInProgress: String?,
    suggestions: Map<String, String>,
    onImprove: (String) -> Unit,
    onAccept: (String) -> Unit,
    onDiscard: (String) -> Unit,
    onSave: (String) -> Unit
) {
    // Pre-fill with a date-stamped version name per spec: "v{n} — {date}".
    // v1 is the original import, so the first optimized save is v2.
    var versionName by remember {
        mutableStateOf("v2 — ${SimpleDateFormat("MMM d", Locale.getDefault()).format(Date())}")
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Step 5 — AI Optimization", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Improve each section with AI, then accept or discard the suggestion.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
        }

        itemsIndexed(version.sections) { _, section ->
            val suggestion = suggestions[section.title]
            val isImproving = sectionInProgress == section.title

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(section.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        if (isImproving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            IconButton(onClick = { onImprove(section.title) }) {
                                Icon(
                                    Icons.Rounded.AutoAwesome,
                                    contentDescription = "Improve with AI",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    if (isImproving) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Improving with AI…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = section.content,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (suggestion != null) {
                            Spacer(Modifier.height(12.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(
                                        "AI suggestion",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    TypewriterText(suggestion)
                                    Spacer(Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        AivancePrimaryButton(
                                            text = "Accept",
                                            onClick = { onAccept(section.title) },
                                            icon = Icons.Rounded.Check
                                        )
                                        AivanceSecondaryButton(
                                            text = "Discard",
                                            onClick = { onDiscard(section.title) },
                                            icon = Icons.Rounded.Close
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = versionName,
                onValueChange = { versionName = it },
                label = { Text("Version name (optional)") },
                placeholder = { Text("e.g. v2 — ${SimpleDateFormat("MMM d", Locale.getDefault()).format(Date())}") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            AivancePrimaryButton(
                text = "Save Version & Continue",
                onClick = { onSave(versionName) },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Rounded.Save
            )
            Spacer(Modifier.height(48.dp))
        }
    }
}

/** Reveals text progressively to mimic streaming AI output. */
@Composable
private fun TypewriterText(text: String) {
    var visibleChars by remember { mutableStateOf(0) }
    LaunchedEffect(text) {
        visibleChars = 0
        while (visibleChars < text.length) {
            delay(12)
            visibleChars = (visibleChars + 3).coerceAtMost(text.length)
        }
    }
    Text(
        text = text.take(visibleChars),
        style = MaterialTheme.typography.bodyMedium
    )
}

// ── Step 6: Save ─────────────────────────────────────────────────────────────

@Composable
private fun SavingStep() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(48.dp), strokeWidth = 5.dp)
        Spacer(Modifier.height(16.dp))
        Text("Saving version…", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Storing the optimized version in your library",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Step 7: Export ───────────────────────────────────────────────────────────

@Composable
private fun ExportStep(
    versionName: String,
    onExportPdf: () -> Unit,
    onExportDocx: () -> Unit,
    onDone: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(32.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(20.dp)) {
                Icon(Icons.Rounded.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                Spacer(Modifier.height(8.dp))
                Text("Version saved!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "\"$versionName\" is ready to export.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("Step 7 — Export", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        AivancePrimaryButton(
            text = "Export as PDF",
            onClick = onExportPdf,
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Rounded.FileDownload
        )
        Spacer(Modifier.height(8.dp))
        AivanceSecondaryButton(
            text = "Export as DOCX",
            onClick = onExportDocx,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        AivancePrimaryButton(
            text = "Done",
            onClick = onDone,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(48.dp))
    }
}

// ── Error ────────────────────────────────────────────────────────────────────

@Composable
private fun ErrorStep(
    step: String,
    message: String,
    canRetry: Boolean,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Rounded.Close, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(16.dp))
        Text("$step failed", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(24.dp))
        if (canRetry) {
            AivancePrimaryButton(text = "Retry", onClick = onRetry, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            AivanceSecondaryButton(text = "Back", onClick = onBack, modifier = Modifier.fillMaxWidth())
        } else {
            // For ATS/Optimization/Save failures Retry would only step back, so
            // surface a single honest "Back" action instead.
            AivancePrimaryButton(text = "Back", onClick = onBack, modifier = Modifier.fillMaxWidth())
        }
    }
}

/** Shares a generated PDF (Phase 4 STEP 2) via ACTION_SEND with the app's FileProvider. */
private fun shareResumePdf(context: android.content.Context, uri: Uri) {
    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(android.content.Intent.EXTRA_STREAM, uri)
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    if (shareIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(android.content.Intent.createChooser(shareIntent, "Export Resume"))
    }
}

/** Writes [text] to a cache file (wrapping it as HTML when it's a .doc so
 *  Word opens it correctly) and fires a share sheet. */
private fun shareResumeFile(context: android.content.Context, text: String, fileName: String) {
    val file = File(context.cacheDir, fileName)
    val content = if (fileName.endsWith(".doc", ignoreCase = true)) {
        "<!DOCTYPE html><html><head><meta charset=\"utf-8\"></head><body><pre>$text</pre></body></html>"
    } else {
        text
    }
    file.writeText(content)
    val uri = androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = if (fileName.endsWith(".doc", ignoreCase = true)) "application/msword" else "text/plain"
        putExtra(android.content.Intent.EXTRA_STREAM, uri)
        putExtra(android.content.Intent.EXTRA_TEXT, text)
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    if (shareIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(android.content.Intent.createChooser(shareIntent, "Export Resume"))
    }
}


