package com.bangersoul.aivance.feature.jobs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bangersoul.aivance.core.common.enums.EmploymentType
import com.bangersoul.aivance.core.common.enums.RemoteType
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.model.JobSearchFilter
import com.bangersoul.aivance.core.designsystem.components.*
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme

@Composable
fun JobsScreen(
    viewModel: JobsViewModel,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToSavedJobs: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        AivanceTopBar(
            title = stringResource(R.string.job_discovery_title),
            subtitle = stringResource(R.string.job_discovery_subtitle)
        )

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                // Deliberately NOT searched on every keystroke — results only
                // refresh when the user commits the query (Enter / search key)
                // or applies a filter. This keeps provider calls intentional
                // and prevents "random results" from firing mid-typing.
                onValueChange = { searchQuery = it },
                placeholder = { Text(stringResource(R.string.search_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    Row {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                viewModel.onEvent(JobsUiEvent.Search(""))
                            }) {
                                Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.clear_search))
                            }
                        }
                        IconButton(onClick = {
                            viewModel.onEvent(JobsUiEvent.Search(searchQuery))
                        }) {
                            Icon(
                                Icons.Rounded.Send,
                                contentDescription = stringResource(R.string.search),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                singleLine = true,
                shape = AivanceTheme.shapes.large,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { viewModel.onEvent(JobsUiEvent.Search(searchQuery)) }
                )
            )

            Spacer(Modifier.height(12.dp))

            // Dropdown filter bar — every dimension is applied client-side so
            // results always reflect the user's selection, not provider dumps.
            JobFilterBar(
                filter = (uiState as? JobsUiState.Success)?.filter ?: JobSearchFilter(),
                onFilterChange = { viewModel.onEvent(JobsUiEvent.UpdateFilter(it)) },
                onClear = { viewModel.onEvent(JobsUiEvent.ClearFilters) }
            )

            Spacer(Modifier.height(12.dp))

            AnimatedContent(
                targetState = uiState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "JobsListTransition"
            ) { state ->
                when (state) {
                    is JobsUiState.Loading -> SkeletonList(itemCount = 6, showAvatar = true)
                    is JobsUiState.Error -> AivanceError(
                        message = state.message,
                        onRetry = { viewModel.onEvent(JobsUiEvent.Refresh) },
                        title = stringResource(R.string.jobs_unavailable)
                    )
                    is JobsUiState.Success -> JobDiscoveryList(
                        jobs = state.jobs,
                        isSearching = state.isSearching,
                        onJobClick = onNavigateToDetails,
                        onBookmarkClick = { viewModel.onEvent(JobsUiEvent.ToggleBookmark(it)) },
                        onRefresh = { viewModel.onEvent(JobsUiEvent.Refresh) },
                        onSavedJobs = onNavigateToSavedJobs
                    )
                    else -> {}
                }
            }
        }
    }
}

/**
 * Dropdown filter bar for the Job Search tab.
 *
 * Four filter groups:
 *  1. Location — cascading Country → State → City dropdowns.
 *  2. Type — Full Time / Part Time / Internship / Apprenticeship / Contract.
 *  3. Workplace — On Site / Remote / Hybrid.
 *  4. Experience — numeric year buckets (0–2 … 15+).
 */
