package com.bangersoul.aivance.core.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.database.AivanceDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class AivanceBackupPayload(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val resumes: List<BackupResume> = emptyList(),
    val jobs: List<BackupJob> = emptyList(),
    val coverLetters: List<BackupCoverLetter> = emptyList(),
    val applications: List<BackupApplication> = emptyList(),
    val profiles: List<BackupUserProfile> = emptyList()
)

@Serializable
data class BackupResume(
    val id: Long,
    val name: String,
    val text: String = "",
    val versions: List<BackupResumeVersion> = emptyList()
)

@Serializable
data class BackupResumeVersion(
    val id: Long,
    val resumeId: Long,
    val versionName: String,
    val sections: List<BackupResumeSection> = emptyList()
)

@Serializable
data class BackupResumeSection(
    val title: String,
    val content: String,
    val sectionOrder: Int = 0
)

@Serializable
data class BackupJob(
    val id: Long,
    val title: String,
    val companyId: Long = 1,
    val location: String? = null,
    val description: String? = null,
    val url: String = ""
)

@Serializable
data class BackupCoverLetter(
    val id: Long,
    val company: String,
    val role: String,
    val dateCreated: Long
)

@Serializable
data class BackupApplication(
    val id: Long,
    val jobId: Long,
    val status: String,
    val dateApplied: Long = System.currentTimeMillis()
)

@Serializable
data class BackupUserProfile(
    val id: String,
    val name: String,
    val email: String,
    val currentRole: String? = null,
    val targetRole: String? = null,
    val skills: List<String> = emptyList()
)

@Singleton
class BackupExporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AivanceDatabase,
    private val json: Json
) {
    suspend fun exportBackup(passphrase: String = DEFAULT_PASSPHRASE): CoreResult<Uri> = withContext(Dispatchers.IO) {
        try {
            val resumesEntities = database.resumeDao().getResumes().firstOrNull() ?: emptyList()
            val backupResumes = resumesEntities.map { r ->
                val versions = database.resumeDao().getVersionsForResume(r.id).firstOrNull() ?: emptyList()
                val backupVersions = versions.map { v ->
                    val sections = database.resumeDao().getSectionsForVersion(v.id).firstOrNull() ?: emptyList()
                    BackupResumeVersion(
                        id = v.id,
                        resumeId = v.resumeId,
                        versionName = v.versionName,
                        sections = sections.map { BackupResumeSection(it.title, it.content, it.sectionOrder) }
                    )
                }
                BackupResume(id = r.id, name = r.name, text = r.rawText?.value ?: "", versions = backupVersions)
            }

            val jobsWithDetails = database.jobDao().getJobsWithDetails().firstOrNull() ?: emptyList()
            val backupJobs = jobsWithDetails.map { j ->
                BackupJob(id = j.job.id, title = j.job.title, companyId = j.job.companyId, location = j.job.location, description = j.job.description, url = j.job.url)
            }

            val coverLetterEntities = database.coverLetterDao().getCoverLetters().firstOrNull() ?: emptyList()
            val backupCoverLetters = coverLetterEntities.map { c ->
                BackupCoverLetter(id = c.id, company = c.company, role = c.role, dateCreated = c.dateCreated)
            }

            val appEntities = database.trackerDao().getApplications().firstOrNull() ?: emptyList()
            val backupApps = appEntities.map { a ->
                BackupApplication(id = a.application.id, jobId = a.application.jobId, status = a.application.status, dateApplied = a.application.dateApplied)
            }

            val profileEntity = database.profileDao().getUserProfile().firstOrNull()
            val backupProfiles = if (profileEntity != null) {
                listOf(BackupUserProfile(id = profileEntity.id, name = profileEntity.name, email = profileEntity.email, currentRole = profileEntity.currentRole, targetRole = profileEntity.targetRole, skills = profileEntity.skills))
            } else emptyList()

            val payload = AivanceBackupPayload(
                resumes = backupResumes,
                jobs = backupJobs,
                coverLetters = backupCoverLetters,
                applications = backupApps,
                profiles = backupProfiles
            )

            val jsonString = json.encodeToString(payload)
            val encryptedBytes = encryptString(jsonString, passphrase)

            val file = File(context.cacheDir, "aivance_backup_${System.currentTimeMillis()}.aivance_backup")
            FileOutputStream(file).use { it.write(encryptedBytes) }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            Result.Success(uri)
        } catch (e: Exception) {
            Timber.e(e, "Backup export failed")
            Result.Failure(DomainError(e.message ?: "Backup export failed", e))
        }
    }

    companion object {
        const val DEFAULT_PASSPHRASE = "AiVance_Secure_Backup_2026"
        private val SALT = "AiVance_Backup_Salt_V1".toByteArray(Charsets.UTF_8)

        fun encryptString(plainText: String, passphrase: String): ByteArray {
            val keySpec = deriveKey(passphrase)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val iv = ByteArray(12)
            SecureRandom().nextBytes(iv)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, GCMParameterSpec(128, iv))
            val ciphertext = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            return iv + ciphertext
        }

        fun decryptBytes(encryptedData: ByteArray, passphrase: String): String {
            val keySpec = deriveKey(passphrase)
            val iv = encryptedData.copyOfRange(0, 12)
            val ciphertext = encryptedData.copyOfRange(12, encryptedData.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec, GCMParameterSpec(128, iv))
            val decryptedBytes = cipher.doFinal(ciphertext)
            return String(decryptedBytes, Charsets.UTF_8)
        }

        private fun deriveKey(passphrase: String): SecretKeySpec {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val spec = PBEKeySpec(passphrase.toCharArray(), SALT, 10000, 256)
            val secretKey = factory.generateSecret(spec)
            return SecretKeySpec(secretKey.encoded, "AES")
        }
    }
}
