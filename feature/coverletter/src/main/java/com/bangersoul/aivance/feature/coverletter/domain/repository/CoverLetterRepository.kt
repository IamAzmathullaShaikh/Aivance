package com.bangersoul.aivance.feature.coverletter.domain.repository

import com.bangersoul.aivance.feature.coverletter.domain.model.CoverLetter
import com.bangersoul.aivance.feature.coverletter.domain.model.LetterTone
import kotlinx.coroutines.flow.Flow

interface CoverLetterRepository {
    fun generateCoverLetter(
        resumeText: String,
        jobDescription: String,
        tone: LetterTone
    ): Flow<String>

    fun saveCoverLetter(coverLetter: CoverLetter): Flow<Unit>

    fun getCoverLetters(): Flow<List<CoverLetter>>
}
