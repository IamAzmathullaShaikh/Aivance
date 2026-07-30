package com.bangersoul.aivance.core.domain.usecase.di

import com.bangersoul.aivance.core.domain.repository.AiRepository
import com.bangersoul.aivance.core.domain.repository.AnalyticsRepository
import com.bangersoul.aivance.core.domain.repository.CoverLetterRepository
import com.bangersoul.aivance.core.domain.repository.InterviewRepository
import com.bangersoul.aivance.core.domain.repository.JobRepository
import com.bangersoul.aivance.core.domain.repository.JobTrackerRepository
import com.bangersoul.aivance.core.domain.repository.ResumeRepository
import com.bangersoul.aivance.core.domain.repository.SettingsRepository
import com.bangersoul.aivance.core.domain.repository.UserRepository
import com.bangersoul.aivance.core.domain.usecase.ai.ClearConversationUseCase
import com.bangersoul.aivance.core.domain.usecase.ai.RegenerateResponseUseCase
import com.bangersoul.aivance.core.domain.usecase.ai.SendMessageUseCase
import com.bangersoul.aivance.core.domain.usecase.ai.StartConversationUseCase
import com.bangersoul.aivance.core.domain.usecase.ai.StreamResponseUseCase
import com.bangersoul.aivance.core.domain.usecase.ai.SummariseConversationUseCase
import com.bangersoul.aivance.core.domain.usecase.analytics.ExportAnalyticsUseCase
import com.bangersoul.aivance.core.domain.usecase.analytics.GenerateUsageReportUseCase
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.career.GenerateCareerRoadmapUseCase
import com.bangersoul.aivance.core.domain.usecase.career.RecommendSkillsUseCase
import com.bangersoul.aivance.core.domain.usecase.career.SuggestLearningPathUseCase
import com.bangersoul.aivance.core.domain.usecase.coverletter.ExportCoverLetterUseCase
import com.bangersoul.aivance.core.domain.usecase.coverletter.GenerateCoverLetterUseCase
import com.bangersoul.aivance.core.domain.usecase.coverletter.ImproveCoverLetterUseCase
import com.bangersoul.aivance.core.domain.usecase.interview.EndInterviewUseCase
import com.bangersoul.aivance.core.domain.usecase.interview.EvaluateAnswersUseCase
import com.bangersoul.aivance.core.domain.usecase.interview.GenerateFeedbackUseCase
import com.bangersoul.aivance.core.domain.usecase.interview.GenerateInterviewQuestionsUseCase
import com.bangersoul.aivance.core.domain.usecase.interview.StartInterviewSessionUseCase
import com.bangersoul.aivance.core.domain.usecase.job.ApplyToJobUseCase
import com.bangersoul.aivance.core.domain.usecase.job.BookmarkJobUseCase
import com.bangersoul.aivance.core.domain.usecase.job.GetJobDetailsUseCase
import com.bangersoul.aivance.core.domain.usecase.job.RemoveSavedJobUseCase
import com.bangersoul.aivance.core.domain.usecase.job.SaveJobUseCase
import com.bangersoul.aivance.core.domain.usecase.job.SearchJobsUseCase
import com.bangersoul.aivance.core.domain.usecase.job.SearchRemoteJobsUseCase
import com.bangersoul.aivance.core.domain.usecase.job.SearchSavedJobsUseCase
import com.bangersoul.aivance.core.domain.usecase.provider.DisableProviderUseCase
import com.bangersoul.aivance.core.domain.usecase.provider.EnableProviderUseCase
import com.bangersoul.aivance.core.domain.usecase.provider.GetAvailableModelsUseCase
import com.bangersoul.aivance.core.domain.usecase.provider.GetProviderHealthUseCase
import com.bangersoul.aivance.core.domain.usecase.provider.SelectProviderUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.AnalyseResumeUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.CalculateATSScoreUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.ExportResumeUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.GenerateResumeSummaryUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.ImproveResumeUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.ImportResumeUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.ParseResumeUseCase
import com.bangersoul.aivance.core.domain.usecase.settings.ExportSettingsUseCase
import com.bangersoul.aivance.core.domain.usecase.settings.LoadSettingsUseCase
import com.bangersoul.aivance.core.domain.usecase.settings.ResetSettingsUseCase
import com.bangersoul.aivance.core.domain.usecase.settings.SaveSettingsUseCase
import com.bangersoul.aivance.core.domain.usecase.user.CreateProfileUseCase
import com.bangersoul.aivance.core.domain.usecase.user.DeleteProfileUseCase
import com.bangersoul.aivance.core.domain.usecase.user.LoadProfileUseCase
import com.bangersoul.aivance.core.domain.usecase.user.UpdateProfileUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing all domain use cases.
 *
 * Each use case is provided as a @Singleton to ensure consistent state
 * across the application.
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    // ===== Resume Use Cases =====

    @Provides
    @Singleton
    fun provideImportResumeUseCase(
        resumeRepository: ResumeRepository
    ): ImportResumeUseCase = ImportResumeUseCase(resumeRepository)

    @Provides
    @Singleton
    fun provideParseResumeUseCase(
        resumeRepository: ResumeRepository
    ): ParseResumeUseCase = ParseResumeUseCase(resumeRepository)

    @Provides
    @Singleton
    fun provideAnalyseResumeUseCase(
        resumeRepository: ResumeRepository
    ): AnalyseResumeUseCase = AnalyseResumeUseCase(resumeRepository)

    @Provides
    @Singleton
    fun provideCalculateATSScoreUseCase(
        resumeRepository: ResumeRepository
    ): CalculateATSScoreUseCase = CalculateATSScoreUseCase(resumeRepository)

    @Provides
    @Singleton
    fun provideImproveResumeUseCase(
        resumeRepository: ResumeRepository
    ): ImproveResumeUseCase = ImproveResumeUseCase(resumeRepository)

    @Provides
    @Singleton
    fun provideGenerateResumeSummaryUseCase(
        resumeRepository: ResumeRepository
    ): GenerateResumeSummaryUseCase = GenerateResumeSummaryUseCase(resumeRepository)

    @Provides
    @Singleton
    fun provideExportResumeUseCase(
        resumeRepository: ResumeRepository
    ): ExportResumeUseCase = ExportResumeUseCase(resumeRepository)

    // ===== Cover Letter Use Cases =====

    @Provides
    @Singleton
    fun provideGenerateCoverLetterUseCase(
        coverLetterRepository: CoverLetterRepository
    ): GenerateCoverLetterUseCase = GenerateCoverLetterUseCase(coverLetterRepository)

    @Provides
    @Singleton
    fun provideImproveCoverLetterUseCase(
        coverLetterRepository: CoverLetterRepository
    ): ImproveCoverLetterUseCase = ImproveCoverLetterUseCase(coverLetterRepository)

    @Provides
    @Singleton
    fun provideExportCoverLetterUseCase(
        coverLetterRepository: CoverLetterRepository
    ): ExportCoverLetterUseCase = ExportCoverLetterUseCase(coverLetterRepository)

    // ===== Job Search Use Cases =====

    @Provides
    @Singleton
    fun provideSearchJobsUseCase(
        jobRepository: JobRepository
    ): SearchJobsUseCase = SearchJobsUseCase(jobRepository)

    @Provides
    @Singleton
    fun provideSearchRemoteJobsUseCase(
        jobRepository: JobRepository
    ): SearchRemoteJobsUseCase = SearchRemoteJobsUseCase(jobRepository)

    @Provides
    @Singleton
    fun provideGetJobDetailsUseCase(
        jobRepository: JobRepository
    ): GetJobDetailsUseCase = GetJobDetailsUseCase(jobRepository)

    @Provides
    @Singleton
    fun provideSaveJobUseCase(
        jobTrackerRepository: JobTrackerRepository
    ): SaveJobUseCase = SaveJobUseCase(jobTrackerRepository)

    @Provides
    @Singleton
    fun provideBookmarkJobUseCase(
        jobTrackerRepository: JobTrackerRepository
    ): BookmarkJobUseCase = BookmarkJobUseCase(jobTrackerRepository)

    @Provides
    @Singleton
    fun provideApplyToJobUseCase(
        jobTrackerRepository: JobTrackerRepository
    ): ApplyToJobUseCase = ApplyToJobUseCase(jobTrackerRepository)

    @Provides
    @Singleton
    fun provideRemoveSavedJobUseCase(
        jobTrackerRepository: JobTrackerRepository
    ): RemoveSavedJobUseCase = RemoveSavedJobUseCase(jobTrackerRepository)

    @Provides
    @Singleton
    fun provideSearchSavedJobsUseCase(
        jobTrackerRepository: JobTrackerRepository
    ): SearchSavedJobsUseCase = SearchSavedJobsUseCase(jobTrackerRepository)

    // ===== AI Assistant Use Cases =====

    @Provides
    @Singleton
    fun provideStartConversationUseCase(
        aiRepository: AiRepository
    ): StartConversationUseCase = StartConversationUseCase(aiRepository)

    @Provides
    @Singleton
    fun provideSendMessageUseCase(
        aiRepository: AiRepository
    ): SendMessageUseCase = SendMessageUseCase(aiRepository)

    @Provides
    @Singleton
    fun provideStreamResponseUseCase(
        aiRepository: AiRepository,
        providerManager: com.bangersoul.aivance.sdk.infrastructure.ProviderManager
    ): StreamResponseUseCase = StreamResponseUseCase(aiRepository, providerManager)

    @Provides
    @Singleton
    fun provideRegenerateResponseUseCase(
        aiRepository: AiRepository
    ): RegenerateResponseUseCase = RegenerateResponseUseCase(aiRepository)

    @Provides
    @Singleton
    fun provideSummariseConversationUseCase(
        aiRepository: AiRepository
    ): SummariseConversationUseCase = SummariseConversationUseCase(aiRepository)

    @Provides
    @Singleton
    fun provideClearConversationUseCase(
        aiRepository: AiRepository
    ): ClearConversationUseCase = ClearConversationUseCase(aiRepository)

    // ===== Interview Use Cases =====

    @Provides
    @Singleton
    fun provideStartInterviewSessionUseCase(
        interviewRepository: InterviewRepository
    ): StartInterviewSessionUseCase = StartInterviewSessionUseCase(interviewRepository)

    @Provides
    @Singleton
    fun provideGenerateInterviewQuestionsUseCase(
        aiRepository: AiRepository
    ): GenerateInterviewQuestionsUseCase = GenerateInterviewQuestionsUseCase(aiRepository)

    @Provides
    @Singleton
    fun provideEvaluateAnswersUseCase(
        interviewRepository: InterviewRepository,
        aiRepository: AiRepository
    ): EvaluateAnswersUseCase = EvaluateAnswersUseCase(interviewRepository, aiRepository)

    @Provides
    @Singleton
    fun provideGenerateFeedbackUseCase(
        interviewRepository: InterviewRepository
    ): GenerateFeedbackUseCase = GenerateFeedbackUseCase(interviewRepository)

    @Provides
    @Singleton
    fun provideEndInterviewUseCase(
        interviewRepository: InterviewRepository
    ): EndInterviewUseCase = EndInterviewUseCase(interviewRepository)

    // ===== Career Use Cases =====

    @Provides
    @Singleton
    fun provideGenerateCareerRoadmapUseCase(
        aiRepository: AiRepository
    ): GenerateCareerRoadmapUseCase = GenerateCareerRoadmapUseCase(aiRepository)

    @Provides
    @Singleton
    fun provideRecommendSkillsUseCase(
        aiRepository: AiRepository
    ): RecommendSkillsUseCase = RecommendSkillsUseCase(aiRepository)

    @Provides
    @Singleton
    fun provideSuggestLearningPathUseCase(
        aiRepository: AiRepository
    ): SuggestLearningPathUseCase = SuggestLearningPathUseCase(aiRepository)

    // ===== User Use Cases =====

    @Provides
    @Singleton
    fun provideCreateProfileUseCase(
        userRepository: UserRepository
    ): CreateProfileUseCase = CreateProfileUseCase(userRepository)

    @Provides
    @Singleton
    fun provideUpdateProfileUseCase(
        userRepository: UserRepository
    ): UpdateProfileUseCase = UpdateProfileUseCase(userRepository)

    @Provides
    @Singleton
    fun provideLoadProfileUseCase(
        userRepository: UserRepository
    ): LoadProfileUseCase = LoadProfileUseCase(userRepository)

    @Provides
    @Singleton
    fun provideDeleteProfileUseCase(
        userRepository: UserRepository
    ): DeleteProfileUseCase = DeleteProfileUseCase(userRepository)

    // ===== Provider Use Cases =====

    @Provides
    @Singleton
    fun provideEnableProviderUseCase(
        settingsRepository: SettingsRepository
    ): EnableProviderUseCase = EnableProviderUseCase(settingsRepository)

    @Provides
    @Singleton
    fun provideDisableProviderUseCase(
        settingsRepository: SettingsRepository
    ): DisableProviderUseCase = DisableProviderUseCase(settingsRepository)

    @Provides
    @Singleton
    fun provideSelectProviderUseCase(
        settingsRepository: SettingsRepository
    ): SelectProviderUseCase = SelectProviderUseCase(settingsRepository)

    @Provides
    @Singleton
    fun provideGetAvailableModelsUseCase(
        providerManager: com.bangersoul.aivance.sdk.infrastructure.ProviderManager
    ): GetAvailableModelsUseCase = GetAvailableModelsUseCase(providerManager)

    @Provides
    @Singleton
    fun provideGetProviderHealthUseCase(
        providerManager: com.bangersoul.aivance.sdk.infrastructure.ProviderManager
    ): GetProviderHealthUseCase = GetProviderHealthUseCase(providerManager)

    // ===== Settings Use Cases =====

    @Provides
    @Singleton
    fun provideLoadSettingsUseCase(
        settingsRepository: SettingsRepository
    ): LoadSettingsUseCase = LoadSettingsUseCase(settingsRepository)

    @Provides
    @Singleton
    fun provideSaveSettingsUseCase(
        settingsRepository: SettingsRepository
    ): SaveSettingsUseCase = SaveSettingsUseCase(settingsRepository)

    @Provides
    @Singleton
    fun provideResetSettingsUseCase(
        settingsRepository: SettingsRepository
    ): ResetSettingsUseCase = ResetSettingsUseCase(settingsRepository)

    @Provides
    @Singleton
    fun provideExportSettingsUseCase(
        settingsRepository: SettingsRepository
    ): ExportSettingsUseCase = ExportSettingsUseCase(settingsRepository)

    // ===== Analytics Use Cases =====

    @Provides
    @Singleton
    fun provideTrackEventUseCase(
        analyticsRepository: AnalyticsRepository
    ): TrackEventUseCase = TrackEventUseCase(analyticsRepository)

    @Provides
    @Singleton
    fun provideGenerateUsageReportUseCase(
        analyticsRepository: AnalyticsRepository
    ): GenerateUsageReportUseCase = GenerateUsageReportUseCase(analyticsRepository)

    @Provides
    @Singleton
    fun provideExportAnalyticsUseCase(
        analyticsRepository: AnalyticsRepository
    ): ExportAnalyticsUseCase = ExportAnalyticsUseCase(analyticsRepository)
}
