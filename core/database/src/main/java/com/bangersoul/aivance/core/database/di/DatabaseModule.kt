package com.bangersoul.aivance.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bangersoul.aivance.core.database.AivanceDatabase
import com.bangersoul.aivance.core.database.DatabaseManager
import com.bangersoul.aivance.core.database.DatabaseSeed
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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AivanceDatabase = Room.databaseBuilder(
        context,
        AivanceDatabase::class.java,
        "aivance-database"
    )
        .addMigrations(
            AivanceDatabase.MIGRATION_1_2,
            AivanceDatabase.MIGRATION_2_3,
            AivanceDatabase.MIGRATION_3_4
        )
        .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
        .setQueryExecutor(Executors.newFixedThreadPool(4))
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun provideDatabaseManager(
        database: AivanceDatabase,
        @ApplicationContext context: Context
    ): DatabaseManager = DatabaseManager(database, context)

    @Provides
    @Singleton
    fun provideDatabaseSeed(
        profileDao: ProfileDao,
        companyDao: CompanyDao,
        jobDao: JobDao
    ): DatabaseSeed = DatabaseSeed(profileDao, companyDao, jobDao)

    @Provides
    fun provideAivanceDao(database: AivanceDatabase): AivanceDao = database.aivanceDao()

    @Provides
    fun provideTrackerDao(database: AivanceDatabase): TrackerDao = database.trackerDao()

    @Provides
    fun provideJobDao(database: AivanceDatabase): JobDao = database.jobDao()

    @Provides
    fun provideCompanyDao(database: AivanceDatabase): CompanyDao = database.companyDao()

    @Provides
    fun provideAtsDao(database: AivanceDatabase): AtsDao = database.atsDao()

    @Provides
    fun provideCoverLetterDao(database: AivanceDatabase): CoverLetterDao = database.coverLetterDao()

    @Provides
    fun provideRoadmapDao(database: AivanceDatabase): RoadmapDao = database.roadmapDao()

    @Provides
    fun provideResumeDao(database: AivanceDatabase): ResumeDao = database.resumeDao()

    @Provides
    fun provideAiAnalyticsDao(database: AivanceDatabase): AiAnalyticsDao = database.aiAnalyticsDao()

    @Provides
    fun provideProfileDao(database: AivanceDatabase): ProfileDao = database.profileDao()

    @Provides
    fun provideInterviewDao(database: AivanceDatabase): InterviewDao = database.interviewDao()

    @Provides
    fun provideSearchDao(database: AivanceDatabase): SearchDao = database.searchDao()
}
