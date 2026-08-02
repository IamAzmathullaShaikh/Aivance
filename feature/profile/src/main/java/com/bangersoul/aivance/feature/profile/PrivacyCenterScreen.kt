package com.bangersoul.aivance.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bangersoul.aivance.core.designsystem.components.ActionButton
import com.bangersoul.aivance.core.designsystem.components.AivanceScreen
import com.bangersoul.aivance.core.designsystem.components.DashboardCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyCenterScreen(
    viewModel: PrivacyViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    AivanceScreen(
        topBar = {
            TopAppBar(
                title = { Text("Privacy & Security", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.PrivacyTip, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Your Data, Your Control", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("AiVance uses on-device encryption to protect your sensitive information.", style = MaterialTheme.typography.bodySmall)
                }
            }

            // Data Portability
            val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
            ) { uri ->
                if (uri != null) {
                    viewModel.importData(uri)
                }
            }

            DashboardCard {
                Column(Modifier.padding(16.dp)) {
                    Text("Data Portability & Backup", fontWeight = FontWeight.Bold)
                    Text("Export or restore an encrypted backup of your resumes, applications, and profile.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ActionButton(
                            text = "Export Backup",
                            onClick = { viewModel.exportData() },
                            icon = Icons.Rounded.Download,
                            modifier = Modifier.weight(1f)
                        )
                        ActionButton(
                            text = "Restore Backup",
                            onClick = { importLauncher.launch(arrayOf("*/*")) },
                            icon = Icons.Rounded.PrivacyTip,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Danger Zone
            Text("Danger Zone", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            DashboardCard {
                Column(Modifier.padding(16.dp)) {
                    Text("Delete All Data", fontWeight = FontWeight.Bold)
                    Text("Permanently remove all local data, credentials, and history. This cannot be undone.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    ActionButton(
                        text = "Wipe Everything",
                        onClick = { showDeleteConfirm = true },
                        icon = Icons.Rounded.DeleteForever,
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (uiState is PrivacyUiState.Success) {
                Text((uiState as PrivacyUiState.Success).message, color = MaterialTheme.colorScheme.primary)
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Are you absolutely sure?") },
            text = { Text("This will delete all your resumes, applications, and settings forever.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAllData()
                    showDeleteConfirm = false
                }) {
                    Text("Yes, Delete Everything", color = MaterialTheme.colorScheme.error)
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
