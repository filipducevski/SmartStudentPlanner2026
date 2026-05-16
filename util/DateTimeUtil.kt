package com.smartstudent.planner.util

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateTimeUtil {

    private val dateFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dayFormat = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("d MMM", Locale.getDefault())

    fun formatDate(millis: Long): String = dateFormat.format(Date(millis))
    fun formatDateTime(millis: Long): String = dateTimeFormat.format(Date(millis))
    fun formatTime(millis: Long): String = timeFormat.format(Date(millis))
    fun formatDayHeader(): String = dayFormat.format(Date())
    fun formatShortDate(millis: Long): String = shortDateFormat.format(Date(millis))

    fun startOfDay(millis: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun endOfDay(millis: Long = System.currentTimeMillis()): Long =
        startOfDay(millis) + TimeUnit.DAYS.toMillis(1) - 1

    fun startOfWeek(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun endOfWeek(): Long = startOfWeek() + TimeUnit.DAYS.toMillis(7)

    fun daysUntil(millis: Long): Long {
        val diff = millis - System.currentTimeMillis()
        return if (diff < 0) -1L else TimeUnit.MILLISECONDS.toDays(diff)
    }

    fun isToday(millis: Long): Boolean {
        val start = startOfDay()
        return millis in start until (start + TimeUnit.DAYS.toMillis(1))
    }

    fun isTomorrow(millis: Long): Boolean {
        val tomorrow = startOfDay() + TimeUnit.DAYS.toMillis(1)
        return millis in tomorrow until (tomorrow + TimeUnit.DAYS.toMillis(1))
    }

    fun isOverdue(millis: Long): Boolean = millis < System.currentTimeMillis()

    /** Convert "HH:mm" string + date millis into full epoch millis */
    fun combineDateTime(dateMills: Long, timeStr: String): Long {
        if (timeStr.isEmpty()) return dateMills
        return try {
            val parts = timeStr.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            Calendar.getInstance().apply {
                timeInMillis = dateMills
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }.timeInMillis
        } catch (_: Exception) {
            dateMills
        }
    }
}
