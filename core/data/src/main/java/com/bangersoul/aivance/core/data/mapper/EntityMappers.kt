package com.bangersoul.aivance.core.data.mapper

import com.bangersoul.aivance.core.common.enums.AIModel
import com.bangersoul.aivance.core.common.enums.ApplicationStatus
import com.bangersoul.aivance.core.common.enums.JobType
import com.bangersoul.aivance.core.common.enums.MessageRole
import com.bangersoul.aivance.core.common.enums.ResumeStatus
import com.bangersoul.aivance.core.common.enums.InterviewDifficulty
import com.bangersoul.aivance.core.common.enums.LetterTone
import com.bangersoul.aivance.core.common.enums.MessageSender
import com.bangersoul.aivance.core.common.model.AIConversation
import com.bangersoul.aivance.core.common.model.AIMessage
import com.bangersoul.aivance.core.common.model.AiProviderConfig
import com.bangersoul.aivance.core.common.model.AnalyticsEvent
import com.bangersoul.aivance.core.common.model.CareerRoadmap
import com.bangersoul.aivance.core.common.model.Company
import com.bangersoul.aivance.core.common.model.CoverLetter
import com.bangersoul.aivance.core.common.model.InterviewFeedback
import com.bangersoul.aivance.core.common.model.InterviewMessage
import com.bangersoul.aivance.core.common.model.InterviewSession
import com.bangersoul.aivance.core.common.model.JobApplication
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.model.Resume
import com.bangersoul.aivance.core.common.model.ResumeSection
import com.bangersoul.aivance.core.common.model.RoadmapStep
import com.bangersoul.aivance.core.common.model.UserProfile
import com.bangersoul.aivance.core.database.model.AIConversationEntity
import com.bangersoul.aivance.core.database.model.AIConversationWithMessages
import com.bangersoul.aivance.core.database.model.AIMessageEntity
import com.bangersoul.aivance.core.database.model.AnalyticsEventEntity
import com.bangersoul.aivance.core.database.model.CompanyEntity
import com.bangersoul.aivance.core.database.model.CoverLetterEntity
import com.bangersoul.aivance.core.database.model.InterviewMessageEntity
import com.bangersoul.aivance.core.database.model.InterviewSessionEntity
import com.bangersoul.aivance.core.database.model.InterviewSessionWithMessages
import com.bangersoul.aivance.core.database.model.JobApplicationEntity
import com.bangersoul.aivance.core.database.model.JobApplicationWithDetails
import com.bangersoul.aivance.core.database.model.JobEntity
import com.bangersoul.aivance.core.database.model.JobWithDetails
import com.bangersoul.aivance.core.database.model.ProviderConfigurationEntity
import com.bangersoul.aivance.core.database.model.ResumeAnalysisEntity
import com.bangersoul.aivance.core.database.model.ResumeEntity
import com.bangersoul.aivance.core.database.model.ResumeSectionEntity
import com.bangersoul.aivance.core.database.model.RoadmapEntity
import com.bangersoul.aivance.core.database.model.RoadmapStepEntity
import com.bangersoul.aivance.core.database.model.RoadmapWithSteps
import com.bangersoul.aivance.core.database.model.UserProfileEntity
import java.time.Instant

/**
 * Mappers to convert Room Entities to Domain Models and vice versa.
 */

fun ResumeEntity.toDomain(sections: List<ResumeSectionEntity> = emptyList()): Resume {
    return Resume(
        id = id,
        fileName = name,
        fileUri = "", // Entity doesn't have fileUri
        rawText = text,
        parsedDate = dateCreated,
        status = ResumeStatus.PARSED, // Default status
        sections = sections.map { it.toDomain() }
    )
}

fun ResumeSectionEntity.toDomain(): ResumeSection {
    return ResumeSection(
        sectionType = "GENERAL", // Entity doesn't have type
        title = title,
        content = content
    )
}

fun Resume.toEntity(): ResumeEntity {
    return ResumeEntity(
        id = id,
        name = fileName,
        text = rawText,
        dateCreated = parsedDate,
        lastModified = System.currentTimeMillis()
    )
}

