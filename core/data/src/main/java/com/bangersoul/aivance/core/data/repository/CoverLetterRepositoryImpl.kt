package com.bangersoul.aivance.core.data.repository

import com.bangersoul.aivance.core.common.enums.LetterTone
import com.bangersoul.aivance.core.common.model.CoverLetter
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.getOrNull
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.data.source.CoverLetterLocalDataSource
import com.bangersoul.aivance.core.domain.repository.CoverLetterRepository
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CoverLetterRepositoryImpl @Inject constructor(
    private val localDataSource: CoverLetterLocalDataSource,
    private val providerManager: ProviderManager
) : CoverLetterRepository {

    override suspend fun generateCoverLetter(resumeId: Long, jobDescription: String, tone: LetterTone): CoreResult<CoverLetter> = runCatchingCore {
        val prompt = "Generate a cover letter for this job description: $jobDescription with tone: $tone"
        val provider = providerManager.getBestProviderFor(ProviderCapability.AI.Chat) as? com.bangersoul.aivance.sdk.api.AIProvider
            ?: throw Exception("No AI provider available")
        val content = provider.generateText(prompt).getOrNull() ?: throw Exception("AI generation failed")
        val letter = CoverLetter(
            company = "Unknown",
            role = "Unknown",
            content = content,
            tone = tone
        )
        val id = localDataSource.saveCoverLetter(letter)
        letter.copy(id = id)
    }

    override fun getCoverLetters(): Flow<CoreResult<List<CoverLetter>>> {
        return localDataSource.getCoverLetters().map { runCatchingCore { it } }
    }

    override fun getCoverLetterById(id: Long): Flow<CoreResult<CoverLetter>> {
        return localDataSource.getCoverLetters().map { letters ->
            runCatchingCore { letters.find { it.id == id } ?: throw Exception("Cover letter not found") }
        }
    }

    override suspend fun saveCoverLetter(coverLetter: CoverLetter): CoreResult<Long> = runCatchingCore {
        localDataSource.saveCoverLetter(coverLetter)
    }

    override suspend fun deleteCoverLetter(id: Long): CoreResult<Unit> = runCatchingCore {
        localDataSource.deleteCoverLetter(id.toInt())
    }
}
