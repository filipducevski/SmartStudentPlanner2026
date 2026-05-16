package com.smartstudent.planner.data.repository

import com.smartstudent.planner.data.local.dao.ExamDao
import com.smartstudent.planner.data.local.dao.SubjectDao
import com.smartstudent.planner.data.local.dao.TaskDao
import com.smartstudent.planner.data.local.entities.ExamEntity
import com.smartstudent.planner.data.local.entities.SubjectEntity
import com.smartstudent.planner.data.local.entities.TaskEntity
import com.smartstudent.planner.data.remote.FirestoreRepository
import com.smartstudent.planner.service.NotificationScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val subjectDao: SubjectDao,
    private val examDao: ExamDao,
    private val firestoreRepository: FirestoreRepository,
    private val notificationScheduler: NotificationScheduler
) {
    // ─── Tasks ───────────────────────────────────────────────────────────────
    fun getAllTasks(userId: String) = taskDao.getAllTasks(userId)
    fun getPendingTasks(userId: String) = taskDao.getPendingTasks(userId)
    fun getCompletedTasks(userId: String) = taskDao.getCompletedTasks(userId)
    fun getOverdueTasks(userId: String) = taskDao.getOverdueTasks(userId)

    fun getTodayTasks(userId: String): Flow<List<TaskEntity>> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = cal.timeInMillis
        return taskDao.getTasksForDay(userId, startOfDay, endOfDay)
    }

    fun getWeekTasks(userId: String): Flow<List<TaskEntity>> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        val startOfWeek = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 7)
        val endOfWeek = cal.timeInMillis
        return taskDao.getTasksForWeek(userId, startOfWeek, endOfWeek)
    }

    fun getTasksForSubject(subjectId: String) = taskDao.getTasksForSubject(subjectId)
    fun getPendingTaskCount(userId: String) = taskDao.getPendingTaskCount(userId)
    fun getCompletedTaskCount(userId: String) = taskDao.getCompletedTaskCount(userId)
    fun getOverdueTaskCount(userId: String) = taskDao.getOverdueTaskCount(userId)
    fun searchTasks(userId: String, query: String) = taskDao.searchTasks(userId, query)

    suspend fun saveTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        taskDao.insertTask(task)
        task.dueDate?.let {
            task.reminderOffsetMinutes?.let {
                notificationScheduler.scheduleTaskReminder(task)
            }
        }
        syncTask(task.userId, task)
    }

    suspend fun updateTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        taskDao.updateTask(task.copy(updatedAt = System.currentTimeMillis(), isSynced = false))
        syncTask(task.userId, task)
    }

    suspend fun toggleTaskCompletion(taskId: String, completed: Boolean) = withContext(Dispatchers.IO) {
        val completedAt = if (completed) System.currentTimeMillis() else null
        taskDao.updateTaskCompletion(taskId, completed, completedAt)
        val task = taskDao.getTaskById(taskId)
        task?.let {
            if (completed) notificationScheduler.cancelTaskReminder(taskId)
            syncTask(it.userId, it.copy(isCompleted = completed, completedAt = completedAt))
        }
    }

    suspend fun deleteTask(taskId: String) = withContext(Dispatchers.IO) {
        val task = taskDao.getTaskById(taskId)
        taskDao.deleteTaskById(taskId)
        notificationScheduler.cancelTaskReminder(taskId)
        task?.let {
            firestoreRepository.deleteRemoteTask(it.userId, taskId)
        }
    }

    suspend fun syncAllPendingTasks(userId: String) = withContext(Dispatchers.IO) {
        val unsyncedTasks = taskDao.getUnsyncedTasks(userId)
        Timber.d("Syncing ${unsyncedTasks.size} tasks")
        unsyncedTasks.forEach { task ->
            firestoreRepository.syncTask(userId, task).onSuccess {
                taskDao.markTaskSynced(task.id)
            }
        }
    }

    private suspend fun syncTask(userId: String, task: TaskEntity) {
        firestoreRepository.syncTask(userId, task).onSuccess {
            taskDao.markTaskSynced(task.id)
        }.onFailure {
            Timber.w("Task sync failed (will retry later): ${it.message}")
        }
    }

    // ─── Subjects ────────────────────────────────────────────────────────────
    fun getAllSubjects(userId: String) = subjectDao.getAllSubjects(userId)
    fun getActiveSubjectCount(userId: String) = subjectDao.getActiveSubjectCount(userId)
    suspend fun getSubjectById(subjectId: String) = subjectDao.getSubjectById(subjectId)

    suspend fun saveSubject(subject: SubjectEntity) = withContext(Dispatchers.IO) {
        subjectDao.insertSubject(subject)
        firestoreRepository.syncSubject(subject.userId, subject).onSuccess {
            subjectDao.markSubjectSynced(subject.id)
        }
    }

    suspend fun updateSubject(subject: SubjectEntity) = withContext(Dispatchers.IO) {
        subjectDao.updateSubject(subject.copy(updatedAt = System.currentTimeMillis(), isSynced = false))
        firestoreRepository.syncSubject(subject.userId, subject).onSuccess {
            subjectDao.markSubjectSynced(subject.id)
        }
    }

    suspend fun deleteSubject(subjectId: String, userId: String) = withContext(Dispatchers.IO) {
        subjectDao.softDeleteSubject(subjectId)
        firestoreRepository.deleteRemoteSubject(userId, subjectId)
    }

    // ─── Exams ───────────────────────────────────────────────────────────────
    fun getAllExams(userId: String) = examDao.getAllExams(userId)
    fun getUpcomingExams(userId: String) = examDao.getUpcomingExams(userId)
    fun getPastExams(userId: String) = examDao.getPastExams(userId)
    fun getExamsForSubject(subjectId: String) = examDao.getExamsForSubject(subjectId)
    fun getUpcomingExamCount(userId: String) = examDao.getUpcomingExamCount(userId)

    suspend fun saveExam(exam: ExamEntity) = withContext(Dispatchers.IO) {
        examDao.insertExam(exam)
        exam.reminderOffsetMinutes?.let { notificationScheduler.scheduleExamReminder(exam) }
        firestoreRepository.syncExam(exam.userId, exam).onSuccess {
            examDao.markExamSynced(exam.id)
        }
    }

    suspend fun updateExam(exam: ExamEntity) = withContext(Dispatchers.IO) {
        examDao.updateExam(exam.copy(updatedAt = System.currentTimeMillis(), isSynced = false))
        firestoreRepository.syncExam(exam.userId, exam).onSuccess {
            examDao.markExamSynced(exam.id)
        }
    }

    suspend fun deleteExam(examId: String, userId: String) = withContext(Dispatchers.IO) {
        examDao.deleteExamById(examId)
        notificationScheduler.cancelExamReminder(examId)
        firestoreRepository.deleteRemoteExam(userId, examId)
    }

    // ─── Full sync from Firestore ─────────────────────────────────────────────
    suspend fun fullSyncFromFirestore(userId: String) = withContext(Dispatchers.IO) {
        Timber.d("Starting full Firestore sync for user: $userId")
        firestoreRepository.fetchRemoteTasks(userId).onSuccess { remoteTasks ->
            val entities = remoteTasks.mapNotNull { it.toTaskEntity(userId) }
            taskDao.insertTasks(entities)
        }
        firestoreRepository.fetchRemoteSubjects(userId).onSuccess { remoteSubjects ->
            val entities = remoteSubjects.mapNotNull { it.toSubjectEntity(userId) }
            subjectDao.insertSubjects(entities)
        }
        firestoreRepository.fetchRemoteExams(userId).onSuccess { remoteExams ->
            val entities = remoteExams.mapNotNull { it.toExamEntity(userId) }
            examDao.insertExams(entities)
        }
        Timber.d("Full sync completed")
    }

    // ─── Mapping from Firestore documents ────────────────────────────────────

    // FIXED: Changed from expression body (= try {) to block body ({ try { } })
    // to allow `return null` inside the function body
    private fun Map<String, Any?>.toTaskEntity(userId: String): TaskEntity? {
        return try {
            TaskEntity(
                id = get("id") as? String ?: return null,
                userId = userId,
                title = get("title") as? String ?: "",
                description = get("description") as? String ?: "",
                subjectId = get("subjectId") as? String,
                subjectName = get("subjectName") as? String ?: "",
                subjectColor = get("subjectColor") as? String ?: "#2563EB",
                dueDate = (get("dueDate") as? Number)?.toLong(),
                dueTime = get("dueTime") as? String,
                priority = (get("priority") as? Number)?.toInt() ?: 1,
                taskType = get("taskType") as? String ?: "HOMEWORK",
                isCompleted = get("isCompleted") as? Boolean ?: false,
                completedAt = (get("completedAt") as? Number)?.toLong(),
                notes = get("notes") as? String ?: "",
                createdAt = (get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (get("updatedAt") as? Number)?.toLong() ?: System.currentTimeMillis(),
                isSynced = true
            )
        } catch (e: Exception) {
            Timber.e(e, "Error mapping task from Firestore")
            null
        }
    }

    private fun Map<String, Any?>.toSubjectEntity(userId: String): SubjectEntity? {
        return try {
            SubjectEntity(
                id = get("id") as? String ?: return null,
                userId = userId,
                name = get("name") as? String ?: "",
                code = get("code") as? String ?: "",
                professor = get("professor") as? String ?: "",
                credits = (get("credits") as? Number)?.toInt() ?: 0,
                color = get("color") as? String ?: "#2563EB",
                roomLocation = get("roomLocation") as? String ?: "",
                scheduleJson = get("scheduleJson") as? String ?: "[]",
                createdAt = (get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (get("updatedAt") as? Number)?.toLong() ?: System.currentTimeMillis(),
                isSynced = true,
                isActive = get("isActive") as? Boolean ?: true
            )
        } catch (e: Exception) {
            Timber.e(e, "Error mapping subject from Firestore")
            null
        }
    }

    private fun Map<String, Any?>.toExamEntity(userId: String): ExamEntity? {
        return try {
            ExamEntity(
                id = get("id") as? String ?: return null,
                userId = userId,
                title = get("title") as? String ?: "",
                subjectId = get("subjectId") as? String,
                subjectName = get("subjectName") as? String ?: "",
                subjectColor = get("subjectColor") as? String ?: "#2563EB",
                examDate = (get("examDate") as? Number)?.toLong() ?: return null,
                examTime = get("examTime") as? String ?: "",
                location = get("location") as? String ?: "",
                durationMinutes = (get("durationMinutes") as? Number)?.toInt() ?: 90,
                examType = get("examType") as? String ?: "WRITTEN",
                notes = get("notes") as? String ?: "",
                createdAt = (get("createdAt") as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (get("updatedAt") as? Number)?.toLong() ?: System.currentTimeMillis(),
                isSynced = true
            )
        } catch (e: Exception) {
            Timber.e(e, "Error mapping exam from Firestore")
            null
        }
    }
}
