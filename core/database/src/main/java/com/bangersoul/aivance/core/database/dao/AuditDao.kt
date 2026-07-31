package com.bangersoul.aivance.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.bangersoul.aivance.core.database.model.AuditLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getLogs(): Flow<List<AuditLogEntity>>

    @Insert
    suspend fun insertLog(log: AuditLogEntity): Long

    @Query("DELETE FROM audit_logs WHERE timestamp < :beforeTimestamp")
    suspend fun clearOldLogs(beforeTimestamp: Long)
}
