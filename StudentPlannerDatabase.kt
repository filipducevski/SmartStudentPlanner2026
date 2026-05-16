package com.smartstudent.planner.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.smartstudent.planner.data.local.dao.ExamDao
import com.smartstudent.planner.data.local.dao.SubjectDao
import com.smartstudent.planner.data.local.dao.TaskDao
import com.smartstudent.planner.data.local.entities.ExamEntity
import com.smartstudent.planner.data.local.entities.SubjectEntity
import com.smartstudent.planner.data.local.entities.TaskEntity

@Database(
    entities = [
        TaskEntity::class,
        SubjectEntity::class,
        ExamEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class StudentPlannerDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun subjectDao(): SubjectDao
    abstract fun examDao(): ExamDao

    companion object {
        private const val DATABASE_NAME = "student_planner_db"

        @Volatile
        private var INSTANCE: StudentPlannerDatabase? = null

        fun getInstance(context: Context): StudentPlannerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudentPlannerDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Prepopulate if needed
        }
    }
}
