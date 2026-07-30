package com.bangersoul.aivance.core.data.source

import com.bangersoul.aivance.core.common.model.InterviewSession
import com.bangersoul.aivance.core.data.mapper.toDomain
import com.bangersoul.aivance.core.data.mapper.toEntity
import com.bangersoul.aivance.core.database.dao.InterviewDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface InterviewLocalDataSource {
    fun getSessions(): Flow<List<InterviewSession>>
    fun getSessionById(id: Long): Flow<InterviewSession?>
    suspend fun saveSession(session: InterviewSession): Long
    suspend fun saveMessage(sessionId: Long, message: com.bangersoul.aivance.core.common.model.InterviewMessage): Long
    suspend fun deleteSession(id: Long)
}

class InterviewLocalDataSourceImpl @Inject constructor(
    private val interviewDao: InterviewDao
) : InterviewLocalDataSource {

    override fun getSessions(): Flow<List<InterviewSession>> {
        return interviewDao.getInterviewSessions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getSessionById(id: Long): Flow<InterviewSession?> {
        return interviewDao.getInterviewSessionWithMessages(id).map { it?.toDomain() }
    }

    override suspend fun saveSession(session: InterviewSession): Long {
        return interviewDao.insertSession(session.toEntity())
    }

    override suspend fun saveMessage(sessionId: Long, message: com.bangersoul.aivance.core.common.model.InterviewMessage): Long {
        return interviewDao.insertMessage(message.toEntity(sessionId))
    }

    override suspend fun deleteSession(id: Long) {
        interviewDao.deleteSessionAndMessages(id)
    }
}
