package com.example.minimal_launcher

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class WidgetsActivity : Activity() {
    private lateinit var closeButton: TextView
    private lateinit var settingsButton: TextView
    private lateinit var monthYearText: TextView
    private lateinit var calendarGrid: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widgets)

        closeButton = findViewById(R.id.btn_close)
        settingsButton = findViewById(R.id.btn_settings)
        monthYearText = findViewById(R.id.tv_month_year)
        calendarGrid = findViewById(R.id.calendar_grid)

        setupButtons()
        setupCalendar()
    }

    private fun setupButtons() {
        closeButton.setOnClickListener {
            finish()
        }

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finish()
    }
}
