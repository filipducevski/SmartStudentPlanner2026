package com.smartstudent.planner.data.local.dao

import androidx.room.*
import com.smartstudent.planner.data.local.entities.ExamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {

    @Query("SELECT * FROM exams WHERE user_id = :userId ORDER BY exam_date ASC")
    fun getAllExams(userId: String): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE user_id = :userId AND exam_date >= :now ORDER BY exam_date ASC")
    fun getUpcomingExams(userId: String, now: Long = System.currentTimeMillis()): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE user_id = :userId AND exam_date < :now ORDER BY exam_date DESC")
    fun getPastExams(userId: String, now: Long = System.currentTimeMillis()): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE subject_id = :subjectId ORDER BY exam_date ASC")
    fun getExamsForSubject(subjectId: String): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE id = :examId")
    suspend fun getExamById(examId: String): ExamEntity?

    @Query("SELECT * FROM exams WHERE is_synced = 0 AND user_id = :userId")
    suspend fun getUnsyncedExams(userId: String): List<ExamEntity>

    @Query("""
        SELECT * FROM exams
        WHERE user_id = :userId
        AND exam_date >= :startMs
        AND exam_date <= :endMs
        ORDER BY exam_date ASC
    """)
    fun getExamsInRange(userId: String, startMs: Long, endMs: Long): Flow<List<ExamEntity>>

    @Query("SELECT COUNT(*) FROM exams WHERE user_id = :userId AND exam_date >= :now")
    fun getUpcomingExamCount(userId: String, now: Long = System.currentTimeMillis()): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExams(exams: List<ExamEntity>)

    @Update
    suspend fun updateExam(exam: ExamEntity)

    @Query("UPDATE exams SET is_synced = 1 WHERE id = :examId")
    suspend fun markExamSynced(examId: String)

    @Delete
    suspend fun deleteExam(exam: ExamEntity)

    @Query("DELETE FROM exams WHERE id = :examId")
    suspend fun deleteExamById(examId: String)

    @Query("DELETE FROM exams WHERE user_id = :userId")
    suspend fun deleteAllExamsForUser(userId: String)
}
