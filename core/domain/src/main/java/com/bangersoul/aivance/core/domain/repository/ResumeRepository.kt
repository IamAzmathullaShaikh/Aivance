package com.bangersoul.aivance.core.domain.repository

import com.bangersoul.aivance.core.common.model.AtsResult
import com.bangersoul.aivance.core.common.model.Resume
import com.bangersoul.aivance.core.common.model.ResumeAnalysis
import com.bangersoul.aivance.core.common.model.ResumeSection
import com.bangersoul.aivance.core.common.result.CoreResult
import kotlinx.coroutines.flow.Flow

interface ResumeRepository {
    fun getResumes(): Flow<CoreResult<List<Resume>>>
    fun getResumeById(id: Long): Flow<CoreResult<Resume>>
    suspend fun insertResume(resume: Resume): CoreResult<Long>
    suspend fun updateResume(resume: Resume): CoreResult<Unit>
    suspend fun deleteResume(id: Long): CoreResult<Unit>
    
    suspend fun updateSections(resumeId: Long, sections: List<ResumeSection>): CoreResult<Unit>
    
    suspend fun analyzeResume(resumeId: Long, jobDescription: String): CoreResult<ResumeAnalysis>
    fun getAtsResults(resumeId: Long): Flow<CoreResult<List<AtsResult>>>
}