@Composable
private fun JobFilterBar(
    filter: JobSearchFilter,
    onFilterChange: (JobSearchFilter) -> Unit,
    onClear: () -> Unit
) {
    var countryExpanded by remember { mutableStateOf(false) }
    var stateExpanded by remember { mutableStateOf(false) }
    var cityExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }
    var workplaceExpanded by remember { mutableStateOf(false) }
    var experienceExpanded by remember { mutableStateOf(false) }

    val hasActiveFilters =
        filter.hasStructuredLocation ||
            filter.location.isNotBlank() ||
            filter.remoteType != null ||
            filter.employmentTypes.isNotEmpty() ||
            filter.experienceLevels.isNotEmpty() ||
            filter.minExperienceYears != null ||
            filter.maxExperienceYears != null

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterDropdown(
                    label = if (filter.country.isNotBlank()) filter.country else stringResource(R.string.country),
                    expanded = countryExpanded,
                    onExpandedChange = { countryExpanded = it },
                    options = LocationCatalog.countryOptions,
                    selected = filter.country,
                    modifier = Modifier.weight(1f)
                ) { selected ->
                    val newState = LocationCatalog.statesFor(selected).firstOrNull().orEmpty()
                    val newCity = LocationCatalog.citiesFor(selected, newState).firstOrNull().orEmpty()
                    onFilterChange(
                        filter.copy(
                            country = selected,
                            state = if (selected == LocationCatalog.REMOTE) "" else newState,
                            city = if (selected == LocationCatalog.REMOTE) "" else newCity,
                            location = if (selected == LocationCatalog.REMOTE) "Remote" else ""
                        )
                    )
                }
                FilterDropdown(
                    label = if (filter.state.isNotBlank()) filter.state else stringResource(R.string.state),
                    expanded = stateExpanded,
                    onExpandedChange = { stateExpanded = it },
                    options = LocationCatalog.statesFor(filter.country),
                    selected = filter.state,
                    modifier = Modifier.weight(1f),
                    enabled = filter.country.isNotBlank() && filter.country != LocationCatalog.REMOTE
                ) { selected ->
                    val newCity = LocationCatalog.citiesFor(filter.country, selected).firstOrNull().orEmpty()
                    onFilterChange(filter.copy(state = selected, city = newCity))
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterDropdown(
                    label = if (filter.city.isNotBlank()) filter.city else stringResource(R.string.city),
                    expanded = cityExpanded,
                    onExpandedChange = { cityExpanded = it },
                    options = LocationCatalog.citiesFor(filter.country, filter.state),
                    selected = filter.city,
                    modifier = Modifier.weight(1f),
                    enabled = filter.state.isNotBlank()
                ) { selected ->
                    onFilterChange(filter.copy(city = selected))
                }
                val employmentOptions = listOf(
                    EmploymentType.FULL_TIME, EmploymentType.PART_TIME,
                    EmploymentType.INTERNSHIP, EmploymentType.APPRENTICESHIP,
                    EmploymentType.CONTRACT
                ).associateWith { it.uiLabel() }
                FilterDropdown(
                    label = filter.employmentTypes.firstOrNull()?.let { it.uiLabel() }
                        ?: stringResource(R.string.type),
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it },
                    options = employmentOptions.values.toList(),
                    selected = filter.employmentTypes.firstOrNull()?.let { it.uiLabel() }.orEmpty(),
                    modifier = Modifier.weight(1f)
                ) { selected ->
                    val mapped = employmentOptions.entries.firstOrNull { it.value == selected }?.key
                    onFilterChange(
                        filter.copy(employmentTypes = if (mapped != null) listOf(mapped) else emptyList())
                    )
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val remoteOptions = listOf(
                    RemoteType.ON_SITE, RemoteType.REMOTE, RemoteType.HYBRID
                ).associateWith { it.uiLabel() }
                FilterDropdown(
                    label = filter.remoteType?.let { it.uiLabel() } ?: stringResource(R.string.workplace),
                    expanded = workplaceExpanded,
                    onExpandedChange = { workplaceExpanded = it },
                    options = remoteOptions.values.toList(),
                    selected = filter.remoteType?.let { it.uiLabel() }.orEmpty(),
                    modifier = Modifier.weight(1f)
                ) { selected ->
                    val mapped = remoteOptions.entries.firstOrNull { it.value == selected }?.key
                    onFilterChange(filter.copy(remoteType = mapped))
                }
                val experienceOptions = ExperienceBuckets.options.associateWith { stringResource(it.labelRes) }
                FilterDropdown(
                    label = experienceLabel(filter),
                    expanded = experienceExpanded,
                    onExpandedChange = { experienceExpanded = it },
                    options = experienceOptions.values.toList(),
                    selected = experienceLabel(filter),
                    modifier = Modifier.weight(1f)
                ) { selected ->
                    val bucket = experienceOptions.entries.firstOrNull { it.value == selected }?.key
                    onFilterChange(
                        filter.copy(
                            minExperienceYears = bucket?.min,
                            maxExperienceYears = bucket?.max
                        )
                    )
                }
            }
        }
        if (hasActiveFilters) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TextButton(onClick = onClear) {
                        Icon(Icons.Rounded.FilterAltOff, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.clear_all_filters))
                    }
                }
            }
        }
    }
}