fun ResumeSection.toEntity(resumeId: Long, order: Int): ResumeSectionEntity {
    return ResumeSectionEntity(
        resumeId = resumeId,
        title = title,
        content = content,
        sectionOrder = order
    )
}

fun ResumeAnalysisEntity.toDomain(): AtsResultMapper {
    // The domain model name in DomainModels.kt is AtsResult, but ResumeAnalysis is also there.
    // Prompt says ResumeLocalDataSource uses ResumeDao and AtsDao.
    // AtsDao uses ResumeAnalysisEntity.
    // ResumeAnalysis domain model is:
    /*
    data class ResumeAnalysis(
        val overallScore: Int,
        val matchingKeywords: List<String> = emptyList(),
        val missingKeywords: List<String> = emptyList(),
        val suggestions: List<String> = emptyList(),
        val matchSummary: String = ""
    )
    */
    // Wait, AtsResult domain model:
    /*
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
    */
    // ResumeAnalysisEntity has: resumeId, jobDescription, score, matchedKeywords, missingKeywords, feedback, date.
    // It seems AtsResult is a better fit.
    return AtsResultMapper(
        id = id,
        score = score,
        date = date,
        resumeName = "", // Needs to be filled from Resume
        matchingKeywords = matchedKeywords.split(",").filter { it.isNotBlank() },
        missingKeywords = missingKeywords.split(",").filter { it.isNotBlank() },
        feedback = feedback
    )
}

// Helper because I can't rename the return type in the middle of writing
typealias AtsResultMapper = com.bangersoul.aivance.core.common.model.AtsResult

fun JobWithDetails.toDomain(): JobListing {
    return JobListing(
        id = job.id.toString(),
        title = job.title,
        company = company.name,
        location = job.location ?: "",
        salaryRange = job.salary,
        jobType = try { JobType.valueOf(job.type ?: "FULL_TIME") } catch (_: Exception) { JobType.FULL_TIME },
        description = job.description ?: "",
        url = "", // Not in entity
        sourceProvider = "LOCAL",
        postedDate = job.postedDate
    )
}

fun CompanyEntity.toDomain(): Company {
    return Company(
        id = id.toString(),
        name = name,
        logoUrl = logoUrl,
        websiteUrl = website,
        industry = industry ?: ""
    )
}

fun JobApplicationWithDetails.toDomain(): JobApplication {
    return JobApplication(
        id = application.id,
        company = job.company.name,
        role = job.job.title,
        status = try { ApplicationStatus.valueOf(application.status) } catch (e: Exception) { ApplicationStatus.SAVED },
        dateApplied = application.dateApplied,
        salaryRange = application.salaryRange ?: "",
        notes = application.notes ?: "",
        lastModified = application.lastModified
    )
}

fun UserProfileEntity.toDomain(): UserProfile {
    return UserProfile(
        id = id,
        fullName = name,
        email = email,
        targetRole = targetRole ?: "",
        skills = skills,
        bio = bio ?: ""
    )
}

fun UserProfile.toEntity(): UserProfileEntity {
    return UserProfileEntity(
        id = id,
        name = fullName,
        email = email,
        currentRole = null,
        skills = skills,
        targetRole = targetRole,
        bio = bio,
        profilePictureUrl = null
    )
}

fun RoadmapWithSteps.toDomain(): CareerRoadmap {
    return CareerRoadmap(
        id = roadmap.id,
        targetRole = roadmap.targetRole,
        currentLevel = roadmap.currentLevel,
        description = roadmap.description,
        steps = steps.map { it.toDomain() }
    )
}

fun RoadmapStepEntity.toDomain(): RoadmapStep {
    return RoadmapStep(
        id = id,
        roadmapId = roadmapId,
        title = title,
        description = description,
        isCompleted = isCompleted,
        stepOrder = stepOrder
    )
}

fun CareerRoadmap.toEntity(): RoadmapEntity {
    return RoadmapEntity(
        id = id,
        targetRole = targetRole,
        currentLevel = currentLevel,
        description = description,
        dateCreated = System.currentTimeMillis(),
        lastModified = System.currentTimeMillis()
    )
}

fun RoadmapStep.toEntity(roadmapId: Long): RoadmapStepEntity {
    return RoadmapStepEntity(
        id = id,
        roadmapId = roadmapId,
        title = title,
        description = description,
        stepOrder = stepOrder,
        isCompleted = isCompleted
    )
}

