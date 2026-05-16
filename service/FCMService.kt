package com.smartstudent.planner.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.smartstudent.planner.R
import com.smartstudent.planner.SmartStudentApp
import com.smartstudent.planner.ui.dashboard.MainActivity
import timber.log.Timber

class FCMService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("FCM Token refreshed: $token")
        // In production: save token to Firestore for server-side push
        saveTokenToFirestore(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.d("FCM message received from: ${message.from}")

        // Log to Analytics
        Firebase.analytics.logEvent("push_notification_received") {
            param("notification_type", message.data["type"] ?: "general")
        }

        // Handle data payload
        if (message.data.isNotEmpty()) {
            handleDataMessage(message.data)
        }

        // Show notification from notification payload
        message.notification?.let { notification ->
            showNotification(
                title = notification.title ?: getString(R.string.app_name),
                body = notification.body ?: "",
                data = message.data
            )
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"] ?: return
        when (type) {
            "task_reminder" -> showTaskReminder(data)
            "exam_reminder" -> showExamReminder(data)
            "sync_required" -> { /* trigger background sync */ }
        }
    }

    private fun showTaskReminder(data: Map<String, String>) {
        showNotification(
            title = getString(R.string.notif_task_due_title),
            body = data["body"] ?: "",
            data = data,
            channelId = SmartStudentApp.CHANNEL_TASKS
        )
    }

    private fun showExamReminder(data: Map<String, String>) {
        showNotification(
            title = getString(R.string.notif_exam_reminder_title),
            body = data["body"] ?: "",
            data = data,
            channelId = SmartStudentApp.CHANNEL_EXAMS
        )
    }

    private fun showNotification(
        title: String,
        body: String,
        data: Map<String, String> = emptyMap(),
        channelId: String = SmartStudentApp.CHANNEL_FCM
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            data.forEach { (key, value) -> putExtra(key, value) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun saveTokenToFirestore(token: String) {
        // Save FCM token to Firestore for server-side notifications
        // In production, associate with current user UID
        Timber.d("Saving FCM token to Firestore: $token")
    }
}
