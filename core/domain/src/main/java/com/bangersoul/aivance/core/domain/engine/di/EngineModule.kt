package com.bangersoul.aivance.core.domain.engine.di

import com.bangersoul.aivance.core.domain.analytics.RecommendationEngine
import com.bangersoul.aivance.core.domain.engine.*
import com.bangersoul.aivance.core.domain.repository.*
import com.bangersoul.aivance.core.domain.workflow.WorkflowEngine
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EngineModule {

    @Provides
    @Singleton
    fun provideCareerStateEngine(
        userRepository: UserRepository,
        resumeRepository: ResumeRepository,
        workflowRepository: ApplicationWorkflowRepository,
        analyticsRepository: AnalyticsRepository,
        providerManager: ProviderManager
    ): CareerStateEngine = CareerStateEngine(
        userRepository,
        resumeRepository,
        workflowRepository,
        analyticsRepository,
        providerManager
    )

    @Provides
    @Singleton
    fun provideContextEngine(
        resumeRepository: ResumeRepository,
        jobRepository: JobRepository
    ): ContextEngine = ContextEngine(resumeRepository, jobRepository)

    @Provides
    @Singleton
    fun provideIntentEngine(): IntentEngine = IntentEngine()

    @Provides
    @Singleton
    fun providePromptOrchestrator(
        contextEngine: ContextEngine
    ): PromptOrchestrator = PromptOrchestrator(contextEngine)

    @Provides
    @Singleton
    fun provideNavigationWorkflowEngine(): NavigationWorkflowEngine = NavigationWorkflowEngine()

    @Provides
    @Singleton
    fun provideWorkflowEngine(
        repository: ApplicationWorkflowRepository,
        analyticsRepository: AnalyticsRepository,
        taskGenerator: com.bangersoul.aivance.core.domain.usecase.workflow.TaskGeneratorUseCase
    ): WorkflowEngine = WorkflowEngine(repository, analyticsRepository, taskGenerator)

    @Provides
    @Singleton
    fun provideRecommendationEngine(
        providerManager: ProviderManager
    ): RecommendationEngine = RecommendationEngine(providerManager)
}
