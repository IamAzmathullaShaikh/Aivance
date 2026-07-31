package com.bangersoul.aivance.core.domain.repository

import com.bangersoul.aivance.core.common.model.AtsReport
import com.bangersoul.aivance.core.common.model.JobDescription
import com.bangersoul.aivance.core.common.result.CoreResult
import kotlinx.coroutines.flow.Flow

interface AtsRepository {
    fun getReportsForVersion(versionId: Long): Flow<CoreResult<List<AtsReport>>>
    suspend fun getReportById(id: Long): AtsReport?
    suspend fun saveReport(report: AtsReport): CoreResult<Long>
    suspend fun deleteReport(id: Long): CoreResult<Unit>

    suspend fun saveJobDescription(jd: JobDescription): CoreResult<Long>
    suspend fun getJobDescription(id: Long): JobDescription?

    suspend fun performAtsAnalysis(resumeId: Long, versionId: Long, jobDescriptionId: Long): CoreResult<AtsReport>
}
