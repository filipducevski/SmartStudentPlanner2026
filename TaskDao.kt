package com.smartstudent.planner.data.local.dao

import androidx.room.*
import com.smartstudent.planner.data.local.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE user_id = :userId ORDER BY due_date ASC, priority DESC")
    fun getAllTasks(userId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE user_id = :userId AND is_completed = 0 ORDER BY due_date ASC, priority DESC")
    fun getPendingTasks(userId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE user_id = :userId AND is_completed = 1 ORDER BY completed_at DESC")
    fun getCompletedTasks(userId: String): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks 
        WHERE user_id = :userId 
        AND due_date >= :startOfDay 
        AND due_date < :endOfDay
        ORDER BY due_date ASC
    """)
    fun getTasksForDay(userId: String, startOfDay: Long, endOfDay: Long): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks 
        WHERE user_id = :userId 
        AND due_date >= :startOfWeek 
        AND due_date < :endOfWeek
        ORDER BY due_date ASC
    """)
    fun getTasksForWeek(userId: String, startOfWeek: Long, endOfWeek: Long): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks
        WHERE user_id = :userId
        AND is_completed = 0
        AND due_date < :now
        ORDER BY due_date ASC
    """)
    fun getOverdueTasks(userId: String, now: Long = System.currentTimeMillis()): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE subject_id = :subjectId ORDER BY due_date ASC")
    fun getTasksForSubject(subjectId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?

    @Query("SELECT * FROM tasks WHERE is_synced = 0 AND user_id = :userId")
    suspend fun getUnsyncedTasks(userId: String): List<TaskEntity>

    @Query("SELECT COUNT(*) FROM tasks WHERE user_id = :userId AND is_completed = 0")
    fun getPendingTaskCount(userId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE user_id = :userId AND is_completed = 1")
    fun getCompletedTaskCount(userId: String): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM tasks
        WHERE user_id = :userId
        AND is_completed = 0
        AND due_date < :now
    """)
    fun getOverdueTaskCount(userId: String, now: Long = System.currentTimeMillis()): Flow<Int>

    @Query("""
        SELECT * FROM tasks
        WHERE user_id = :userId
        AND (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        ORDER BY due_date ASC
    """)
    fun searchTasks(userId: String, query: String): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET is_completed = :completed, completed_at = :completedAt, updated_at = :updatedAt, is_synced = 0 WHERE id = :taskId")
    suspend fun updateTaskCompletion(taskId: String, completed: Boolean, completedAt: Long?, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE tasks SET is_synced = 1 WHERE id = :taskId")
    suspend fun markTaskSynced(taskId: String)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)

    @Query("DELETE FROM tasks WHERE user_id = :userId")
    suspend fun deleteAllTasksForUser(userId: String)
}
