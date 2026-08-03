package com.bangersoul.aivance.feature.resume

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bangersoul.aivance.core.designsystem.components.*
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme

@Composable
fun IntelligenceHubScreen(
    viewModel: ResumeEngineViewModel,
    onNavigateToEngine: () -> Unit,
    onNavigateToAts: (String?) -> Unit,
    onBack: () -> Unit
) {
    // Note: Assuming we have a way to get the resume list and recent ATS scans.
    // For now, focusing on the UI structure using the new scaffold.

    AivanceWorkspaceScaffold(
        title = "Intelligence Hub",
        subtitle = "Manage your career product",
        onBack = onBack,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToEngine,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Resume")
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionHeader(title = "Your Resumes")
                Spacer(Modifier.height(8.dp))
                // Placeholder for resume list
                AivanceWorkspaceCard(
                    onClick = onNavigateToEngine
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Rounded.Description, contentDescription = null, tint = AivanceTheme.colors.accent)
                        Column {
                            Text("Main Resume", fontWeight = FontWeight.Bold)
                            Text("Last updated 2 days ago", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            item {
                SectionHeader(title = "Recent ATS Scans")
                Spacer(Modifier.height(8.dp))
                // Placeholder for ATS history
                AivanceWorkspaceCard(
                    onClick = { onNavigateToAts(null) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Rounded.History, contentDescription = null, tint = AivanceTheme.colors.info)
                        Column {
                            Text("Google - Android Engineer", fontWeight = FontWeight.Bold)
                            Text("85% Match · Jul 30", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
