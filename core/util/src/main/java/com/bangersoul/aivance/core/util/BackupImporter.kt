package com.bangersoul.aivance.core.util

import android.content.Context
import android.net.Uri
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.security.EncryptedString
import com.bangersoul.aivance.core.database.AivanceDatabase
import com.bangersoul.aivance.core.database.model.CoverLetterEntity
import com.bangersoul.aivance.core.database.model.JobApplicationEntity
import com.bangersoul.aivance.core.database.model.JobEntity
import com.bangersoul.aivance.core.database.model.ResumeEntity
import com.bangersoul.aivance.core.database.model.ResumeSectionEntity
import com.bangersoul.aivance.core.database.model.ResumeVersionEntity
import com.bangersoul.aivance.core.database.model.UserProfileEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AivanceDatabase,
    private val json: Json
) {
    suspend fun importBackup(
        uri: Uri,
        passphrase: String = BackupExporter.DEFAULT_PASSPHRASE
    ): CoreResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@withContext Result.Failure(DomainError("Could not read backup file"))

            val jsonString = BackupExporter.decryptBytes(bytes, passphrase)
            val payload = json.decodeFromString<AivanceBackupPayload>(jsonString)

            // Restore User Profiles
            payload.profiles.forEach { p ->
                database.profileDao().insertProfile(
                    UserProfileEntity(
                        id = p.id,
                        name = p.name,
                        email = p.email,
                        currentRole = p.currentRole,
                        targetRole = p.targetRole,
                        skills = p.skills
                    )
                )
            }

            // Restore Resumes and Versions
            payload.resumes.forEach { r ->
                val resumeId = database.resumeDao().insertResume(
                    ResumeEntity(
                        id = r.id,
                        name = r.name,
                        primaryVersionId = r.versions.firstOrNull()?.id,
                        fileName = null,
                        originalFileUri = null,
                        rawText = if (r.text.isNotBlank()) EncryptedString(r.text) else null,
                        dateCreated = System.currentTimeMillis(),
                        lastModified = System.currentTimeMillis()
                    )
                )
                r.versions.forEach { v ->
                    val versionId = database.resumeDao().insertVersion(
                        ResumeVersionEntity(
                            id = v.id,
                            resumeId = resumeId,
                            versionName = v.versionName,
                            templateId = "modern",
                            lastModified = System.currentTimeMillis()
                        )
                    )
                    val sections = v.sections.map { s ->
                        ResumeSectionEntity(
                            versionId = versionId,
                            title = s.title,
                            content = s.content,
                            sectionOrder = s.sectionOrder,
                            sectionType = "general"
                        )
                    }
                    database.resumeDao().insertSections(sections)
                }
            }

            // Restore Cover Letters
            payload.coverLetters.forEach { c ->
                database.coverLetterDao().insertCoverLetter(
                    CoverLetterEntity(
                        id = c.id,
                        resumeVersionId = null,
                        jobId = null,
                        recruiterId = null,
                        primaryVersionId = null,
                        company = c.company,
                        role = c.role,
                        dateCreated = c.dateCreated
                    )
                )
            }

            // Restore Jobs
            payload.jobs.forEach { j ->
                database.jobDao().insertJob(
                    JobEntity(
                        id = j.id,
                        companyId = j.companyId,
                        title = j.title,
                        location = j.location,
                        type = null,
                        remoteType = null,
                        experienceLevel = null,
                        salaryMin = null,
                        salaryMax = null,
                        currency = null,
                        description = j.description,
                        descriptionHtml = null,
                        url = j.url,
                        sourceProviderId = "BACKUP",
                        postedDate = System.currentTimeMillis()
                    )
                )
            }

            // Restore Applications
            payload.applications.forEach { a ->
                database.trackerDao().insertApplication(
                    JobApplicationEntity(
                        id = a.id,
                        jobId = a.jobId,
                        status = a.status,
                        dateApplied = a.dateApplied,
                        salaryRange = null,
                        notes = null,
                        lastModified = System.currentTimeMillis()
                    )
                )
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Backup import failed")
            Result.Failure(DomainError(e.message ?: "Backup import failed", e))
        }
    }
}
