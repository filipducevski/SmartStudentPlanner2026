package com.smartstudent.planner.service

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.smartstudent.planner.R
import com.smartstudent.planner.SmartStudentApp
import com.smartstudent.planner.data.local.entities.ExamEntity
import com.smartstudent.planner.data.local.entities.TaskEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleTaskReminder(task: TaskEntity) {
        val dueDate = task.dueDate ?: return
        val offset = task.reminderOffsetMinutes ?: return
        val triggerAt = dueDate - (offset * 60 * 1000)

        if (triggerAt <= System.currentTimeMillis()) {
            Timber.d("Task reminder time already passed: ${task.id}")
            return
        }

        val intent = createNotificationIntent(
            id = task.id,
            title = context.getString(R.string.notif_task_due_title),
            body = task.title,
            type = "task",
            channelId = SmartStudentApp.CHANNEL_TASKS
        )

        scheduleAlarm(task.id.hashCode(), triggerAt, intent)
        Timber.d("Task reminder scheduled for: ${task.title} at $triggerAt")
    }

    fun scheduleExamReminder(exam: ExamEntity) {
        val offset = exam.reminderOffsetMinutes ?: return
        val triggerAt = exam.examDate - (offset * 60 * 1000)

        if (triggerAt <= System.currentTimeMillis()) return

        val intent = createNotificationIntent(
            id = exam.id,
            title = context.getString(R.string.notif_exam_reminder_title),
            body = exam.title,
            type = "exam",
            channelId = SmartStudentApp.CHANNEL_EXAMS
        )

        scheduleAlarm(exam.id.hashCode(), triggerAt, intent)
        Timber.d("Exam reminder scheduled for: ${exam.title} at $triggerAt")
    }

    fun cancelTaskReminder(taskId: String) {
        cancelAlarm(taskId.hashCode(), "task")
    }

    fun cancelExamReminder(examId: String) {
        cancelAlarm(examId.hashCode(), "exam")
    }

    private fun createNotificationIntent(
        id: String, title: String, body: String,
        type: String, channelId: String
    ): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("id", id)
            putExtra("title", title)
            putExtra("body", body)
            putExtra("type", type)
            putExtra("channelId", channelId)
        }
        return PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun scheduleAlarm(requestCode: Int, triggerAt: Long, pendingIntent: PendingIntent) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        } catch (e: SecurityException) {
            Timber.e(e, "Cannot schedule exact alarm")
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    private fun cancelAlarm(requestCode: Int, type: String) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: return
        val body = intent.getStringExtra("body") ?: return
        val channelId = intent.getStringExtra("channelId") ?: SmartStudentApp.CHANNEL_TASKS

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Timber.d("Device rebooted — reschedule reminders")
            // In production: query DB and reschedule all pending reminders
        }
    }
}
