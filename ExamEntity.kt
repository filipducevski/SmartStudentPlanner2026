package com.smartstudent.planner.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "exams",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subject_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("subject_id"), Index("exam_date")]
)
data class ExamEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "user_id")
    val userId: String = "",

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "subject_id")
    val subjectId: String? = null,

    @ColumnInfo(name = "subject_name")
    val subjectName: String = "",

    @ColumnInfo(name = "subject_color")
    val subjectColor: String = "#2563EB",

    @ColumnInfo(name = "exam_date")
    val examDate: Long,    // epoch millis

    @ColumnInfo(name = "exam_time")
    val examTime: String = "",   // "HH:mm"

    @ColumnInfo(name = "location")
    val location: String = "",

    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int = 90,

    @ColumnInfo(name = "exam_type")
    val examType: String = ExamType.WRITTEN.name,

    @ColumnInfo(name = "notes")
    val notes: String = "",

    @ColumnInfo(name = "reminder_offset_minutes")
    val reminderOffsetMinutes: Long? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false
) {
    enum class ExamType {
        WRITTEN, ORAL, PRACTICAL, ONLINE
    }

    fun isUpcoming(): Boolean = examDate > System.currentTimeMillis()

    fun daysUntil(): Long {
        val diff = examDate - System.currentTimeMillis()
        return diff / (1000 * 60 * 60 * 24)
    }
}
