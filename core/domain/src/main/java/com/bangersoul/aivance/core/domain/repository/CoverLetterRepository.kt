package com.bangersoul.aivance.core.domain.repository

import com.bangersoul.aivance.core.common.enums.LetterTone
import com.bangersoul.aivance.core.common.model.CoverLetter
import com.bangersoul.aivance.core.common.result.CoreResult
import kotlinx.coroutines.flow.Flow

interface CoverLetterRepository {
    suspend fun generateCoverLetter(resumeId: Long, jobDescription: String, tone: LetterTone): CoreResult<CoverLetter>
    fun getCoverLetters(): Flow<CoreResult<List<CoverLetter>>>
    fun getCoverLetterById(id: Long): Flow<CoreResult<CoverLetter>>
    suspend fun saveCoverLetter(coverLetter: CoverLetter): CoreResult<Long>
    suspend fun deleteCoverLetter(id: Long): CoreResult<Unit>
}
