package com.smartstudent.planner.di

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.smartstudent.planner.data.local.StudentPlannerDatabase
import com.smartstudent.planner.data.local.dao.ExamDao
import com.smartstudent.planner.data.local.dao.SubjectDao
import com.smartstudent.planner.data.local.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ─── Database ─────────────────────────────────────────────────────────────
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StudentPlannerDatabase =
        StudentPlannerDatabase.getInstance(context)

    @Provides
    fun provideTaskDao(db: StudentPlannerDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideSubjectDao(db: StudentPlannerDatabase): SubjectDao = db.subjectDao()

    @Provides
    fun provideExamDao(db: StudentPlannerDatabase): ExamDao = db.examDao()

    // ─── Firebase ─────────────────────────────────────────────────────────────
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        val db = FirebaseFirestore.getInstance()
        // Enable offline persistence
        val settings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(com.google.firebase.firestore.FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        db.firestoreSettings = settings
        return db
    }

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics =
        FirebaseAnalytics.getInstance(context)

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()
}
