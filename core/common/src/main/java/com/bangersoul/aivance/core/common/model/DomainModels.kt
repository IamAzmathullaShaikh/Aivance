package com.bangersoul.aivance.core.common.model

import com.bangersoul.aivance.core.common.enums.AIModel
import com.bangersoul.aivance.core.common.enums.ApplicationStatus
import com.bangersoul.aivance.core.common.enums.EmploymentType
import com.bangersoul.aivance.core.common.enums.ExperienceLevel
import com.bangersoul.aivance.core.common.enums.InterviewDifficulty
import com.bangersoul.aivance.core.common.enums.JobSortOrder
import com.bangersoul.aivance.core.common.enums.JobType
import com.bangersoul.aivance.core.common.enums.LetterTone
import com.bangersoul.aivance.core.common.enums.MessageRole
import com.bangersoul.aivance.core.common.enums.MessageSender
import com.bangersoul.aivance.core.common.enums.ProviderState
import com.bangersoul.aivance.core.common.enums.ProviderType
import com.bangersoul.aivance.core.common.enums.RemoteType
import com.bangersoul.aivance.core.common.enums.ResumeStatus
import kotlinx.serialization.Serializable

@Serializable
data class Resume(
    val id: Long = 0,
    val fileName: String,
    val fileUri: String,
    val rawText: String,
    val parsedDate: Long = System.currentTimeMillis(),
    val characterCount: Int = rawText.length,
    val isPrimary: Boolean = false,
    val status: ResumeStatus = ResumeStatus.PARSED,
    val sections: List<ResumeSection> = emptyList()
)

@Serializable
data class ResumeSection(
    val sectionType: String,
    val title: String,
    val content: String
)

@Serializable
data class ResumeAnalysis(
    val overallScore: Int,
    val matchingKeywords: List<String> = emptyList(),
    val missingKeywords: List<String> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val matchSummary: String = ""
)

@Serializable
data class AtsResult(
    val id: Long = 0,
    val score: Int,
    val date: Long = System.currentTimeMillis(),
    val resumeName: String,
    val missingKeywords: List<String> = emptyList(),
    val feedback: String,
    val matchingKeywords: List<String> = emptyList(),
    val formattingScore: Int = 100
)

@Serializable
data class CoverLetter(
    val id: Long = 0,
    val company: String,
    val role: String,
    val content: String,
    val dateCreated: Long = System.currentTimeMillis(),
    val tone: LetterTone = LetterTone.PROFESSIONAL
)

@Serializable
data class InterviewSession(
    val id: String,
    val targetRole: String,
    val companyName: String = "",
    val difficulty: InterviewDifficulty = InterviewDifficulty.MEDIUM,
    val messages: List<InterviewMessage> = emptyList(),
    val feedback: InterviewFeedback? = null,
    val startTime: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
)

@Serializable
data class InterviewMessage(
    val id: String,
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class InterviewFeedback(
    val overallScore: Int,
    val strengths: List<String> = emptyList(),
    val improvements: List<String> = emptyList(),
    val detailedSummary: String = ""
)

@Serializable
data class JobListing(
    val id: String,
    val title: String,
    val company: String,
    val companyLogoUrl: String? = null,
    val location: String = "",
    val salaryMin: Double? = null,
    val salaryMax: Double? = null,
    val currency: String? = "USD",
    val salaryRange: String? = null,
    val employmentType: EmploymentType = EmploymentType.FULL_TIME,
    val experienceLevel: ExperienceLevel = ExperienceLevel.NOT_SPECIFIED,
    val remoteType: RemoteType = RemoteType.ON_SITE,
    val jobType: JobType = JobType.FULL_TIME,
    val isRemote: Boolean = false,
    val description: String,
    val descriptionHtml: String? = null,
    val url: String,
    val sourceUrl: String? = null,
    val sourceProvider: String,
    val postedDate: Long = System.currentTimeMillis(),
    val matchScore: Int? = null
)

@Serializable
data class Company(
    val id: String,
    val name: String,
    val logoUrl: String? = null,
    val websiteUrl: String? = null,
    val location: String = "",
    val industry: String = "",
    val description: String = ""
)

@Serializable
data class JobApplication(
    val id: Long = 0,
    val company: String,
    val role: String,
    val status: ApplicationStatus = ApplicationStatus.SAVED,
    val dateApplied: Long = System.currentTimeMillis(),
    val salaryRange: String = "",
    val notes: String = "",
    val lastModified: Long = System.currentTimeMillis()
)

@Serializable
data class UserProfile(
    val id: String = "user_default",
    val fullName: String,
    val email: String,
    val phone: String = "",
    val targetRole: String = "",
    val bio: String = "",
    val location: String = "",
    val skills: List<String> = emptyList(),
    val experienceYears: Int = 0,
    val createdDate: Long = System.currentTimeMillis()
)

@Serializable
data class CareerRoadmap(
    val id: Long = 0,
    val targetRole: String,
    val currentLevel: String,
    val description: String,
    val steps: List<RoadmapStep> = emptyList()
)

@Serializable
data class RoadmapStep(
    val id: Long = 0,
    val roadmapId: Long = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val stepOrder: Int = 0
)

@Serializable
data class AIConversation(
    val id: String,
    val title: String,
    val providerId: String,
    val modelName: String,
    val createdDate: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val messages: List<AIMessage> = emptyList()
)

@Serializable
data class AIMessage(
    val id: String,
    val conversationId: String,
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val tokenCount: Int = 0
)

@Serializable
data class ProviderInfo(
    val id: String,
    val name: String,
    val type: ProviderType,
    val state: ProviderState = ProviderState.UNCONFIGURED,
    val supportedModels: List<AIModel> = emptyList(),
    val capabilities: List<ProviderCapability> = emptyList()
)

@Serializable
data class ProviderCapability(
    val name: String,
    val isSupported: Boolean = true,
    val maxContextTokens: Int = 8192,
    val supportsStreaming: Boolean = true,
    val supportsVision: Boolean = false,
    val supportsJsonOutput: Boolean = true
)

@Serializable
data class JobSearchFilter(
    val query: String = "",
    val location: String = "",
    val remoteType: RemoteType? = null,
    val employmentTypes: List<EmploymentType> = emptyList(),
    val experienceLevels: List<ExperienceLevel> = emptyList(),
    val minSalary: Double? = null,
    val maxSalary: Double? = null,
    val currency: String = "USD"
)

@Serializable
data class SearchFilter(
    val keywords: String = "",
    val location: String = "",
    val isRemote: Boolean = false,
    val jobType: JobType? = null,
    val minSalary: Int? = null,
    val maxSalary: Int? = null,
    val sourceProvider: String? = null
)

@Serializable
data class SearchResult(
    val id: String,
    val query: String,
    val totalResults: Int,
    val page: Int,
    val items: List<JobListing> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class AnalyticsEvent(
    val id: String,
    val eventName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val properties: Map<String, String> = emptyMap(),
    val category: String = "GENERAL"
)

@Serializable
data class AiProviderConfig(
    val providerId: String,
    val apiKey: String,
    val selectedModel: String,
    val temperature: Float = 0.7f,
    val maxTokens: Int = 2048,
    val customBaseUrl: String? = null,
    val isEnabled: Boolean = true
)

@Serializable
data class JobScraperConfig(
    val providerId: String,
    val apifyToken: String,
    val activeActorId: String,
    val syncIntervalHours: Int = 24,
    val cacheRetentionDays: Int = 7
)
