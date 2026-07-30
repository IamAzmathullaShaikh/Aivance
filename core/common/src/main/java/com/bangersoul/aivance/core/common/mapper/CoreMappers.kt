package com.bangersoul.aivance.core.common.mapper

import com.bangersoul.aivance.core.common.dto.AiMessageDto
import com.bangersoul.aivance.core.common.dto.AnalyticsEventDto
import com.bangersoul.aivance.core.common.dto.ApifyJobScraperDto
import com.bangersoul.aivance.core.common.dto.AtsResultDto
import com.bangersoul.aivance.core.common.dto.CoverLetterDto
import com.bangersoul.aivance.core.common.dto.InterviewFeedbackDto
import com.bangersoul.aivance.core.common.dto.JobListingDto
import com.bangersoul.aivance.core.common.dto.ProviderInfoDto
import com.bangersoul.aivance.core.common.dto.ResumeAnalysisDto
import com.bangersoul.aivance.core.common.dto.SettingsDto
import com.bangersoul.aivance.core.common.dto.UserProfileDto
import com.bangersoul.aivance.core.common.enums.AIModel
import com.bangersoul.aivance.core.common.enums.JobType
import com.bangersoul.aivance.core.common.enums.LetterTone
import com.bangersoul.aivance.core.common.enums.MessageRole
import com.bangersoul.aivance.core.common.enums.ProviderState
import com.bangersoul.aivance.core.common.enums.ProviderType
import com.bangersoul.aivance.core.common.model.AIMessage
import com.bangersoul.aivance.core.common.model.AiProviderConfig
import com.bangersoul.aivance.core.common.model.AnalyticsEvent
import com.bangersoul.aivance.core.common.model.AtsResult
import com.bangersoul.aivance.core.common.model.CoverLetter
import com.bangersoul.aivance.core.common.model.InterviewFeedback
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.model.ProviderInfo
import com.bangersoul.aivance.core.common.model.ResumeAnalysis
import com.bangersoul.aivance.core.common.model.UserProfile

// ResumeAnalysis
fun ResumeAnalysisDto.toDomain(): ResumeAnalysis = ResumeAnalysis(
    overallScore = overallScore,
    matchingKeywords = matchingKeywords,
    missingKeywords = missingKeywords,
    suggestions = suggestions,
    matchSummary = matchSummary
)

fun ResumeAnalysis.toDto(): ResumeAnalysisDto = ResumeAnalysisDto(
    overallScore = overallScore,
    matchingKeywords = matchingKeywords,
    missingKeywords = missingKeywords,
    suggestions = suggestions,
    matchSummary = matchSummary
)

// AtsResult
fun AtsResultDto.toDomain(id: Long = 0): AtsResult = AtsResult(
    id = id,
    score = score,
    date = dateEpoch,
    resumeName = resumeName,
    missingKeywords = missingKeywords,
    feedback = feedback,
    matchingKeywords = matchingKeywords,
    formattingScore = formattingScore
)

fun AtsResult.toDto(): AtsResultDto = AtsResultDto(
    score = score,
    resumeName = resumeName,
    missingKeywords = missingKeywords,
    feedback = feedback,
    matchingKeywords = matchingKeywords,
    formattingScore = formattingScore,
    dateEpoch = date
)

// CoverLetter
fun CoverLetterDto.toDomain(id: Long = 0): CoverLetter = CoverLetter(
    id = id,
    company = company,
    role = role,
    content = content,
    tone = try { LetterTone.valueOf(tone.uppercase()) } catch (e: Exception) { LetterTone.PROFESSIONAL }
)

fun CoverLetter.toDto(): CoverLetterDto = CoverLetterDto(
    company = company,
    role = role,
    content = content,
    tone = tone.name
)

// InterviewFeedback
fun InterviewFeedbackDto.toDomain(): InterviewFeedback = InterviewFeedback(
    overallScore = overallScore,
    strengths = strengths,
    improvements = improvements,
    detailedSummary = detailedSummary
)

fun InterviewFeedback.toDto(): InterviewFeedbackDto = InterviewFeedbackDto(
    overallScore = overallScore,
    strengths = strengths,
    improvements = improvements,
    detailedSummary = detailedSummary
)

