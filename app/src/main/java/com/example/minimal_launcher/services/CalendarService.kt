package com.example.minimal_launcher.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

data class CalendarEvent(
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val allDay: Boolean
) {
    fun getFormattedTime(): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = Date(startTime)
        return dateFormat.format(date)
    }

    fun getFormattedDate(): String {
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val date = Date(startTime)
        return dateFormat.format(date)
    }
}

class CalendarService(private val context: Context) {

    fun hasCalendarPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getUpcomingEvents(daysAhead: Int = 7): List<CalendarEvent> {
        if (!hasCalendarPermission()) {
            return emptyList()
        }

        val events = mutableListOf<CalendarEvent>()
        val calendar = Calendar.getInstance()
        val startTime = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_MONTH, daysAhead)
        val endTime = calendar.timeInMillis

        val projection = arrayOf(
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.ALL_DAY
        )

        val selection = "(( ${CalendarContract.Events.DTSTART} >= ? ) AND " +
                "( ${CalendarContract.Events.DTSTART} <= ? ))"
        val selectionArgs = arrayOf(startTime.toString(), endTime.toString())
        val sortOrder = "${CalendarContract.Events.DTSTART} ASC"

        try {
            context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val titleIndex = cursor.getColumnIndex(CalendarContract.Events.TITLE)
                val startIndex = cursor.getColumnIndex(CalendarContract.Events.DTSTART)
                val endIndex = cursor.getColumnIndex(CalendarContract.Events.DTEND)
                val allDayIndex = cursor.getColumnIndex(CalendarContract.Events.ALL_DAY)

                while (cursor.moveToNext() && events.size < 5) { // Limit to 5 events
                    val title = cursor.getString(titleIndex) ?: "Untitled"
                    val start = cursor.getLong(startIndex)
                    val end = cursor.getLong(endIndex)
                    val allDay = cursor.getInt(allDayIndex) == 1

                    events.add(CalendarEvent(title, start, end, allDay))
                }
            }
        } catch (e: Exception) {
            // Silently fail if there are calendar permission issues
        }

        return events
    }
}
