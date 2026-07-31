package com.bangersoul.aivance.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.bangersoul.aivance.core.database.model.CommunicationHistoryEntity
import com.bangersoul.aivance.core.database.model.OutreachDraftEntity
import com.bangersoul.aivance.core.database.model.RecruiterContactEntity
import com.bangersoul.aivance.core.database.model.RecruiterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecruiterDao {

    @Query("SELECT * FROM recruiters WHERE companyId = :companyId")
    fun getRecruitersForCompany(companyId: Long): Flow<List<RecruiterEntity>>

    @Query("SELECT * FROM recruiters WHERE id = :id")
    suspend fun getRecruiterById(id: String): RecruiterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecruiter(recruiter: RecruiterEntity)

    @Delete
    suspend fun deleteRecruiter(recruiter: RecruiterEntity)

    // Contacts
    @Query("SELECT * FROM recruiter_contacts WHERE recruiterId = :recruiterId")
    fun getContactsForRecruiter(recruiterId: String): Flow<List<RecruiterContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: RecruiterContactEntity)

    // Outreach Drafts
    @Query("SELECT * FROM outreach_drafts WHERE recruiterId = :recruiterId")
    fun getDraftsForRecruiter(recruiterId: String): Flow<List<OutreachDraftEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: OutreachDraftEntity): Long

    // History
    @Query("SELECT * FROM communication_history WHERE recruiterId = :recruiterId ORDER BY sentDate DESC")
    fun getHistoryForRecruiter(recruiterId: String): Flow<List<CommunicationHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: CommunicationHistoryEntity): Long
}
