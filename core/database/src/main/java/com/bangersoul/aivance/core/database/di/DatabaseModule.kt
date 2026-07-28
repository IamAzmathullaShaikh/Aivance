package com.bangersoul.aivance.core.database.di

import android.content.Context
import androidx.room.Room
import com.bangersoul.aivance.core.database.AivanceDatabase
import com.bangersoul.aivance.core.database.dao.AivanceDao
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
}
