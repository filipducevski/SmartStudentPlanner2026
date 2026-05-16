package com.smartstudent.planner.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "user_id")
    val userId: String = "",

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "code")
    val code: String = "",

    @ColumnInfo(name = "professor")
    val professor: String = "",

    @ColumnInfo(name = "credits")
    val credits: Int = 0,

    @ColumnInfo(name = "color")
    val color: String = "#2563EB",   // hex color

    @ColumnInfo(name = "room_location")
    val roomLocation: String = "",

    @ColumnInfo(name = "schedule_json")
    val scheduleJson: String = "[]",   // JSON array of schedule slots

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true
)
