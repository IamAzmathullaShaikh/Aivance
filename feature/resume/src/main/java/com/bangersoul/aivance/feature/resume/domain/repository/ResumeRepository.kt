package com.bangersoul.aivance.feature.resume.domain.repository

import com.bangersoul.aivance.feature.resume.domain.model.ResumeAnalysis
import kotlinx.coroutines.flow.Flow

interface ResumeRepository {
    fun analyzeResume(resumeText: String, jobDescription: String): Flow<ResumeAnalysis>
    suspend fun saveAnalysis(analysis: ResumeAnalysis, resumeId: Long, jobDescription: String)
}
