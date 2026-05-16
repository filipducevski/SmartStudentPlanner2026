package com.smartstudent.planner.util

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralised wrapper around Firebase Analytics so every screen/action
 * is logged consistently.
 */
@Singleton
class AnalyticsHelper @Inject constructor(
    private val analytics: FirebaseAnalytics
) {

    // ─── Screen tracking ──────────────────────────────────────────────────────
    fun logScreen(screenName: String, screenClass: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        })
    }

    // ─── Task events ──────────────────────────────────────────────────────────
    fun logTaskCreated(taskType: String, priority: Int) {
        analytics.logEvent("task_created", Bundle().apply {
            putString("task_type", taskType)
            putInt("priority", priority)
        })
    }

    fun logTaskCompleted(taskId: String) {
        analytics.logEvent("task_completed", Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, taskId)
        })
    }

    fun logTaskDeleted(taskId: String) {
        analytics.logEvent("task_deleted", Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, taskId)
        })
    }

    // ─── Subject events ───────────────────────────────────────────────────────
    fun logSubjectCreated(subjectName: String) {
        analytics.logEvent("subject_created", Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_NAME, subjectName)
        })
    }

    // ─── Exam events ──────────────────────────────────────────────────────────
    fun logExamCreated(examTitle: String, subjectName: String) {
        analytics.logEvent("exam_created", Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_NAME, examTitle)
            putString("subject", subjectName)
        })
    }

    // ─── Auth events ──────────────────────────────────────────────────────────
    fun logLogin(method: String) {
        analytics.logEvent(FirebaseAnalytics.Event.LOGIN, Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
        })
    }

    fun logSignUp(method: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
        })
    }

    // ─── Sync events ──────────────────────────────────────────────────────────
    fun logSyncTriggered(source: String) {
        analytics.logEvent("sync_triggered", Bundle().apply {
            putString("source", source)
        })
    }

    fun logSyncResult(success: Boolean, itemCount: Int = 0) {
        analytics.logEvent("sync_result", Bundle().apply {
            putBoolean("success", success)
            putInt("item_count", itemCount)
        })
    }

    // ─── Notification events ──────────────────────────────────────────────────
    fun logNotificationReceived(type: String) {
        analytics.logEvent("notification_received", Bundle().apply {
            putString("notification_type", type)
        })
    }

    // ─── User properties ─────────────────────────────────────────────────────
    fun setUserProperty(name: String, value: String) {
        analytics.setUserProperty(name, value)
    }

    fun setUserId(userId: String) {
        analytics.setUserId(userId)
    }
}
