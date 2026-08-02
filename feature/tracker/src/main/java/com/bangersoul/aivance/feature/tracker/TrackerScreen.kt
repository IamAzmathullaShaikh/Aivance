package com.bangersoul.aivance.feature.tracker

import android.content.ClipData
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bangersoul.aivance.core.common.model.Application
import com.bangersoul.aivance.core.common.model.ApplicationStage
import com.bangersoul.aivance.core.common.model.TimelineEvent
import com.bangersoul.aivance.core.designsystem.components.*
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TrackerScreen(
    viewModel: TrackerViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            AivanceTopBar(title = "Career Pipeline", onBack = onBack)
            AnimatedContent(
                targetState = uiState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TrackerTransition"
            ) { state ->
                when (state) {
                    is TrackerUiState.Loading -> SkeletonDashboard(modifier = Modifier.fillMaxSize())
                    is TrackerUiState.Error -> AivanceError(
                        message = state.message,
                        onRetry = { viewModel.onEvent(TrackerUiEvent.Refresh) },
                        title = "Pipeline unavailable"
                    )
                    is TrackerUiState.Success -> PipelineBoard(
                        stages = state.stages,
                        applications = state.applications,
                        selectedApplicationId = state.selectedApplicationId,
                        onMove = { appId, stageId ->
                            viewModel.onEvent(TrackerUiEvent.UpdateStage(appId, stageId))
                        },
                        onSelect = { appId ->
                            viewModel.onEvent(TrackerUiEvent.SelectApplication(appId))
                        },
                        onClose = { viewModel.onEvent(TrackerUiEvent.CloseApplication) },
                        onDelete = { appId ->
                            viewModel.onEvent(TrackerUiEvent.DeleteApplication(appId))
                        },
                        onNotesChange = { appId, notes ->
                            viewModel.onEvent(TrackerUiEvent.UpdateNotes(appId, notes))
                        }
                    )
                    else -> {}
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun PipelineBoard(
    stages: List<ApplicationStage>,
    applications: List<Application>,
    selectedApplicationId: Long?,
    onMove: (Long, String) -> Unit,
    onSelect: (Long) -> Unit,
    onClose: () -> Unit,
    onDelete: (Long) -> Unit,
    onNotesChange: (Long, String) -> Unit
) {
    if (stages.isEmpty()) {
        AivanceEmptyState(
            title = "No pipeline stages",
            description = "Pipeline stages could not be loaded.",
            icon = Icons.Rounded.ViewKanban
        )
        return
    }

    val selectedApplication = selectedApplicationId?.let { id ->
        applications.firstOrNull { it.id == id }
    }

    LazyRow(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(stages) { stage ->
            PipelineColumn(
                stage = stage,
                applications = applications.filter { it.currentStageId == stage.id },
                onMove = onMove,
                onSelect = onSelect
            )
        }
    }

    if (selectedApplication != null) {
        ApplicationDetailSheet(
            application = selectedApplication,
            onDismiss = onClose,
            onDelete = { onDelete(selectedApplication.id) },
            onNotesChange = { onNotesChange(selectedApplication.id, it) }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PipelineColumn(
    stage: ApplicationStage,
    applications: List<Application>,
    onMove: (Long, String) -> Unit,
    onSelect: (Long) -> Unit
) {
    var isDropTarget by remember { mutableStateOf(false) }

    Column(modifier = Modifier.width(288.dp)) {
        // Column header with count
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(stageColor(stage.id), RoundedCornerShape(3.dp))
                )
                Text(stage.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Badge(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                Text(applications.size.toString(), fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(12.dp))

        val currentOnMove by rememberUpdatedState(onMove)
        val dropTarget = remember(stage.id) {
            object : DragAndDropTarget {
                override fun onStarted(event: DragAndDropEvent) {
                    isDropTarget = true
                }

                override fun onEntered(event: DragAndDropEvent) {
                    isDropTarget = true
                }

                override fun onExited(event: DragAndDropEvent) {
                    isDropTarget = false
                }

                override fun onEnded(event: DragAndDropEvent) {
                    isDropTarget = false
                }

                override fun onDrop(event: DragAndDropEvent): Boolean {
                    isDropTarget = false
                    val appId = event.toAndroidDragEvent()
                        ?.clipData?.getItemAt(0)?.text?.toString()?.toLongOrNull()
                    return if (appId != null) {
                        currentOnMove(appId, stage.id)
                        true
                    } else {
                        false
                    }
                }
            }
        }

        // Drop target surface wrapping the card list
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .dragAndDropTarget(
                    shouldStartDragAndDrop = { event ->
                        event.toAndroidDragEvent()?.clipData
                            ?.let { it.getItemAt(0)?.text?.isNotBlank() == true } ?: false
                    },
                    target = dropTarget
                )
                .dropHighlight(isDropTarget)
        ) {
            if (applications.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No applications",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(applications, key = { it.id }) { app ->
                        val currentOnSelect by rememberUpdatedState(onSelect)
                        KanbanCard(
                            app = app,
                            onClick = { currentOnSelect(app.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Modifier.dropHighlight(isActive: Boolean): Modifier {
    val targetAlpha by animateFloatAsState(if (isActive) 1f else 0f, label = "dropTarget")
    return this.then(
        Modifier.background(
            AivanceTheme.colors.accent.copy(alpha = 0.16f * targetAlpha),
            RoundedCornerShape(16.dp)
        )
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun KanbanCard(
    app: Application,
    onClick: () -> Unit
) {
    val currentOnClick by rememberUpdatedState(onClick)
    DashboardCard(
        modifier = Modifier
            .fillMaxWidth()
            .dragAndDropSource {
                // Long-press to lift the card into a drag payload; a plain tap
                // opens the application detail sheet.
                detectTapGestures(
                    onTap = { currentOnClick() },
                    onLongPress = {
                        startTransfer(
                            DragAndDropTransferData(
                                ClipData.newPlainText("application", app.id.toString())
                            )
                        )
                    }
                )
            }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                app.job?.title ?: "Unknown Role",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Rounded.Business, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
                Text(
                    app.job?.company ?: "Unknown Company",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1
                )
            }

            if (app.atsReportId != null) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Rounded.Description, null, Modifier.size(12.dp), tint = AivanceTheme.colors.success)
                    Text("ATS Optimized", style = MaterialTheme.typography.labelSmall, color = AivanceTheme.colors.success)
                }
            }

            if (app.tasks.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                val openTasks = app.tasks.count { !it.isCompleted }
                Text(
                    "$openTasks open task${if (openTasks == 1) "" else "s"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun stageColor(stageId: String): Color = when (stageId.uppercase()) {
    "SAVED", "PREPARING" -> AivanceTheme.colors.info
    "APPLIED" -> AivanceTheme.colors.accent
    "INTERVIEW", "INTERVIEWING" -> AivanceTheme.colors.warning
    "OFFER" -> AivanceTheme.colors.success
    "REJECTED", "CLOSED" -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.secondary
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApplicationDetailSheet(
    application: Application,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onNotesChange: (String) -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    // Locally buffer the notes so typing never fights the Room re-emissions;
    // save (debounced) once the user pauses.
    var notesText by remember(application.id) { mutableStateOf(application.notes.orEmpty()) }
    val currentOnNotesChange by rememberUpdatedState(onNotesChange)
    LaunchedEffect(notesText, application.id) {
        if (notesText != application.notes.orEmpty()) {
            kotlinx.coroutines.delay(600)
            currentOnNotesChange(notesText)
        }
    }

    // Flush any pending (debounced) notes when the sheet is dismissed so the
    // last edits are never silently dropped.
    val dismissWithFlush = {
        if (notesText != application.notes.orEmpty()) {
            onNotesChange(notesText)
        }
        onDismiss()
    }

    ModalBottomSheet(onDismissRequest = dismissWithFlush) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            stageColor(application.currentStageId).copy(alpha = 0.15f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Business,
                        null,
                        tint = stageColor(application.currentStageId),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        application.job?.title ?: "Unknown Role",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        application.job?.company ?: "Unknown Company",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Current status + last modified
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(text = application.currentStageId.replace('_', ' '), tone = BannerTone.INFO)
                Text(
                    "Updated ${formatTimestamp(application.lastModified)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            // Timeline
            Text("Timeline", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (application.timeline.isEmpty()) {
                Text(
                    "No activity recorded yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                application.timeline.forEach { event ->
                    TimelineRow(event = event)
                }
            }

            HorizontalDivider()

            // Notes
            Text("Notes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Add follow-up notes, contact details, links…") },
                minLines = 3
            )
            Text(
                "Saved automatically as you type.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Job listing link
            val jobUrl = application.job?.url
            if (!jobUrl.isNullOrBlank()) {
                OutlinedButton(
                    onClick = { uriHandler.openUri(jobUrl) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.OpenInNew, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Open Job Listing")
                }
            }

            // Delete
            TextButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Rounded.DeleteOutline, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Delete Application")
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete application?") },
            text = {
                Text("This removes \"${application.job?.title ?: "this role"}\" and its entire history from your pipeline. This cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TimelineRow(event: TimelineEvent) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(AivanceTheme.colors.accent, RoundedCornerShape(4.dp))
                .padding(top = 20.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                event.title.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            if (!event.description.isNullOrBlank()) {
                Text(
                    event.description.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                formatTimestamp(event.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    return try {
        SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(date)
    } catch (_: Exception) {
        ""
    }
}
