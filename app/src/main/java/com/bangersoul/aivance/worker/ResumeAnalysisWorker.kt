package com.bangersoul.aivance.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.usecase.resume.AnalyseResumeUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.CalculateATSScoreUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * One-time worker that performs resume analysis in the background.
 *
 * Triggered when a user uploads a new resume or updates their existing one.
 * Requires network connectivity (AI provider needs to be reachable).
 */
@HiltWorker
class ResumeAnalysisWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val analyseResumeUseCase: AnalyseResumeUseCase,
    private val calculateATSScoreUseCase: CalculateATSScoreUseCase,
    private val connectivityMonitor: ConnectivityMonitor
) : CoroutineWorker(context, params) {

    companion object {
        const val EXTRA_RESUME_TEXT = "resume_text"
        const val EXTRA_JOB_DESCRIPTION = "job_description"
    }

    override suspend fun doWork(): Result {
        val resumeText = inputData.getString(EXTRA_RESUME_TEXT) ?: ""
        val jobDescription = inputData.getString(EXTRA_JOB_DESCRIPTION) ?: ""

        if (resumeText.isBlank()) {
            Timber.w("ResumeAnalysisWorker — no resume text provided")
            return Result.failure()
        }

        Timber.d("ResumeAnalysisWorker started — resume length: %d chars", resumeText.length)

        if (!connectivityMonitor.isOnline) {
            Timber.d("ResumeAnalysisWorker — offline, deferring")
            return Result.retry()
        }

        return try {
            // 1. Analyse resume
            val analyseResult = analyseResumeUseCase(
                AnalyseResumeUseCase.AnalyseResumeInput(
                    resumeText = resumeText,
                    jobDescription = jobDescription
                )
            )
            when (analyseResult) {
                is Result.Success -> Timber.d("Resume analysis completed")
                is Result.Failure -> Timber.w("Resume analysis failed: %s", analyseResult.error.message)
            }

            // 2. Calculate ATS score
            val atsResult = calculateATSScoreUseCase(resumeText)
            when (atsResult) {
                is Result.Success -> Timber.d("ATS score calculated: %d", atsResult.data)
                is Result.Failure -> Timber.w("ATS score failed: %s", atsResult.error.message)
            }

            Timber.d("ResumeAnalysisWorker completed")
            Result.success()

        } catch (e: Exception) {
            Timber.e(e, "ResumeAnalysisWorker failed")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
