package com.smartstudent.planner.data.local.dao

import androidx.room.*
import com.smartstudent.planner.data.local.entities.SubjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {

    @Query("SELECT * FROM subjects WHERE user_id = :userId AND is_active = 1 ORDER BY name ASC")
    fun getAllSubjects(userId: String): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :subjectId")
    suspend fun getSubjectById(subjectId: String): SubjectEntity?

    @Query("SELECT * FROM subjects WHERE is_synced = 0 AND user_id = :userId")
    suspend fun getUnsyncedSubjects(userId: String): List<SubjectEntity>

    @Query("SELECT COUNT(*) FROM subjects WHERE user_id = :userId AND is_active = 1")
    fun getActiveSubjectCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Query("UPDATE subjects SET is_synced = 1 WHERE id = :subjectId")
    suspend fun markSubjectSynced(subjectId: String)

    @Query("UPDATE subjects SET is_active = 0, updated_at = :updatedAt WHERE id = :subjectId")
    suspend fun softDeleteSubject(subjectId: String, updatedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)

    @Query("DELETE FROM subjects WHERE user_id = :userId")
    suspend fun deleteAllSubjectsForUser(userId: String)
}
