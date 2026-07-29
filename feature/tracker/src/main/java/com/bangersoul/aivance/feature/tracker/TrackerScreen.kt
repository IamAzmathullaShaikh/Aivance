package com.bangersoul.aivance.feature.tracker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bangersoul.aivance.core.designsystem.components.ActionButton
import com.bangersoul.aivance.core.designsystem.components.AivanceScreen
import com.bangersoul.aivance.core.designsystem.components.DashboardCard
import com.bangersoul.aivance.core.designsystem.components.MetricChip
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme
import com.bangersoul.aivance.core.designsystem.theme.DarkAccent
import com.bangersoul.aivance.core.designsystem.theme.DarkError
import com.bangersoul.aivance.core.designsystem.theme.DarkSurface
import com.bangersoul.aivance.core.designsystem.theme.Zinc700
import com.bangersoul.aivance.core.designsystem.theme.Zinc800
import com.bangersoul.aivance.feature.tracker.domain.ApplicationStatus
import com.bangersoul.aivance.feature.tracker.domain.JobApplication
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen(
    viewModel: TrackerViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddBottomSheet by remember { mutableStateOf(false) }

    AivanceScreen(
        topBar = {
            TopAppBar(
                title = { Text(text = "Job Tracker", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = { showAddBottomSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Job")
            }
        },
        isLoading = uiState is TrackerUiState.Loading,
        error = (uiState as? TrackerUiState.Error)?.message,
        isEmpty = (uiState as? TrackerUiState.Success)?.applications?.isEmpty() == true,
        emptyTitle = "No Applications Yet",
        emptyDescription = "Start tracking your career journey by adding your first job application."
    ) {
        if (uiState is TrackerUiState.Success) {
            val applications = (uiState as TrackerUiState.Success).applications
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(applications, key = { it.id }) { application ->
                    JobApplicationItem(
                        application = application,
                        onDelete = { viewModel.deleteApplication(it) },
                        onUpdateStatus = { id, status -> viewModel.updateStatus(id, status) }
                    )
                }
            }
        }
    }

    if (showAddBottomSheet) {
        AddJobBottomSheet(
            onDismiss = { showAddBottomSheet = false },
            onAdd = { company, role, status ->
                viewModel.addApplication(company, role, status)
                showAddBottomSheet = false
            }
        )
    }
}

@Composable
fun JobApplicationItem(
    application: JobApplication,
    onDelete: (Long) -> Unit,
    onUpdateStatus: (Long, ApplicationStatus) -> Unit
) {
    DashboardCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = application.role,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = application.company,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                Box {
                    var showStatusMenu by remember { mutableStateOf(false) }
                    ApplicationStatusChip(
                        status = application.status,
                        onClick = { showStatusMenu = true }
                    )
                    DropdownMenu(
                        expanded = showStatusMenu,
                        onDismissRequest = { showStatusMenu = false },
                        modifier = Modifier.background(DarkSurface)
                    ) {
                        ApplicationStatus.entries.forEach { s ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = s.name.lowercase().replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.bodyMedium
                                    ) 
                                },
                                onClick = {
                                    onUpdateStatus(application.id, s)
                                    showStatusMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = formatInstant(application.dateApplied),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    if (application.salaryRange != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Payments,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = application.salaryRange ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { onDelete(application.id) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ApplicationStatusChip(
    status: ApplicationStatus,
    onClick: () -> Unit = {}
) {
    val (containerColor, contentColor) = when (status) {
        ApplicationStatus.APPLIED -> Zinc800 to MaterialTheme.colorScheme.onSurface
        ApplicationStatus.SCREENING -> DarkAccent.copy(alpha = 0.1f) to DarkAccent
        ApplicationStatus.INTERVIEWING -> Color(0xFF8B5CF6).copy(alpha = 0.1f) to Color(0xFF8B5CF6)
        ApplicationStatus.OFFER -> Color(0xFF10B981).copy(alpha = 0.1f) to Color(0xFF10B981)
        ApplicationStatus.REJECTED -> DarkError.copy(alpha = 0.1f) to DarkError
        ApplicationStatus.GHOSTED -> Color(0xFFF59E0B).copy(alpha = 0.1f) to Color(0xFFF59E0B)
    }
    MetricChip(
        label = status.name.lowercase().replaceFirstChar { it.uppercase() },
        containerColor = containerColor,
        contentColor = contentColor,
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddJobBottomSheet(
    onDismiss: () -> Unit,
    onAdd: (String, String, ApplicationStatus) -> Unit
) {
    var company by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(ApplicationStatus.APPLIED) }
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Zinc700) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Track New Application",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Company Name",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    placeholder = { Text("e.g. Google, Meta", color = MaterialTheme.colorScheme.secondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Role / Position",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    placeholder = { Text("e.g. Android Engineer", color = MaterialTheme.colorScheme.secondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Current Status",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ApplicationStatus.entries.toTypedArray()) { s ->
                        FilterChip(
                            selected = status == s,
                            onClick = { status = s },
                            label = { Text(s.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            shape = RoundedCornerShape(100.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            ActionButton(
                text = "Add Application",
                onClick = { if (company.isNotBlank() && role.isNotBlank()) onAdd(company, role, status) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

fun formatInstant(instant: Instant): String {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        .withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun TrackerScreenPreview() {
    AivanceTheme(darkTheme = true) {
        // We can't easily preview with a real ViewModel, but we can preview components
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            JobApplicationItem(
                application = JobApplication(
                    id = 1,
                    company = "Google",
                    role = "Android Engineer",
                    status = ApplicationStatus.INTERVIEWING,
                    dateApplied = Instant.now(),
                    salaryRange = "$180k - $220k",
                    notes = null,
                    lastModified = Instant.now()
                ),
                onDelete = {},
                onUpdateStatus = { _, _ -> }
            )
        }
    }
}
