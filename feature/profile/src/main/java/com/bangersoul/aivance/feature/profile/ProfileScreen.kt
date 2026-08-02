package com.bangersoul.aivance.feature.profile

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import java.io.File
import com.bangersoul.aivance.core.designsystem.components.*
import com.bangersoul.aivance.core.designsystem.theme.AivanceTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Profile — full-screen career profile reached from the Dashboard avatar.
 *
 * Layout: header (avatar + name + designation/company), Personal section
 * (contact details), Career section (role, company, experience, skills), and
 * a Settings shortcut. Editing is gated behind an Edit/Save toggle; email is
 * read-only after authentication.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit = {},
    onNavigateToInterview: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToProviders: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToLearning: () -> Unit = {},
    onNavigateToSavedJobs: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            // Copy the picked image into app storage and persist the file path —
            // a raw content:// URI grant does not survive process death.
            val savedPath = copyToAppStorage(context, uri)
            savedPath?.let { viewModel.onEvent(ProfileUiEvent.UpdateProfilePicture(it)) }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AivanceTopBar(
            title = stringResource(R.string.profile_title),
            onBack = onBack,
            actions = {
                val state = uiState as? ProfileUiState.Success
                if (state != null) {
                    if (state.isEditing) {
                        TextButton(
                            onClick = { viewModel.onEvent(ProfileUiEvent.ToggleEdit) }
                        ) {
                            Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(
                            onClick = { viewModel.onEvent(ProfileUiEvent.SaveProfile) },
                            enabled = state.isDirty && !state.isSaving
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Text(stringResource(R.string.save), fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Edit affordance — without it the fields could never be edited.
                        TextButton(
                            onClick = { viewModel.onEvent(ProfileUiEvent.ToggleEdit) }
                        ) {
                            Icon(Icons.Rounded.Edit, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.edit), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        )
        when (val state = uiState) {
            is ProfileUiState.Loading -> SkeletonList(itemCount = 5, showAvatar = true, modifier = Modifier.fillMaxSize())
            is ProfileUiState.Error -> AivanceError(
                message = state.message,
                onRetry = { viewModel.onEvent(ProfileUiEvent.LoadProfile) }
            )
            is ProfileUiState.Success -> ProfileContent(
                state = state,
                onEvent = viewModel::onEvent,
                onPickPhoto = { photoPicker.launch("image/*") },
                onNavigateToInterview = onNavigateToInterview,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToProviders = onNavigateToProviders,
                onNavigateToNotifications = onNavigateToNotifications,
                onNavigateToAnalytics = onNavigateToAnalytics,
                onNavigateToLearning = onNavigateToLearning,
                onNavigateToSavedJobs = onNavigateToSavedJobs
            )
            else -> {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ProfileContent(
    state: ProfileUiState.Success,
    onEvent: (ProfileUiEvent) -> Unit,
    onPickPhoto: () -> Unit,
    onNavigateToInterview: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProviders: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToLearning: () -> Unit,
    onNavigateToSavedJobs: () -> Unit
) {
    val isEditing = state.isEditing
    val skills = state.skills.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Header ─────────────────────────────────────────────────────────
        item {
            DashboardCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar — tappable when editing to pick a photo
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(AivanceTheme.colors.accent.copy(alpha = 0.14f))
                            .clickable(enabled = isEditing, onClick = onPickPhoto),
                        contentAlignment = Alignment.Center
                    ) {
                        val photoUrl = state.profilePictureUrl
                        if (!photoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = stringResource(R.string.profile_photo_desc),
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                initials(state.fullName),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = AivanceTheme.colors.accent
                            )
                        }
                        if (isEditing) {
                            Surface(
                                shape = CircleShape,
                                color = AivanceTheme.colors.accent,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(32.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.PhotoCamera,
                                    contentDescription = stringResource(R.string.change_photo),
                                    tint = AivanceTheme.colors.onAccent,
                                    modifier = Modifier.padding(7.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Text(
                        state.fullName.ifBlank { stringResource(R.string.your_name_placeholder) },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    val designation = buildString {
                        append(state.currentRole.ifBlank { stringResource(R.string.add_current_role) })
                        if (state.company.isNotBlank()) append(stringResource(R.string.designation_at)).append(state.company)
                    }
                    Text(
                        designation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    if (isEditing) {
                        Text(
                            stringResource(R.string.tap_camera_hint),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        }

        // ── Personal ───────────────────────────────────────────────────────
        item { SectionHeader(title = stringResource(R.string.personal_section)) }
        item {
            DashboardCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = state.fullName,
                        onValueChange = { onEvent(ProfileUiEvent.UpdateFullName(it)) },
                        label = { Text(stringResource(R.string.full_name)) },
                        enabled = isEditing,
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Email is immutable after auth — locked
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.email)) },
                        enabled = false,
                        readOnly = true,
                        leadingIcon = { Icon(Icons.Rounded.Lock, null, modifier = Modifier.size(18.dp)) },
                        supportingText = { Text(stringResource(R.string.email_locked)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.phone,
                        onValueChange = { onEvent(ProfileUiEvent.UpdatePhone(it)) },
                        label = { Text(stringResource(R.string.phone_optional)) },
                        enabled = isEditing,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Date of birth picker
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = formatDate(state.dateOfBirth),
                            onValueChange = {},
                            label = { Text(stringResource(R.string.date_of_birth)) },
                            enabled = false,
                            readOnly = true,
                            modifier = Modifier.weight(1f)
                        )
                        if (isEditing) {
                            ProfileDatePickerButton(
                                initial = state.dateOfBirth,
                                onPicked = { onEvent(ProfileUiEvent.UpdateDateOfBirth(it)) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = state.linkedinUrl,
                        onValueChange = { onEvent(ProfileUiEvent.UpdateLinkedIn(it)) },
                        label = { Text(stringResource(R.string.linkedin_optional)) },
                        enabled = isEditing,
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(capitalization = KeyboardCapitalization.None),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.githubUrl,
                        onValueChange = { onEvent(ProfileUiEvent.UpdateGithub(it)) },
                        label = { Text(stringResource(R.string.github_optional)) },
                        enabled = isEditing,
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(capitalization = KeyboardCapitalization.None),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // ── Career ─────────────────────────────────────────────────────────
        item { SectionHeader(title = stringResource(R.string.career_section)) }
        item {
            DashboardCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = state.currentRole,
                        onValueChange = { onEvent(ProfileUiEvent.UpdateCurrentRole(it)) },
                        label = { Text(stringResource(R.string.current_role)) },
                        enabled = isEditing,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.company,
                        onValueChange = { onEvent(ProfileUiEvent.UpdateCompany(it)) },
                        label = { Text(stringResource(R.string.company)) },
                        enabled = isEditing,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.targetRole,
                        onValueChange = { onEvent(ProfileUiEvent.UpdateTargetRole(it)) },
                        label = { Text(stringResource(R.string.target_role)) },
                        enabled = isEditing,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Years of experience slider
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.years_experience), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text(
                                stringResource(R.string.years_short, state.experienceYears),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AivanceTheme.colors.accent
                            )
                        }
                        Slider(
                            value = state.experienceYears.toFloat(),
                            onValueChange = { onEvent(ProfileUiEvent.UpdateExperience(it.toInt())) },
                            valueRange = 0f..30f,
                            steps = 29,
                            enabled = isEditing
                        )
                    }

                    // Skills chips
                    Text(stringResource(R.string.skills), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    if (skills.isEmpty() && !isEditing) {
                        Text(
                            stringResource(R.string.no_skills),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            skills.forEach { skill ->
                                InputChip(
                                    selected = false,
                                    onClick = { if (isEditing) onEvent(ProfileUiEvent.UpdateSkills(skills.filterNot { it == skill }.joinToString(", "))) },
                                    label = { Text(skill) },
                                    trailingIcon = if (isEditing) {
                                        {
                                            Icon(
                                                Icons.Rounded.Close,
                                                contentDescription = stringResource(R.string.remove_skill_desc, skill),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null
                                )
                            }
                            if (isEditing) {
                                AddSkillChip(
                                    onAdd = { newSkill ->
                                        val clean = newSkill.trim()
                                        if (clean.isNotBlank() && clean !in skills) {
                                            onEvent(ProfileUiEvent.UpdateSkills((skills + clean).joinToString(", ")))
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Settings shortcut ──────────────────────────────────────────────
        item {
            DashboardCard(onClick = onNavigateToSettings, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = AivanceTheme.shapes.medium,
                        color = AivanceTheme.colors.accent.copy(alpha = 0.12f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(Icons.Rounded.Settings, null, modifier = Modifier.size(20.dp), tint = AivanceTheme.colors.accent)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.settings), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            stringResource(R.string.settings_sub),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Rounded.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // ── Quick access ───────────────────────────────────────────────────
        item {
            SectionHeader(title = stringResource(R.string.quick_access))
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item { ProfilePill(stringResource(R.string.saved_jobs), Icons.Rounded.BookmarkBorder, onNavigateToSavedJobs) }
                item { ProfilePill(stringResource(R.string.interview), Icons.Rounded.RecordVoiceOver, onNavigateToInterview) }
            }
        }
        item {
            ProfileActionCard(stringResource(R.string.career_analytics), stringResource(R.string.career_analytics_sub), Icons.Rounded.BarChart, onNavigateToAnalytics)
        }
        item {
            ProfileActionCard(stringResource(R.string.learning_hub), stringResource(R.string.learning_hub_sub), Icons.Rounded.School, onNavigateToLearning)
        }
        item {
            ProfileActionCard(stringResource(R.string.notifications), stringResource(R.string.notifications_sub), Icons.Rounded.Notifications, onNavigateToNotifications)
        }
        // Provider selection (AI / Job / Enrichment) lives in Provider
        // Management alone — the legacy AI Configuration screen was removed so
        // the dropdowns are never repeated in two places.
        item {
            ProfileActionCard(stringResource(R.string.provider_management), stringResource(R.string.provider_management_sub), Icons.Rounded.Tune, onNavigateToProviders)
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileDatePickerButton(
    initial: Long?,
    onPicked: (Long) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    if (showPicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initial)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let(onPicked)
                        showPicker = false
                    }
                ) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    OutlinedButton(onClick = { showPicker = true }) {
        Icon(Icons.Rounded.CalendarMonth, null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(stringResource(R.string.pick))
    }
}

@Composable
private fun AddSkillChip(onAdd: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    var adding by remember { mutableStateOf(false) }

    if (!adding) {
        InputChip(
            selected = false,
            onClick = { adding = true },
            label = { Text(stringResource(R.string.add_skill)) }
        )
    } else {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text(stringResource(R.string.skill_placeholder)) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    onAdd(text)
                    text = ""
                    adding = false
                },
                enabled = text.isNotBlank()
            ) {
                Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.add_skill_desc))
            }
        }
    }
}

/** Copies a content URI into app-private storage and returns the file path. */
private fun copyToAppStorage(context: Context, uri: Uri): String? {
    return try {
        val input = context.contentResolver.openInputStream(uri) ?: return null
        input.use { stream ->
            val dir = File(context.filesDir, "avatars").apply { mkdirs() }
            val file = File(dir, "avatar_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { output ->
                stream.copyTo(output)
            }
            // Return a file:// URI string — Coil only resolves String models
            // that carry a scheme, so a bare absolute path would fail to load.
            Uri.fromFile(file).toString()
        }
    } catch (_: Exception) {
        null
    }
}

private fun initials(fullName: String): String {
    return fullName.trim().split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull() }
        .joinToString("")
        .uppercase()
        .ifBlank { "?" }
}

private fun formatDate(millis: Long?): String {
    if (millis == null) return ""
    return try {
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(millis))
    } catch (_: Exception) {
        ""
    }
}

@Composable
private fun ProfilePill(label: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = AivanceTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = AivanceTheme.colors.accent)
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ProfileActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
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
                modifier = Modifier.size(40.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = AivanceTheme.colors.accent)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