// ApifyJobScraperDto
fun ApifyJobScraperDto.toDomain(): JobListing = JobListing(
    id = id,
    title = positionName,
    company = companyName,
    location = location,
    salaryRange = salary,
    jobType = JobType.FULL_TIME,
    isRemote = location.lowercase().contains("remote"),
    description = description,
    url = url,
    sourceProvider = "APIFY",
    postedDate = System.currentTimeMillis()
)

// AIMessage
fun AiMessageDto.toDomain(): AIMessage = AIMessage(
    id = id,
    conversationId = conversationId,
    role = try { MessageRole.valueOf(role.uppercase()) } catch (e: Exception) { MessageRole.USER },
    content = content,
    timestamp = timestamp,
    tokenCount = tokenCount
)

fun AIMessage.toDto(): AiMessageDto = AiMessageDto(
    id = id,
    conversationId = conversationId,
    role = role.name,
    content = content,
    timestamp = timestamp,
    tokenCount = tokenCount
)

// UserProfile
fun UserProfileDto.toDomain(): UserProfile = UserProfile(
    id = id,
    fullName = fullName,
    email = email,
    phone = phone,
    targetRole = targetRole,
    bio = bio,
    location = location,
    skills = skills,
    experienceYears = experienceYears,
    createdDate = createdDate
)

fun UserProfile.toDto(): UserProfileDto = UserProfileDto(
    id = id,
    fullName = fullName,
    email = email,
    phone = phone,
    targetRole = targetRole,
    bio = bio,
    location = location,
    skills = skills,
    experienceYears = experienceYears,
    createdDate = createdDate
)

// ProviderInfo
fun ProviderInfoDto.toDomain(): ProviderInfo = ProviderInfo(
    id = id,
    name = name,
    type = try { ProviderType.valueOf(type.uppercase()) } catch (e: Exception) { ProviderType.AI },
    state = try { ProviderState.valueOf(state.uppercase()) } catch (e: Exception) { ProviderState.UNCONFIGURED },
    supportedModels = supportedModels.mapNotNull {
        try { AIModel.valueOf(it.uppercase()) } catch (e: Exception) { null }
    }
)

fun ProviderInfo.toDto(): ProviderInfoDto = ProviderInfoDto(
    id = id,
    name = name,
    type = type.name,
    state = state.name,
    supportedModels = supportedModels.map { it.name }
)

// AnalyticsEvent
fun AnalyticsEventDto.toDomain(): AnalyticsEvent = AnalyticsEvent(
    id = id,
    eventName = eventName,
    timestamp = timestamp,
    properties = properties,
    category = category
)

fun AnalyticsEvent.toDto(): AnalyticsEventDto = AnalyticsEventDto(
    id = id,
    eventName = eventName,
    timestamp = timestamp,
    properties = properties,
    category = category
)

// JobListing
fun JobListingDto.toDomain(): JobListing = JobListing(
    id = id,
    title = title,
    company = company,
    location = location,
    salaryRange = salaryRange,
    jobType = try { JobType.valueOf(jobType.uppercase()) } catch (e: Exception) { JobType.FULL_TIME },
    isRemote = isRemote,
    description = description,
    url = url,
    sourceProvider = sourceProvider,
    postedDate = postedDate,
    matchScore = matchScore
)

fun JobListing.toDto(): JobListingDto = JobListingDto(
    id = id,
    title = title,
    company = company,
    location = location,
    salaryRange = salaryRange,
    jobType = jobType.name,
    isRemote = isRemote,
    description = description,
    url = url,
    sourceProvider = sourceProvider,
    postedDate = postedDate,
    matchScore = matchScore
)

// Settings
fun SettingsDto.toDomain(): AiProviderConfig = AiProviderConfig(
    providerId = providerId,
    apiKey = apiKey,
    selectedModel = selectedModel,
    temperature = temperature,
    maxTokens = maxTokens,
    customBaseUrl = customBaseUrl,
    isEnabled = isEnabled
)

fun AiProviderConfig.toDto(): SettingsDto = SettingsDto(
    providerId = providerId,
    apiKey = apiKey,
    selectedModel = selectedModel,
    temperature = temperature,
    maxTokens = maxTokens,
    customBaseUrl = customBaseUrl,
    isEnabled = isEnabled
)
