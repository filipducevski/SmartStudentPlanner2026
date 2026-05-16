package com.smartstudent.planner.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subject_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("subject_id"), Index("due_date"), Index("is_completed")]
)
data class TaskEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "user_id")
    val userId: String = "",

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "subject_id")
    val subjectId: String? = null,

    @ColumnInfo(name = "subject_name")
    val subjectName: String = "",

    @ColumnInfo(name = "subject_color")
    val subjectColor: String = "#2563EB",

    @ColumnInfo(name = "due_date")
    val dueDate: Long? = null,         // epoch millis

    @ColumnInfo(name = "due_time")
    val dueTime: String? = null,       // "HH:mm"

    @ColumnInfo(name = "priority")
    val priority: Int = Priority.MEDIUM.value,  // 0=Low 1=Med 2=High 3=Critical

    @ColumnInfo(name = "task_type")
    val taskType: String = TaskType.HOMEWORK.name,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null,

    @ColumnInfo(name = "reminder_offset_minutes")
    val reminderOffsetMinutes: Long? = null,   // minutes before due

    @ColumnInfo(name = "notes")
    val notes: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,

    @ColumnInfo(name = "firestore_id")
    val firestoreId: String = ""
) {
    enum class Priority(val value: Int) {
        LOW(0), MEDIUM(1), HIGH(2), CRITICAL(3);

        companion object {
            fun fromValue(v: Int) = values().firstOrNull { it.value == v } ?: MEDIUM
        }
    }

    enum class TaskType {
        HOMEWORK, PROJECT, READING, ASSIGNMENT, LAB, OTHER
    }
}
