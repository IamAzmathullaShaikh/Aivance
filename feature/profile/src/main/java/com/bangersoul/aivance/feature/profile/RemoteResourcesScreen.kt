package com.bangersoul.aivance.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bangersoul.aivance.core.designsystem.components.AivanceScreen
import com.bangersoul.aivance.core.designsystem.components.DashboardCard

data class ResourceLink(
    val title: String,
    val description: String,
    val url: String,
    val category: String
)

object ResourceHubCatalog {
    val resources = listOf(
        ResourceLink("RemoteOK", "Global remote tech jobs & developer roles", "https://remoteok.com", "Job Boards"),
        ResourceLink("Remotive", "Hand-screened remote jobs in tech, design, marketing", "https://remotive.com", "Job Boards"),
        ResourceLink("Jobicy", "Free global remote job search engine", "https://jobicy.com", "Job Boards"),
        ResourceLink("Arbeitnow", "Germany & European remote-friendly job board", "https://arbeitnow.com", "Job Boards"),
        ResourceLink("Awesome Remote Jobs", "Community-curated remote work resources", "https://github.com/lukasz-madon/awesome-remote-job", "Curated Lists"),
        ResourceLink("Tech Interview Handbook", "Free curated guide for coding & system design interviews", "https://techinterviewhandbook.org", "Interview Prep"),
        ResourceLink("System Design Primer", "Learn how to design large-scale systems", "https://github.com/donnemartin/system-design-primer", "Interview Prep")
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteResourcesScreen(
    onBack: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    AivanceScreen(
        topBar = {
            TopAppBar(
                title = { Text("Remote Work Resources", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.WorkOutline, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Remote Work & Career Hub", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Curated reference platforms and interview prep guides", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            items(ResourceHubCatalog.resources) { item ->
                DashboardCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onOpenUrl(item.url) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Badge { Text(item.category) }
                            Spacer(Modifier.height(4.dp))
                            Text(item.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(item.description, style = MaterialTheme.typography.bodyMedium)
                        }
                        Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = "Open Link", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
