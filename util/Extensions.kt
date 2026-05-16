package com.smartstudent.planner.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

// ─── View extensions ─────────────────────────────────────────────────────────

fun View.show() { visibility = View.VISIBLE }
fun View.hide() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

fun View.showIf(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun View.snack(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, message, duration).show()
}

fun View.snackError(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_LONG)
        .setBackgroundTint(context.getColor(com.smartstudent.planner.R.color.error))
        .show()
}

// ─── Fragment extensions ──────────────────────────────────────────────────────

fun Fragment.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(requireContext(), message, duration).show()
}

// ─── String extensions ────────────────────────────────────────────────────────

fun String.capitalizeFirst(): String =
    if (isEmpty()) this else this[0].uppercaseChar() + substring(1)

fun String?.orDash(): String = if (isNullOrEmpty()) "—" else this

// ─── Long (millis) extensions ─────────────────────────────────────────────────

fun Long.toFormattedDate(): String = DateTimeUtil.formatDate(this)
fun Long.toFormattedDateTime(): String = DateTimeUtil.formatDateTime(this)
fun Long.toFormattedTime(): String = DateTimeUtil.formatTime(this)
fun Long.isToday(): Boolean = DateTimeUtil.isToday(this)
fun Long.isOverdue(): Boolean = DateTimeUtil.isOverdue(this)
fun Long.daysUntil(): Long = DateTimeUtil.daysUntil(this)