fun AIConversationWithMessages.toDomain(): AIConversation {
    return AIConversation(
        id = conversation.id,
        title = conversation.title,
        providerId = "default", // Not in entity
        modelName = "default", // Not in entity
        createdDate = conversation.createdAt.toEpochMilli(),
        lastUpdated = conversation.updatedAt.toEpochMilli(),
        messages = messages.map { it.toDomain() }
    )
}

fun AIMessageEntity.toDomain(): AIMessage {
    return AIMessage(
        id = id,
        conversationId = conversationId,
        role = try { MessageRole.valueOf(role) } catch (e: Exception) { MessageRole.USER },
        content = content,
        timestamp = timestamp.toEpochMilli()
    )
}

fun AIMessage.toEntity(): AIMessageEntity {
    return AIMessageEntity(
        id = id,
        conversationId = conversationId,
        role = role.name,
        content = content,
        timestamp = Instant.ofEpochMilli(timestamp)
    )
}

fun ProviderConfigurationEntity.toDomain(): AiProviderConfig {
    return AiProviderConfig(
        providerId = provider,
        apiKey = apiKey,
        selectedModel = settings["selectedModel"] ?: AIModel.GEMINI_1_5_FLASH.name,
        temperature = settings["temperature"]?.toFloatOrNull() ?: 0.7f,
        maxTokens = settings["maxTokens"]?.toIntOrNull() ?: 2048,
        customBaseUrl = baseUrl
    )
}

fun AnalyticsEventEntity.toDomain(): AnalyticsEvent {
    return AnalyticsEvent(
        id = id.toString(),
        eventName = eventName,
        timestamp = timestamp.toEpochMilli(),
        properties = params
    )
}

fun AnalyticsEvent.toEntity(): AnalyticsEventEntity {
    return AnalyticsEventEntity(
        eventName = eventName,
        params = properties,
        timestamp = Instant.ofEpochMilli(timestamp)
    )
}

fun CoverLetterEntity.toDomain(): CoverLetter {
    return CoverLetter(
        id = id.toLong(),
        company = company,
        role = role,
        content = content,
        dateCreated = dateCreated,
        tone = try { LetterTone.valueOf(tone) } catch (e: Exception) { LetterTone.PROFESSIONAL }
    )
}

fun CoverLetter.toEntity(): CoverLetterEntity {
    return CoverLetterEntity(
        id = id.toInt(),
        company = company,
        role = role,
        content = content,
        dateCreated = dateCreated,
        tone = tone.name
    )
}

fun InterviewSessionWithMessages.toDomain(): InterviewSession {
    return InterviewSession(
        id = session.id.toString(),
        targetRole = session.targetRole,
        companyName = "", // Not in entity
        difficulty = try { InterviewDifficulty.valueOf(session.difficulty) } catch (e: Exception) { InterviewDifficulty.MEDIUM },
        startTime = session.dateStarted.toEpochMilli(),
        isCompleted = session.isCompleted,
        messages = messages.map { it.toDomain() },
        feedback = session.overallFeedback
    )
}

fun InterviewMessageEntity.toDomain(): InterviewMessage {
    return InterviewMessage(
        id = id.toString(),
        sender = try { MessageSender.valueOf(role) } catch (e: Exception) { MessageSender.AI_INTERVIEWER },
        text = text,
        timestamp = timestamp.toEpochMilli()
    )
}

fun InterviewSession.toEntity(): InterviewSessionEntity {
    return InterviewSessionEntity(
        id = id.toLongOrNull() ?: 0L,
        targetRole = targetRole,
        difficulty = difficulty.name,
        dateStarted = Instant.ofEpochMilli(startTime),
        isCompleted = isCompleted,
        overallFeedback = feedback
    )
}

fun InterviewMessage.toEntity(sessionId: Long): InterviewMessageEntity {
    return InterviewMessageEntity(
        id = id.toLongOrNull() ?: 0L,
        sessionId = sessionId,
        role = sender.name,
        text = text,
        timestamp = Instant.ofEpochMilli(timestamp)
    )
}
