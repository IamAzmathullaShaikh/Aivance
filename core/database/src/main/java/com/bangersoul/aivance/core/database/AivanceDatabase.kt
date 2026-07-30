package com.bangersoul.aivance.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bangersoul.aivance.core.database.converter.AivanceConverters
import com.bangersoul.aivance.core.database.dao.AiAnalyticsDao
import com.bangersoul.aivance.core.database.dao.AivanceDao
import com.bangersoul.aivance.core.database.dao.AtsDao
import com.bangersoul.aivance.core.database.dao.CompanyDao
import com.bangersoul.aivance.core.database.dao.CoverLetterDao
import com.bangersoul.aivance.core.database.dao.InterviewDao
import com.bangersoul.aivance.core.database.dao.JobDao
import com.bangersoul.aivance.core.database.dao.ProfileDao
import com.bangersoul.aivance.core.database.dao.ResumeDao
import com.bangersoul.aivance.core.database.dao.RoadmapDao
import com.bangersoul.aivance.core.database.dao.SearchDao
import com.bangersoul.aivance.core.database.dao.TrackerDao
import com.bangersoul.aivance.core.database.model.AIConversationEntity
import com.bangersoul.aivance.core.database.model.AIMessageEntity
import com.bangersoul.aivance.core.database.model.AivanceEntity
import com.bangersoul.aivance.core.database.model.AnalyticsEventEntity
import com.bangersoul.aivance.core.database.model.CompanyEntity
import com.bangersoul.aivance.core.database.model.CoverLetterEntity
import com.bangersoul.aivance.core.database.model.InterviewMessageEntity
import com.bangersoul.aivance.core.database.model.InterviewSessionEntity
import com.bangersoul.aivance.core.database.model.JobApplicationEntity
import com.bangersoul.aivance.core.database.model.JobEntity
import com.bangersoul.aivance.core.database.model.ProviderConfigurationEntity
import com.bangersoul.aivance.core.database.model.ResumeAnalysisEntity
import com.bangersoul.aivance.core.database.model.ResumeEntity
import com.bangersoul.aivance.core.database.model.ResumeSectionEntity
import com.bangersoul.aivance.core.database.model.RoadmapEntity
import com.bangersoul.aivance.core.database.model.RoadmapStepEntity
import com.bangersoul.aivance.core.database.model.SavedSearchEntity
import com.bangersoul.aivance.core.database.model.UserProfileEntity

@Database(
    entities = [
        AivanceEntity::class,
        CompanyEntity::class,
        JobEntity::class,
        JobApplicationEntity::class,
        ResumeEntity::class,
        ResumeSectionEntity::class,
        ResumeAnalysisEntity::class,
        CoverLetterEntity::class,
        RoadmapEntity::class,
        RoadmapStepEntity::class,
        UserProfileEntity::class,
        InterviewSessionEntity::class,
        InterviewMessageEntity::class,
        AIConversationEntity::class,
        AIMessageEntity::class,
        ProviderConfigurationEntity::class,
        AnalyticsEventEntity::class,
        SavedSearchEntity::class
    ],
    version = 9,
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

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Schema migration 1 -> 2
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Schema migration 2 -> 3
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Schema migration 3 -> 4
            }
        }

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

        val ALL_MIGRATIONS = arrayOf(
            MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
            MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9
        )
    }
}
