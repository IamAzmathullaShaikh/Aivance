package com.bangersoul.aivance.core.data.source

import com.bangersoul.aivance.core.common.model.CoverLetter
import com.bangersoul.aivance.core.data.mapper.toDomain
import com.bangersoul.aivance.core.data.mapper.toEntity
import com.bangersoul.aivance.core.database.dao.CoverLetterDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface CoverLetterLocalDataSource {
    fun getCoverLetters(): Flow<List<CoverLetter>>
    suspend fun getCoverLetterById(id: Long): CoverLetter?
    suspend fun saveCoverLetter(coverLetter: CoverLetter): Long
    suspend fun deleteCoverLetter(coverLetter: CoverLetter)
}

class CoverLetterLocalDataSourceImpl @Inject constructor(
    private val coverLetterDao: CoverLetterDao
) : CoverLetterLocalDataSource {

    override fun getCoverLetters(): Flow<List<CoverLetter>> {
        return coverLetterDao.getCoverLetters().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCoverLetterById(id: Long): CoverLetter? {
        return coverLetterDao.getCoverLetterById(id)?.toDomain()
    }

    override suspend fun saveCoverLetter(coverLetter: CoverLetter): Long {
        return coverLetterDao.insertCoverLetter(coverLetter.toEntity())
    }

    override suspend fun deleteCoverLetter(coverLetter: CoverLetter) {
        coverLetterDao.deleteCoverLetter(coverLetter.toEntity())
    }
}
