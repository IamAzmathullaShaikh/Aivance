package com.bangersoul.aivance.core.network.di

import com.bangersoul.aivance.core.network.AiService
import com.bangersoul.aivance.core.network.DelegatingAiService
import com.bangersoul.aivance.core.network.MockAiService
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {

    @Binds
    @Singleton
    abstract fun bindAiService(
        delegatingAiService: DelegatingAiService
    ): AiService

    companion object {
        @Provides
        @Singleton
        fun provideGenerativeModel(): GenerativeModel {
            return Firebase.ai(backend = GenerativeBackend.googleAI())
                .generativeModel(
                    modelName = "gemini-2.5-flash"
                )
        }
    }
}
