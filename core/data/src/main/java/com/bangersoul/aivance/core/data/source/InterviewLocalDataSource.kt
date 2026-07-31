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
    suspend fun getSessionById(id: Long): InterviewSession?
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

    override suspend fun getSessionById(id: Long): InterviewSession? {
        return interviewDao.getInterviewSessionWithMessagesById(id)?.toDomain()
    }

    override suspend fun saveSession(session: InterviewSession): Long {
        return interviewDao.insertSession(session.toEntity())
    }

    override suspend fun saveMessage(sessionId: Long, message: com.bangersoul.aivance.core.common.model.InterviewMessage): Long {
        return interviewDao.insertMessage(message.toEntity(sessionId))
    }

    override suspend fun deleteSession(id: Long) {
        val entity = interviewDao.getInterviewSessionWithMessagesById(id)
        if (entity != null) {
            interviewDao.deleteSession(entity.session)
        }
    }
}
