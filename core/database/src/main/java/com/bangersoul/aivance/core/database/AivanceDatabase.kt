package com.bangersoul.aivance.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bangersoul.aivance.core.database.converter.AivanceConverters
import com.bangersoul.aivance.core.database.dao.*
import com.bangersoul.aivance.core.database.model.*

@Database(
    entities = [
        AivanceEntity::class,
        CompanyEntity::class,
        JobEntity::class,
        JobApplicationEntity::class,
        ResumeEntity::class,
        ResumeVersionEntity::class,
        ResumeSectionEntity::class,
        ResumeAnalysisEntity::class,
        CoverLetterEntity::class,
        CoverLetterVersionEntity::class,
        CoverLetterSectionEntity::class,
        RoadmapEntity::class,
        RoadmapStepEntity::class,
        UserProfileEntity::class,
        InterviewSessionEntity::class,
        InterviewMessageEntity::class,
        InterviewQuestionEntity::class,
        InterviewEvaluationEntity::class,
        AIConversationEntity::class,
        AIMessageEntity::class,
        ProviderConfigurationEntity::class,
        AnalyticsEventEntity::class,
        SavedSearchEntity::class,
        JobDescriptionEntity::class,
        AtsReportEntity::class,
        SavedJobEntity::class,
        ViewedJobEntity::class,
        SearchHistoryEntity::class,
        RecruiterEntity::class,
        RecruiterContactEntity::class,
        OutreachDraftEntity::class,
        CommunicationHistoryEntity::class,
        ApplicationEntity::class,
        ApplicationStageEntity::class,
        ApplicationTimelineEntity::class,
        TaskEntity::class,
        AutomationRuleEntity::class,
        AnalyticsSnapshotEntity::class,
        RecommendationEntity::class,
        GoalEntity::class,
        AssistantConversationEntity::class,
        AssistantMessageEntity::class,
        WorkflowExecutionEntity::class,
        AuditLogEntity::class,
        UserEntity::class
    ],
    version = 23,
    exportSchema = true
)
@TypeConverters(AivanceConverters::class)
abstract class AivanceDatabase : RoomDatabase() {
    abstract fun aivanceDao(): AivanceDao
    abstract fun trackerDao(): TrackerDao
    abstract fun jobDao(): JobDao
    abstract fun companyDao(): CompanyDao
    abstract fun atsDao(): AtsDao
    abstract fun coverLetterDao(): CoverLetterDao
    abstract fun roadmapDao(): RoadmapDao
    abstract fun resumeDao(): ResumeDao
    abstract fun aiAnalyticsDao(): AiAnalyticsDao
    abstract fun profileDao(): ProfileDao
    abstract fun interviewDao(): InterviewDao
    abstract fun searchDao(): SearchDao
    abstract fun recruiterDao(): RecruiterDao
    abstract fun workflowDao(): WorkflowDao
    abstract fun analyticsDao(): AnalyticsDao
    abstract fun assistantDao(): AssistantDao
    abstract fun auditDao(): AuditDao
    abstract fun userDao(): UserDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) { override fun migrate(db: SupportSQLiteDatabase) {} }
        val MIGRATION_2_3 = object : Migration(2, 3) { override fun migrate(db: SupportSQLiteDatabase) {} }
        val MIGRATION_3_4 = object : Migration(3, 4) { override fun migrate(db: SupportSQLiteDatabase) {} }
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `user_profiles` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `email` TEXT NOT NULL, `currentRole` TEXT, `skills` TEXT NOT NULL, `targetRole` TEXT, `bio` TEXT, `profilePictureUrl` TEXT, PRIMARY KEY(`id`))")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_user_profiles_email` ON `user_profiles` (`email`)")
            }
        }
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `resumes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `text` TEXT NOT NULL, `dateCreated` INTEGER NOT NULL, `lastModified` INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `resume_sections` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `resumeId` INTEGER NOT NULL, `title` TEXT NOT NULL, `content` TEXT NOT NULL, `sectionOrder` INTEGER NOT NULL, FOREIGN KEY(`resumeId`) REFERENCES `resumes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE TABLE IF NOT EXISTS `resume_analyses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `resumeId` INTEGER NOT NULL, `jobDescription` TEXT NOT NULL, `score` INTEGER NOT NULL, `matchedKeywords` TEXT NOT NULL, `missingKeywords` TEXT NOT NULL, `feedback` TEXT NOT NULL, `date` INTEGER NOT NULL, FOREIGN KEY(`resumeId`) REFERENCES `resumes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_resume_sections_resumeId` ON `resume_sections` (`resumeId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_resume_analyses_resumeId` ON `resume_analyses` (`resumeId`)")
                db.execSQL("DROP TABLE IF EXISTS `ats_results`")
            }
        }
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `interview_sessions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `targetRole` TEXT NOT NULL, `difficulty` TEXT NOT NULL, `dateStarted` INTEGER NOT NULL, `isCompleted` INTEGER NOT NULL, `overallFeedback` TEXT)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `interview_messages` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sessionId` INTEGER NOT NULL, `role` TEXT NOT NULL, `text` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `feedback` TEXT, FOREIGN KEY(`sessionId`) REFERENCES `interview_sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_interview_sessions_targetRole` ON `interview_sessions` (`targetRole`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_interview_messages_sessionId` ON `interview_messages` (`sessionId`)")
            }
        }
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `companies` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `logoUrl` TEXT, `website` TEXT, `industry` TEXT)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `jobs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `companyId` INTEGER NOT NULL, `title` TEXT NOT NULL, `location` TEXT, `type` TEXT, `salary` TEXT, `description` TEXT, `postedDate` INTEGER NOT NULL, FOREIGN KEY(`companyId`) REFERENCES `companies`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE TABLE IF NOT EXISTS `job_applications` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `jobId` INTEGER NOT NULL, `status` TEXT NOT NULL, `dateApplied` INTEGER NOT NULL, `salaryRange` TEXT, `notes` TEXT, `lastModified` INTEGER NOT NULL, FOREIGN KEY(`jobId`) REFERENCES `jobs`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_jobs_companyId` ON `jobs` (`companyId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_job_applications_jobId` ON `job_applications` (`jobId`)")
                db.execSQL("DROP TABLE IF EXISTS `applications`")
            }
        }
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `ai_conversations` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `ai_messages` (`id` TEXT NOT NULL, `conversationId` TEXT NOT NULL, `role` TEXT NOT NULL, `content` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`conversationId`) REFERENCES `ai_conversations`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE TABLE IF NOT EXISTS `provider_configurations` (`provider` TEXT NOT NULL, `apiKey` TEXT NOT NULL, `baseUrl` TEXT, `settings` TEXT NOT NULL, PRIMARY KEY(`provider`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `analytics_events` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `eventName` TEXT NOT NULL, `params` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `saved_searches` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `query` TEXT NOT NULL, `filters` TEXT NOT NULL, `dateCreated` INTEGER NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_ai_messages_conversationId` ON `ai_messages` (`conversationId`)")
            }
        }
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `provider_configurations` ADD COLUMN `type` TEXT NOT NULL DEFAULT 'AI'")
                db.execSQL("ALTER TABLE `provider_configurations` ADD COLUMN `selectedModel` TEXT")
                db.execSQL("ALTER TABLE `provider_configurations` ADD COLUMN `actorId` TEXT")
                db.execSQL("ALTER TABLE `provider_configurations` ADD COLUMN `isEnabled` INTEGER NOT NULL DEFAULT 1")
            }
        }
        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `resume_versions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `resumeId` INTEGER NOT NULL, `versionName` TEXT NOT NULL, `templateId` TEXT NOT NULL, `lastModified` INTEGER NOT NULL, FOREIGN KEY(`resumeId`) REFERENCES `resumes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_resume_versions_resumeId` ON `resume_versions` (`resumeId`)")
                db.execSQL("ALTER TABLE `resumes` ADD COLUMN `primaryVersionId` INTEGER")
                db.execSQL("ALTER TABLE `resumes` ADD COLUMN `fileName` TEXT")
                db.execSQL("ALTER TABLE `resumes` ADD COLUMN `originalFileUri` TEXT")
                db.execSQL("ALTER TABLE `resumes` ADD COLUMN `rawText` TEXT")
                db.execSQL("UPDATE `resumes` SET `rawText` = `text`")
                db.execSQL("INSERT INTO `resume_versions` (resumeId, versionName, templateId, lastModified) SELECT id, 'Main Version', 'modern', lastModified FROM resumes")
                db.execSQL("UPDATE resumes SET primaryVersionId = (SELECT id FROM resume_versions WHERE resume_versions.resumeId = resumes.id LIMIT 1)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `resume_sections_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `versionId` INTEGER NOT NULL, `title` TEXT NOT NULL, `content` TEXT NOT NULL, `sectionOrder` INTEGER NOT NULL, `sectionType` TEXT NOT NULL, FOREIGN KEY(`versionId`) REFERENCES `resume_versions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("INSERT INTO `resume_sections_new` (versionId, title, content, sectionOrder, sectionType) SELECT v.id, s.title, s.content, s.sectionOrder, 'general' FROM resume_sections s JOIN resume_versions v ON s.resumeId = v.resumeId")
                db.execSQL("DROP TABLE `resume_sections`")
                db.execSQL("ALTER TABLE `resume_sections_new` RENAME TO `resume_sections`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_resume_sections_versionId` ON `resume_sections` (`versionId`)")
            }
        }
        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `job_descriptions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `companyName` TEXT, `jobTitle` TEXT, `rawText` TEXT NOT NULL, `sourceUrl` TEXT, `extractedSkills` TEXT, `dateCreated` INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `ats_reports` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `resumeVersionId` INTEGER NOT NULL, `jobDescriptionId` INTEGER NOT NULL, `overallScore` INTEGER NOT NULL, `matchPercentage` INTEGER NOT NULL, `matchedKeywords` TEXT NOT NULL, `missingKeywords` TEXT NOT NULL, `sectionScores` TEXT NOT NULL, `optimizationTips` TEXT NOT NULL, `dateGenerated` INTEGER NOT NULL, FOREIGN KEY(`resumeVersionId\") REFERENCES `resume_versions`(\"id\") ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(\"jobDescriptionId\") REFERENCES `job_descriptions`(\"id\") ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_ats_reports_resumeVersionId` ON `ats_reports` (`resumeVersionId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_ats_reports_jobDescriptionId` ON `ats_reports` (`jobDescriptionId`)")
            }
        }
        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `jobs_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `companyId` INTEGER NOT NULL, `title` TEXT NOT NULL, `location` TEXT, `type` TEXT, `remoteType\" TEXT, \"experienceLevel\" TEXT, \"salaryMin\" REAL, \"salaryMax\" REAL, \"currency\" TEXT, \"description\" TEXT, \"descriptionHtml\" TEXT, \"url\" TEXT NOT NULL, \"sourceProviderId\" TEXT NOT NULL, \"postedDate\" INTEGER NOT NULL, \"expirationDate\" INTEGER, FOREIGN KEY(\"companyId\") REFERENCES `companies`(\"id\") ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("INSERT INTO `jobs_new` (id, companyId, title, location, type, salary, description, postedDate, url, sourceProviderId) SELECT id, companyId, title, location, type, salary, description, postedDate, '', 'UNKNOWN' FROM jobs")
                db.execSQL("DROP TABLE `jobs`")
                db.execSQL("ALTER TABLE `jobs_new` RENAME TO `jobs`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_jobs_companyId` ON `jobs` (`companyId`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `saved_jobs` (`jobId` INTEGER NOT NULL, `dateSaved` INTEGER NOT NULL, PRIMARY KEY(`jobId\"), FOREIGN KEY(\"jobId\") REFERENCES `jobs`(\"id\") ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_saved_jobs_jobId` ON `saved_jobs` (`jobId`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `viewed_jobs\" (\"jobId\" INTEGER NOT NULL, \"lastViewed\" INTEGER NOT NULL, PRIMARY KEY(\"jobId\"), FOREIGN KEY(\"jobId\") REFERENCES `jobs`(\"id\") ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_viewed_jobs_jobId\" ON `viewed_jobs` (\"jobId\")")
                db.execSQL("CREATE TABLE IF NOT EXISTS `search_history\" (\"id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, \"query\" TEXT NOT NULL, \"filtersJson\" TEXT NOT NULL, \"timestamp\" INTEGER NOT NULL)")
            }
        }
        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `companies` ADD COLUMN `domain` TEXT")
                db.execSQL("ALTER TABLE `companies` ADD COLUMN `headquarters\" TEXT")
                db.execSQL("ALTER TABLE `companies` ADD COLUMN `socialLinks\" TEXT")
                db.execSQL("CREATE TABLE IF NOT EXISTS `recruiters\" (\"id\" TEXT NOT NULL, \"companyId\" INTEGER NOT NULL, \"name\" TEXT NOT NULL, \"title\" TEXT, \"department\" TEXT, \"linkedinUrl\" TEXT, \"sourceProvider\" TEXT, \"status\" TEXT NOT NULL DEFAULT \u0027ACTIVE\u0027, PRIMARY KEY(\"id\"), FOREIGN KEY(\"companyId\") REFERENCES `companies`(\"id\") ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_recruiters_companyId\" ON `recruiters` (\"companyId\")")
                db.execSQL("CREATE TABLE IF NOT EXISTS `recruiter_contacts\" (\"id\" TEXT NOT NULL, \"recruiterId\" TEXT NOT NULL, \"email\" TEXT NOT NULL, \"confidence\" INTEGER NOT NULL, \"isVerified\" INTEGER NOT NULL, \"lastUpdated\" INTEGER NOT NULL, PRIMARY KEY(\"id\"), FOREIGN KEY(\"recruiterId\") REFERENCES `recruiters`(\"id\") ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_recruiter_contacts_recruiterId\" ON `recruiter_contacts` (\"recruiterId\")")
                db.execSQL("CREATE TABLE IF NOT EXISTS `outreach_drafts\" (\"id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, \"recruiterId\" TEXT NOT NULL, \"jobId\" INTEGER, \"type\" TEXT NOT NULL, \"content\" TEXT NOT NULL, \"dateCreated\" INTEGER NOT NULL, FOREIGN KEY(\"recruiterId\") REFERENCES `recruiters`(\"id\") ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(\"jobId\") REFERENCES `jobs`(\"id\") ON UPDATE NO ACTION ON DELETE SET NULL )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_outreach_drafts_recruiterId\" ON `outreach_drafts` (\"recruiterId\")")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_outreach_drafts_jobId\" ON `outreach_drafts` (\"jobId\")")
                db.execSQL("CREATE TABLE IF NOT EXISTS `communication_history\" (\"id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, \"recruiterId\" TEXT NOT NULL, \"messageType\" TEXT NOT NULL, \"content\" TEXT NOT NULL, \"sentDate\" INTEGER NOT NULL, \"status\" TEXT NOT NULL, \"notes\" TEXT, \"nextFollowUpDate\" INTEGER, FOREIGN KEY(\"recruiterId\") REFERENCES `recruiters`(\"id\") ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_communication_history_recruiterId\" ON `communication_history` (\"recruiterId\")")
            }
        }
        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `cover_letters_new\" (\"id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, \"resumeVersionId\" INTEGER, \"jobId\" INTEGER, \"recruiterId\" TEXT, \"primaryVersionId\" INTEGER, \"company\" TEXT NOT NULL, \"role\" TEXT NOT NULL, \"dateCreated\" INTEGER NOT NULL)")
                db.execSQL("INSERT INTO `cover_letters_new\" (id, company, role, dateCreated) SELECT id, company, role, dateCreated FROM cover_letters")
                db.execSQL("DROP TABLE `cover_letters`")
                db.execSQL("ALTER TABLE `cover_letters_new\" RENAME TO `cover_letters`")
                db.execSQL("CREATE TABLE IF NOT EXISTS `cover_letter_versions\" (\"id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, \"coverLetterId\" INTEGER NOT NULL, \"versionName\" TEXT NOT NULL, \"templateId\" TEXT NOT NULL, \"writingStyle\" TEXT NOT NULL, \"state\" TEXT NOT NULL, \"lastModified\" INTEGER NOT NULL, FOREIGN KEY(\"coverLetterId\") REFERENCES `cover_letters`(\"id\") ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_cover_letter_versions_coverLetterId\" ON `cover_letter_versions` (\"coverLetterId\")")
                db.execSQL("CREATE TABLE IF NOT EXISTS `cover_letter_sections\" (\"id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, \"versionId\" INTEGER NOT NULL, \"sectionType\" TEXT NOT NULL, \"title\" TEXT NOT NULL, \"content\" TEXT NOT NULL, \"sectionOrder\" INTEGER NOT NULL, FOREIGN KEY(\"versionId\") REFERENCES `cover_letter_versions`(\"id\") ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_cover_letter_sections_versionId\" ON `cover_letter_sections` (\"versionId\")")
            }
        }
        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `interview_sessions_new\" (\"id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, \"resumeVersionId\" INTEGER, \"jobId\" INTEGER, \"targetRole\" TEXT NOT NULL, \"type\" TEXT NOT NULL DEFAULT \u0027BEHAVIORAL\u0027, \"difficulty\" TEXT NOT NULL, \"dateStarted\" INTEGER NOT NULL, \"dateEnded\" INTEGER, \"isCompleted\" INTEGER NOT NULL, \"overallFeedback\" TEXT, FOREIGN KEY(\"resumeVersionId\") REFERENCES `resume_versions`(\"id\") ON UPDATE NO ACTION ON DELETE SET NULL , FOREIGN KEY(\"jobId\") REFERENCES `jobs`(\"id\") ON UPDATE NO ACTION ON DELETE SET NULL )")
                db.execSQL("INSERT INTO `interview_sessions_new\" (id, targetRole, difficulty, dateStarted, isCompleted, overallFeedback) SELECT id, targetRole, difficulty, dateStarted, isCompleted, overallFeedback FROM interview_sessions")
                db.execSQL("DROP TABLE `interview_sessions`")
                db.execSQL("ALTER TABLE `interview_sessions_new\" RENAME TO `interview_sessions`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_interview_sessions_targetRole\" ON `interview_sessions\" (\"targetRole\")")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_interview_sessions_resumeVersionId\" ON `interview_sessions\" (\"resumeVersionId\")")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_interview_sessions_jobId\" ON `interview_sessions\" (\"jobId\")")
                db.execSQL("CREATE TABLE IF NOT EXISTS `interview_questions\" (\"id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, \"sessionId\" INTEGER, \"text\" TEXT NOT NULL, \"category\" TEXT NOT NULL, \"difficulty\" TEXT NOT NULL, \"expectedKeyPoints\" TEXT, \"idealAnswer\" TEXT, FOREIGN KEY(\"sessionId\") REFERENCES `interview_sessions`(\"id\") ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_interview_questions_sessionId\" ON `interview_questions\" (\"sessionId\")")
                db.execSQL("CREATE TABLE IF NOT EXISTS `interview_evaluations\" (\"id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, \"messageId\" INTEGER NOT NULL, \"scoreClarity\" INTEGER NOT NULL, \"scoreAccuracy\" INTEGER NOT NULL, \"scoreTone\" INTEGER NOT NULL, \"starMethodScore\" INTEGER, \"feedback\" TEXT NOT NULL, \"improvementTips\" TEXT, FOREIGN KEY(\"messageId\") REFERENCES `interview_messages`(\"id\") ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_interview_evaluations_messageId\" ON `interview_evaluations\" (\"messageId\")")
            }
        }
        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `applications\" (\"id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, \"jobId\" INTEGER NOT NULL, \"resumeVersionId\" INTEGER, \"atsReportId\" INTEGER, \"coverLetterVersionId\" INTEGER, \"currentStageId\" TEXT NOT NULL DEFAULT \u0027SAVED\u0027, \"status\" TEXT NOT NULL DEFAULT \u0027ACTIVE\u0027, \"dateApplied\" INTEGER, \"lastModified\" INTEGER NOT NULL, \"notes\" TEXT, FOREIGN KEY(\"jobId\") REFERENCES `jobs`(\"id\") ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(\"resumeVersionId\") REFERENCES `resume_versions`(\"id\") ON UPDATE NO ACTION ON DELETE SET NULL , FOREIGN KEY(\"atsReportId\") REFERENCES `ats_reports\"(\"id\") ON UPDATE NO ACTION ON DELETE SET NULL , FOREIGN KEY(\"coverLetterVersionId\") REFERENCES `cover_letter_versions\"(\"id\") ON UPDATE NO ACTION ON DELETE SET NULL )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_applications_jobId\" ON `applications\" (\"jobId\")")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_applications_resumeVersionId\" ON `applications\" (\"resumeVersionId\")")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_applications_atsReportId\" ON `applications\" (\"atsReportId\")")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_applications_coverLetterVersionId\" ON `applications\" (\"coverLetterVersionId\")")
                db.execSQL("INSERT INTO `applications\" (\"jobId\", \"currentStageId\", \"dateApplied\", \"lastModified\", \"notes\") SELECT jobId, status, dateApplied, lastModified, notes FROM job_applications")
                db.execSQL("DROP TABLE `job_applications`")
                db.execSQL("CREATE TABLE IF NOT EXISTS `application_stages\" (\"id\" TEXT NOT NULL, \"label\" TEXT NOT NULL, \"order\" INTEGER NOT NULL, \"isSystemStage\" INTEGER NOT NULL DEFAULT 1, PRIMARY KEY(\"id\"))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `application_timeline\" (\"id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, \"applicationId\" INTEGER NOT NULL, \"eventType\" TEXT NOT NULL, \"title\" TEXT NOT NULL, \"description\" TEXT, \"timestamp\" INTEGER NOT NULL, \"metadataJson\" TEXT, FOREIGN KEY(\"applicationId\") REFERENCES `applications\"(\"id\") ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_application_timeline_applicationId\" ON `application_timeline\" (\"applicationId\")")
                db.execSQL("CREATE TABLE IF NOT EXISTS `application_tasks\" (\"id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, \"applicationId\" INTEGER NOT NULL, \"title\" TEXT NOT NULL, \"description\" TEXT, \"priority\" TEXT NOT NULL DEFAULT \u0027MEDIUM\u0027, \"dueDate\" INTEGER, \"isCompleted\" INTEGER NOT NULL DEFAULT 0, \"completedAt\" INTEGER, \"createdAt\" INTEGER NOT NULL, FOREIGN KEY(\"applicationId\") REFERENCES `applications\"(\"id\") ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_application_tasks_applicationId\" ON `application_tasks\" (\"applicationId\")")
                db.execSQL("CREATE TABLE IF NOT EXISTS `automation_rules\" (\"id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, \"triggerType\" TEXT NOT NULL, \"triggerValue\" TEXT, \"actionType\" TEXT NOT NULL, \"actionParamsJson\" TEXT NOT NULL, \"isEnabled\" INTEGER NOT NULL DEFAULT 1)")
            }
        }
        val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `analytics_snapshots\" (\"id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, \"timestamp\" INTEGER NOT NULL, \"kpiJson\" TEXT NOT NULL, \"careerScore\" INTEGER NOT NULL, \"dimensionScoresJson\" TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `recommendations\" (\"id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, \"title\" TEXT NOT NULL, \"description\" TEXT NOT NULL, \"priority\" TEXT NOT NULL, \"category\" TEXT NOT NULL, \"actionDeepLink\" TEXT, \"evidenceJson\" TEXT, \"isDismissed\" INTEGER NOT NULL, \"timestamp\" INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `career_goals\" (\"id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, \"title\" TEXT NOT NULL, \"description\" TEXT, \"targetValue\" REAL NOT NULL, \"currentValue\" REAL NOT NULL, \"unit\" TEXT NOT NULL, \"deadline\" INTEGER, \"isCompleted\" INTEGER NOT NULL, \"type\" TEXT NOT NULL)")
            }
        }
        val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `assistant_conversations` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `activeJobId` INTEGER, `activeResumeVersionId` INTEGER, `lastIntent` TEXT, `createdAt` INTEGER NOT NULL, `lastUpdatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `assistant_messages` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `conversationId` TEXT NOT NULL, `role` TEXT NOT NULL, `content` TEXT NOT NULL, `actionButtonsJson` TEXT, `timestamp` INTEGER NOT NULL, FOREIGN KEY(`conversationId`) REFERENCES `assistant_conversations`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_assistant_messages_conversationId` ON `assistant_messages` (`conversationId`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `workflow_executions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `conversationId` TEXT NOT NULL, `workflowType` TEXT NOT NULL, `currentStep` INTEGER NOT NULL, `totalSteps` INTEGER NOT NULL, `status` TEXT NOT NULL, `stateJson` TEXT, FOREIGN KEY(`conversationId`) REFERENCES `assistant_conversations`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_workflow_executions_conversationId` ON `workflow_executions` (`conversationId`)")
            }
        }

        val MIGRATION_20_21 = object : Migration(20, 21) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `users` (`id` TEXT NOT NULL, `googleId` TEXT NOT NULL, `email` TEXT NOT NULL, `firstName` TEXT NOT NULL, `lastName` TEXT NOT NULL, `phone` TEXT, `photoUrl` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_users_email` ON `users` (`email`)")
            }
        }

        val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `interview_questions` ADD COLUMN `isFavorite` INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_22_23 = object : Migration(22, 23) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `user_profiles` ADD COLUMN `company` TEXT")
                db.execSQL("ALTER TABLE `user_profiles` ADD COLUMN `linkedinUrl` TEXT")
                db.execSQL("ALTER TABLE `user_profiles` ADD COLUMN `githubUrl` TEXT")
                db.execSQL("ALTER TABLE `user_profiles` ADD COLUMN `dateOfBirth` INTEGER")
            }
        }

        val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create audit_logs table
                db.execSQL("CREATE TABLE IF NOT EXISTS `audit_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `action` TEXT NOT NULL, `module\" TEXT NOT NULL, \"timestamp\" INTEGER NOT NULL, \"metadataJson\" TEXT, \"severity\" TEXT NOT NULL DEFAULT \u0027INFO\u0027)")

                // 2. Remove apiKey from provider_configurations
                db.execSQL("CREATE TABLE IF NOT EXISTS `provider_configurations_new` (`provider` TEXT NOT NULL, `type` TEXT NOT NULL, `baseUrl` TEXT, `selectedModel` TEXT, `actorId` TEXT, `settings` TEXT NOT NULL, `isEnabled` INTEGER NOT NULL, PRIMARY KEY(`provider`))")
                db.execSQL("INSERT INTO `provider_configurations_new` (provider, type, baseUrl, selectedModel, actorId, settings, isEnabled) SELECT provider, type, baseUrl, selectedModel, actorId, settings, isEnabled FROM provider_configurations")
                db.execSQL("DROP TABLE `provider_configurations`")
                db.execSQL("ALTER TABLE `provider_configurations_new` RENAME TO `provider_configurations`")
            }
        }

        val ALL_MIGRATIONS = arrayOf(
            MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
            MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
            MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13,
            MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17,
            MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21,
            MIGRATION_21_22, MIGRATION_22_23
        )
    }
}
