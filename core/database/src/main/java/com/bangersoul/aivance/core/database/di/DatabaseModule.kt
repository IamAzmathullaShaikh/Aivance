package com.bangersoul.aivance.core.database.di

import android.content.Context
import androidx.room.Room
import com.bangersoul.aivance.core.database.AivanceDatabase
import com.bangersoul.aivance.core.database.dao.AivanceDao
import com.bangersoul.aivance.core.database.dao.ApplicationDao
import com.bangersoul.aivance.core.database.dao.AtsDao
import com.bangersoul.aivance.core.database.dao.CoverLetterDao
import com.bangersoul.aivance.core.database.dao.RoadmapDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
    ).build()

    @Provides
    fun provideAivanceDao(database: AivanceDatabase): AivanceDao = database.aivanceDao()

    @Provides
    fun provideApplicationDao(database: AivanceDatabase): ApplicationDao = database.applicationDao()

    @Provides
    fun provideAtsDao(database: AivanceDatabase): AtsDao = database.atsDao()

    @Provides
    fun provideCoverLetterDao(database: AivanceDatabase): CoverLetterDao = database.coverLetterDao()

    @Provides
    fun provideRoadmapDao(database: AivanceDatabase): RoadmapDao = database.roadmapDao()
}
