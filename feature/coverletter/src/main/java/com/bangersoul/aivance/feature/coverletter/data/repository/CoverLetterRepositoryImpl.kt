package com.bangersoul.aivance.feature.coverletter.data.repository

import com.bangersoul.aivance.core.database.dao.CoverLetterDao
import com.bangersoul.aivance.core.database.model.CoverLetterEntity
import com.bangersoul.aivance.core.network.AiService
import com.bangersoul.aivance.feature.coverletter.domain.model.CoverLetter
import com.bangersoul.aivance.feature.coverletter.domain.model.LetterTone
import com.bangersoul.aivance.feature.coverletter.domain.repository.CoverLetterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CoverLetterRepositoryImpl @Inject constructor(
    private val aiService: AiService,
    private val coverLetterDao: CoverLetterDao
) : CoverLetterRepository {

    override fun generateCoverLetter(
        resumeText: String,
        jobDescription: String,
        tone: LetterTone
    ): Flow<String> = flow {
        val prompt = """
            Act as an expert career coach and professional writer. 
            Generate a compelling cover letter for the following job application.
            
            RESUME CONTENT:
            $resumeText
            
            JOB DESCRIPTION:
            $jobDescription
            
            TONE REQUIREMENT: 
            The cover letter MUST be written in a ${tone.name.lowercase()} tone.
            
            INSTRUCTIONS:
            1. Highlight relevant skills and experiences from the resume that match the job description.
            2. Show enthusiasm for the role and the company.
            3. Ensure the tone is consistently ${tone.name.lowercase()} throughout the letter.
            4. Keep it concise and professionally structured.
            
            Please provide only the content of the cover letter.
        """.trimIndent()

        val result = aiService.analyzeText(prompt)
        emit(result.getOrThrow())
    }

    override fun saveCoverLetter(coverLetter: CoverLetter): Flow<Unit> = flow {
        coverLetterDao.insertCoverLetter(coverLetter.toEntity())
        emit(Unit)
    }

    override fun getCoverLetters(): Flow<List<CoverLetter>> {
        return coverLetterDao.getCoverLetters().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}

private fun CoverLetter.toEntity() = CoverLetterEntity(
    id = id,
    company = company,
    role = role,
    content = content,
    dateCreated = dateCreated,
    tone = tone.name
)

private fun CoverLetterEntity.toDomain() = CoverLetter(
    id = id,
    company = company,
    role = role,
    content = content,
    dateCreated = dateCreated,
    tone = LetterTone.valueOf(tone)
)
