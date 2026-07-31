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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bangersoul.aivance.core.common.model.Application
import com.bangersoul.aivance.core.common.model.ApplicationStage
import com.bangersoul.aivance.core.designsystem.components.*
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme

@Composable
fun TrackerScreen(
    viewModel: TrackerViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                    onMove = { appId, stageId ->
                        viewModel.onEvent(TrackerUiEvent.UpdateStage(appId, stageId))
                    }
                )
                else -> {}
            }
        }
    }
}

@Composable
private fun PipelineBoard(
    stages: List<ApplicationStage>,
    applications: List<Application>,
    onMove: (Long, String) -> Unit
) {
    if (stages.isEmpty()) {
        AivanceEmptyState(
            title = "No pipeline stages",
            description = "Pipeline stages could not be loaded.",
            icon = Icons.Rounded.ViewKanban
        )
        return
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
                onMove = onMove
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PipelineColumn(
    stage: ApplicationStage,
    applications: List<Application>,
    onMove: (Long, String) -> Unit
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
                        KanbanCard(app = app)
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
private fun KanbanCard(app: Application) {
    DashboardCard(
        modifier = Modifier
            .fillMaxWidth()
            .dragAndDropSource {
                // Long-press to lift the card into a drag payload.
                detectTapGestures(
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
