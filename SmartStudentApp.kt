package com.smartstudent.planner

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class SmartStudentApp : Application() {

    lateinit var analytics: FirebaseAnalytics
        private set

    override fun onCreate() {
        super.onCreate()

        // Timber logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Firebase Analytics
        analytics = Firebase.analytics
        analytics.setAnalyticsCollectionEnabled(true)

        // Facebook SDK
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)

        // Notification channels
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Tasks channel
            val tasksChannel = NotificationChannel(
                CHANNEL_TASKS,
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming task deadlines"
                enableVibration(true)
                enableLights(true)
            }

            // Exams channel
            val examsChannel = NotificationChannel(
                CHANNEL_EXAMS,
                "Exam Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming exams"
                enableVibration(true)
                enableLights(true)
            }

            // FCM channel
            val fcmChannel = NotificationChannel(
                CHANNEL_FCM,
                "Push Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Firebase push notifications"
            }

            notificationManager.createNotificationChannels(
                listOf(tasksChannel, examsChannel, fcmChannel)
            )
        }
    }

    companion object {
        const val CHANNEL_TASKS = "student_planner_tasks"
        const val CHANNEL_EXAMS = "student_planner_exams"
        const val CHANNEL_FCM = "student_planner_channel"
    }
}
