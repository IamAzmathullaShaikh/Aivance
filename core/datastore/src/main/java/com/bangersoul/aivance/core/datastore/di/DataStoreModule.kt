package com.bangersoul.aivance.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.bangersoul.aivance.core.datastore.UserPreferences
import com.bangersoul.aivance.core.datastore.UserPreferencesRepository
import com.bangersoul.aivance.core.datastore.UserPreferencesRepositoryImpl
import com.bangersoul.aivance.core.datastore.UserPreferencesSerializer
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataStoreModule {

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        userPreferencesRepositoryImpl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository

    companion object {
        @Provides
        @Singleton
        fun provideUserPreferencesDataStore(
            @ApplicationContext context: Context,
            userPreferencesSerializer: UserPreferencesSerializer
        ): DataStore<UserPreferences> =
            DataStoreFactory.create(
                serializer = userPreferencesSerializer,
            ) {
                context.dataStoreFile("user_preferences.json")
            }
    }
}