/** A single Material-3 exposed dropdown used for one filter dimension. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDropdown(
    label: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    options: List<String>,
    selected: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onSelect: (String) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(if (selected.isBlank()) stringResource(R.string.all) else selected) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, style = MaterialTheme.typography.bodySmall) },
                    onClick = {
                        onExpandedChange(false)
                        onSelect(option)
                    }
                )
            }
        }
    }
}

private data class ExperienceBucket(val labelRes: Int, val min: Int, val max: Int)

private object ExperienceBuckets {
    val options = listOf(
        ExperienceBucket(R.string.exp_0_2, 0, 2),
        ExperienceBucket(R.string.exp_3_5, 3, 5),
        ExperienceBucket(R.string.exp_6_10, 6, 10),
        ExperienceBucket(R.string.exp_11_15, 11, 15),
        ExperienceBucket(R.string.exp_15_plus, 15, 99)
    )
}

@Composable
private fun experienceLabel(filter: JobSearchFilter): String {
    val min = filter.minExperienceYears ?: return stringResource(R.string.experience)
    val max = filter.maxExperienceYears ?: return stringResource(R.string.years_min_plus, min)
    return ExperienceBuckets.options.firstOrNull { it.min == min && it.max == max }?.let {
        stringResource(it.labelRes)
    } ?: stringResource(R.string.years_range, min, max)
}

@Composable
private fun EmploymentType.uiLabel(): String = stringResource(
    when (this) {
        EmploymentType.FULL_TIME -> R.string.employment_full_time
        EmploymentType.PART_TIME -> R.string.employment_part_time
        EmploymentType.INTERNSHIP -> R.string.employment_internship
        EmploymentType.APPRENTICESHIP -> R.string.employment_apprenticeship
        EmploymentType.CONTRACT -> R.string.employment_contract
        EmploymentType.TEMPORARY -> R.string.employment_temporary
        EmploymentType.FREELANCE -> R.string.employment_freelance
        EmploymentType.OTHER -> R.string.employment_other
    }
)

@Composable
private fun RemoteType.uiLabel(): String = stringResource(
    when (this) {
        RemoteType.ON_SITE -> R.string.remote_on_site
        RemoteType.REMOTE -> R.string.remote
        RemoteType.HYBRID -> R.string.remote_hybrid
        RemoteType.OTHER -> R.string.employment_other
    }
)

@Composable
private fun JobDiscoveryList(
    jobs: List<JobListing>,
    isSearching: Boolean,
    onJobClick: (String) -> Unit,
    onBookmarkClick: (String) -> Unit,
    onRefresh: () -> Unit,
    onSavedJobs: () -> Unit
) {
    if (jobs.isEmpty()) {
        AivanceEmptyState(
            title = if (isSearching) stringResource(R.string.no_matches_found) else stringResource(R.string.no_jobs_yet),
            description = if (isSearching) {
                stringResource(R.string.no_matches_desc)
            } else {
                stringResource(R.string.no_jobs_desc)
            },
            icon = Icons.Rounded.WorkOff,
            primaryActionText = stringResource(R.string.refresh),
            onPrimaryAction = onRefresh,
            secondaryActionText = stringResource(R.string.saved_jobs),
            onSecondaryAction = onSavedJobs
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(jobs, key = { it.id }) { job ->
                JobDiscoveryCard(
                    job = job,
                    onClick = { onJobClick(job.id) },
                    onBookmarkClick = { onBookmarkClick(job.id) }
                )
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun JobDiscoveryCard(
    job: JobListing,
    onClick: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    DashboardCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = AivanceTheme.shapes.medium,
                color = AivanceTheme.colors.accent.copy(alpha = 0.12f),
                modifier = Modifier.size(44.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(
                        Icons.Rounded.Business,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = AivanceTheme.colors.accent
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    job.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetaText(job.company)
                    if (job.location.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(Icons.Rounded.LocationOn, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
                            MetaText(job.location)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (job.remoteType == RemoteType.REMOTE || job.isRemote) {
                        StatusChip(text = stringResource(R.string.remote), tone = BannerTone.INFO)
                    }
                    job.salaryRange?.let {
                        StatusChip(text = it, tone = BannerTone.SUCCESS)
                    }
                    val matchScore = job.matchScore
                    if (matchScore != null) {
                        StatusChip(text = stringResource(R.string.ats_match, matchScore), tone = BannerTone.WARNING)
                    }
                    if (job.employmentType != EmploymentType.FULL_TIME) {
                        StatusChip(text = job.employmentType.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }, tone = BannerTone.INFO)
                    }
                }
            }

            IconButton(onClick = onBookmarkClick) {
                Icon(
                    Icons.Rounded.BookmarkBorder,
                    contentDescription = stringResource(R.string.save_job),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MetaText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
