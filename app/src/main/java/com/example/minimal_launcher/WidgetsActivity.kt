package com.example.minimal_launcher

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.minimal_launcher.services.CalendarService
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class WidgetsActivity : Activity() {
    private lateinit var closeButton: TextView
    private lateinit var settingsButton: TextView
    private lateinit var openCalendarButton: TextView
    private lateinit var monthYearText: TextView
    private lateinit var calendarGrid: LinearLayout
    private lateinit var eventsContainer: LinearLayout
    private lateinit var calendarService: CalendarService
    private lateinit var gestureDetector: GestureDetector

    companion object {
        private const val CALENDAR_PERMISSION_REQUEST = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widgets)

        closeButton = findViewById(R.id.btn_close)
        settingsButton = findViewById(R.id.btn_settings)
        openCalendarButton = findViewById(R.id.btn_open_calendar)
        monthYearText = findViewById(R.id.tv_month_year)
        calendarGrid = findViewById(R.id.calendar_grid)
        eventsContainer = findViewById(R.id.events_container)

        calendarService = CalendarService(this)

        setupButtons()
        setupCalendar()
        setupUpcomingEvents()
        setupSwipeGesture()
    }

    private fun setupButtons() {
        closeButton.setOnClickListener {
            finish()
        }

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        openCalendarButton.setOnClickListener {
            openCalendarApp()
        }
    }

    private fun openCalendarApp() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("content://com.android.calendar/time")
            }
            startActivity(intent)
        } catch (e: Exception) {
            // If calendar app not found, try generic calendar intent
            try {
                val intent = Intent(Intent.ACTION_INSERT).apply {
                    data = CalendarContract.Events.CONTENT_URI
                }
                startActivity(intent)
            } catch (e2: Exception) {
                Toast.makeText(this, "No calendar app found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupUpcomingEvents() {
        if (!calendarService.hasCalendarPermission()) {
            // Request permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CALENDAR),
                CALENDAR_PERMISSION_REQUEST
            )
            return
        }

        displayUpcomingEvents()
    }

    private fun displayUpcomingEvents() {
        eventsContainer.removeAllViews()
        val events = calendarService.getUpcomingEvents()

        if (events.isEmpty()) {
            val noEventsText = TextView(this).apply {
                text = "No upcoming events"
                setTextColor(Color.GRAY)
                textSize = 14f
                setPadding(16, 16, 16, 16)
            }
            eventsContainer.addView(noEventsText)
        } else {
            events.forEach { event ->
                val eventView = createEventView(event)
                eventsContainer.addView(eventView)
            }
        }
    }

    private fun createEventView(event: com.example.minimal_launcher.services.CalendarEvent): LinearLayout {
        return LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 8)
            }
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 12, 16, 12)
            setBackgroundColor(Color.parseColor("#1A1A1A"))

            // Time/Date
            addView(TextView(this@WidgetsActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    weight = 0.3f
                }
                text = if (event.allDay) event.getFormattedDate() else event.getFormattedTime()
                setTextColor(Color.GRAY)
                textSize = 12f
            })

            // Event title
            addView(TextView(this@WidgetsActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    weight = 0.7f
                    marginStart = 16
                }
                text = event.title
                setTextColor(Color.WHITE)
                textSize = 14f
                maxLines = 2
            })
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CALENDAR_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                displayUpcomingEvents()
            } else {
                Toast.makeText(this, "Calendar permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupCalendar() {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_MONTH)

        // Set month/year text
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        monthYearText.text = monthFormat.format(calendar.time)

        // Get first day of month and number of days
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Create calendar grid
        var dayCounter = 1
        var currentRow: LinearLayout? = null

        // Add empty cells for days before first day of month
        var cellCount = 0
        while (cellCount < firstDayOfWeek) {
            if (cellCount % 7 == 0) {
                currentRow = createWeekRow()
                calendarGrid.addView(currentRow)
            }
            currentRow?.addView(createDayCell("", false))
            cellCount++
        }

        // Add days of the month
        while (dayCounter <= daysInMonth) {
            if (cellCount % 7 == 0) {
                currentRow = createWeekRow()
                calendarGrid.addView(currentRow)
            }

            val isToday = dayCounter == today
            currentRow?.addView(createDayCell(dayCounter.toString(), isToday))

            dayCounter++
            cellCount++
        }

        // Fill remaining cells in last row
        while (cellCount % 7 != 0) {
            currentRow?.addView(createDayCell("", false))
            cellCount++
        }
    }

    private fun createWeekRow(): LinearLayout {
        return LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            weightSum = 7f
        }
    }

    private fun createDayCell(day: String, isToday: Boolean): TextView {
        return TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                resources.displayMetrics.density.toInt() * 40 // 40dp height
            ).apply {
                weight = 1f
            }
            text = day
            gravity = Gravity.CENTER
            textSize = 14f

            if (isToday) {
                setTextColor(Color.BLACK)
                setBackgroundColor(Color.WHITE)
            } else {
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.TRANSPARENT)
            }

            setPadding(8, 8, 8, 8)
        }
    }

    private fun setupSwipeGesture() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false

                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y

                if (abs(diffX) > abs(diffY)) {
                    if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        // Swipe left or right - go back to home
                        finish()
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        return true
                    }
                }
                return false
            }
        })
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finish()
    }
}
