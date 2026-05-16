package com.smartstudent.planner.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.smartstudent.planner.data.local.entities.ExamEntity
import com.smartstudent.planner.data.local.entities.SubjectEntity
import com.smartstudent.planner.data.local.entities.TaskEntity
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    // ─── Collection paths ────────────────────────────────────────────────────
    private fun tasksCollection(userId: String) =
        firestore.collection("users").document(userId).collection("tasks")

    private fun subjectsCollection(userId: String) =
        firestore.collection("users").document(userId).collection("subjects")

    private fun examsCollection(userId: String) =
        firestore.collection("users").document(userId).collection("exams")

    private fun userDocument(userId: String) =
        firestore.collection("users").document(userId)

    // ─── Tasks ───────────────────────────────────────────────────────────────
    suspend fun syncTask(userId: String, task: TaskEntity): Result<Unit> = runCatching {
        val data = task.toFirestoreMap()
        tasksCollection(userId).document(task.id).set(data, SetOptions.merge()).await()
        Timber.d("Task synced: ${task.id}")
    }

    suspend fun fetchRemoteTasks(userId: String): Result<List<Map<String, Any?>>> = runCatching {
        val snapshot = tasksCollection(userId).get().await()
        snapshot.documents.mapNotNull { it.data }
    }

    suspend fun deleteRemoteTask(userId: String, taskId: String): Result<Unit> = runCatching {
        tasksCollection(userId).document(taskId).delete().await()
    }

    // ─── Subjects ────────────────────────────────────────────────────────────
    suspend fun syncSubject(userId: String, subject: SubjectEntity): Result<Unit> = runCatching {
        val data = subject.toFirestoreMap()
        subjectsCollection(userId).document(subject.id).set(data, SetOptions.merge()).await()
        Timber.d("Subject synced: ${subject.id}")
    }

    suspend fun fetchRemoteSubjects(userId: String): Result<List<Map<String, Any?>>> = runCatching {
        val snapshot = subjectsCollection(userId).get().await()
        snapshot.documents.mapNotNull { it.data }
    }

    suspend fun deleteRemoteSubject(userId: String, subjectId: String): Result<Unit> = runCatching {
        subjectsCollection(userId).document(subjectId).delete().await()
    }

    // ─── Exams ───────────────────────────────────────────────────────────────
    suspend fun syncExam(userId: String, exam: ExamEntity): Result<Unit> = runCatching {
        val data = exam.toFirestoreMap()
        examsCollection(userId).document(exam.id).set(data, SetOptions.merge()).await()
        Timber.d("Exam synced: ${exam.id}")
    }

    suspend fun fetchRemoteExams(userId: String): Result<List<Map<String, Any?>>> = runCatching {
        val snapshot = examsCollection(userId).get().await()
        snapshot.documents.mapNotNull { it.data }
    }

    suspend fun deleteRemoteExam(userId: String, examId: String): Result<Unit> = runCatching {
        examsCollection(userId).document(examId).delete().await()
    }

    // ─── User Profile ─────────────────────────────────────────────────────────
    suspend fun saveUserProfile(userId: String, profile: Map<String, Any>): Result<Unit> = runCatching {
        userDocument(userId).set(profile, SetOptions.merge()).await()
    }

    suspend fun getUserProfile(userId: String): Result<Map<String, Any?>?> = runCatching {
        userDocument(userId).get().await().data
    }

    // ─── Mapping helpers ─────────────────────────────────────────────────────
    private fun TaskEntity.toFirestoreMap() = mapOf(
        "id" to id,
        "userId" to userId,
        "title" to title,
        "description" to description,
        "subjectId" to subjectId,
        "subjectName" to subjectName,
        "subjectColor" to subjectColor,
        "dueDate" to dueDate,
        "dueTime" to dueTime,
        "priority" to priority,
        "taskType" to taskType,
        "isCompleted" to isCompleted,
        "completedAt" to completedAt,
        "reminderOffsetMinutes" to reminderOffsetMinutes,
        "notes" to notes,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    private fun SubjectEntity.toFirestoreMap() = mapOf(
        "id" to id,
        "userId" to userId,
        "name" to name,
        "code" to code,
        "professor" to professor,
        "credits" to credits,
        "color" to color,
        "roomLocation" to roomLocation,
        "scheduleJson" to scheduleJson,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
        "isActive" to isActive
    )

    private fun ExamEntity.toFirestoreMap() = mapOf(
        "id" to id,
        "userId" to userId,
        "title" to title,
        "subjectId" to subjectId,
        "subjectName" to subjectName,
        "subjectColor" to subjectColor,
        "examDate" to examDate,
        "examTime" to examTime,
        "location" to location,
        "durationMinutes" to durationMinutes,
        "examType" to examType,
        "notes" to notes,
        "reminderOffsetMinutes" to reminderOffsetMinutes,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )
}
