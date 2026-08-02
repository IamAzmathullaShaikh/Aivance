package com.bangersoul.aivance.core.common.enums

import kotlinx.serialization.Serializable

@Serializable
enum class JobType {
    FULL_TIME,
    PART_TIME,
    CONTRACT,
    INTERNSHIP,
    APPRENTICESHIP,
    TEMPORARY,
    FREELANCE,
    OTHER
}

@Serializable
enum class RemoteType {
    ON_SITE,
    REMOTE,
    HYBRID,
    OTHER
}

@Serializable
enum class EmploymentType {
    FULL_TIME,
    PART_TIME,
    CONTRACT,
    INTERNSHIP,
    APPRENTICESHIP,
    TEMPORARY,
    FREELANCE,
    OTHER
}

@Serializable
enum class ExperienceLevel {
    ENTRY_LEVEL,
    MID_LEVEL,
    SENIOR_LEVEL,
    EXECUTIVE,
    NOT_SPECIFIED
}

@Serializable
enum class JobSortOrder {
    RELEVANCE,
    DATE_DESC,
    SALARY_DESC,
    COMPANY_ASC
}

@Serializable
enum class ResumeStatus {
    DRAFT,
    PARSED,
    ANALYZED,
    ARCHIVED,
    PRIMARY
}

@Serializable
enum class ProviderState {
    ACTIVE,
    INACTIVE,
    DEPRECATED,
    UNCONFIGURED,
    ERROR
}

@Serializable
enum class ProviderType {
    AI,
    JOB_SCRAPER,
    STORAGE,
    ANALYTICS
}

@Serializable
enum class AIModel {
    GEMINI_1_5_FLASH,
    GEMINI_1_5_PRO,
    GPT_4O,
    GPT_4O_MINI,
    CLAUDE_3_5_SONNET,
    GROQ_LLAMA_3,
    OLLAMA_LOCAL
}

@Serializable
enum class MessageRole {
    SYSTEM,
    USER,
    ASSISTANT,
    TOOL
}

@Serializable
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

@Serializable
enum class Language {
    ENGLISH,
    SPANISH,
    FRENCH,
    GERMAN,
    CHINESE,
    JAPANESE
}

@Serializable
enum class ApplicationStatus {
    SAVED,
    APPLIED,
    INTERVIEWING,
    OFFER,
    REJECTED,
    WITHDRAWN
}

@Serializable
enum class PermissionState {
    GRANTED,
    DENIED,
    NOT_REQUESTED,
    RATIONALE_REQUIRED
}

@Serializable
enum class LetterTone {
    PROFESSIONAL,
    ENTHUSIASTIC,
    CONFIDENT,
    CREATIVE
}

@Serializable
enum class InterviewDifficulty {
    EASY,
    MEDIUM,
    HARD
}

@Serializable
enum class MessageSender {
    USER,
    AI_INTERVIEWER
}
