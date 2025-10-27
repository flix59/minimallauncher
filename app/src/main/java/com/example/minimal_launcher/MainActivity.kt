package com.example.minimal_launcher

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.minimal_launcher.adapters.AppListAdapter
import com.example.minimal_launcher.models.AppInfo
import com.example.minimal_launcher.services.AppDiscoveryService
import com.example.minimal_launcher.utils.PreferenceManager
import kotlin.math.abs

class MainActivity : Activity() {
    private lateinit var priorityAppsRecyclerView: RecyclerView
    private lateinit var priorityAppsAdapter: AppListAdapter
    private lateinit var allAppsButton: TextView
    private lateinit var rootView: View
    private lateinit var gestureDetector: GestureDetector

    private lateinit var appDiscoveryService: AppDiscoveryService
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var allApps: List<AppInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        initializeServices()
        setupPriorityApps()
        setupAllAppsButton()
        setupSwipeGesture()
    }

    private fun initializeViews() {
        priorityAppsRecyclerView = findViewById(R.id.rv_priority_apps)
        allAppsButton = findViewById(R.id.btn_all_apps)
        rootView = findViewById(android.R.id.content)

        // Setup priority apps RecyclerView with 2x3 grid
        priorityAppsRecyclerView.layoutManager = GridLayoutManager(this, 2)
        priorityAppsAdapter = AppListAdapter(this, showIcons = true)
        priorityAppsRecyclerView.adapter = priorityAppsAdapter
    }

    private fun initializeServices() {
        appDiscoveryService = AppDiscoveryService(this)
        preferenceManager = PreferenceManager(this)
    }

    private fun setupPriorityApps() {
        // Load all apps
        allApps = appDiscoveryService.getAllInstalledApps()

        // Get priority apps
        val priorityPackages = preferenceManager.getPriorityAppPackages()
        val priorityApps = appDiscoveryService.getPriorityApps(allApps, priorityPackages)

        // Display priority apps
        priorityAppsAdapter.setApps(priorityApps)
    }

    private fun setupAllAppsButton() {
        allAppsButton.setOnClickListener {
            openAllAppsActivity()
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
                        if (diffX < 0) {
                            // Swipe left - open all apps
                            openAllAppsActivity()
                            return true
                        }
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

    private fun openAllAppsActivity() {
        val intent = Intent(this, AllAppsActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }

    override fun onResume() {
        super.onResume()
        // Refresh priority apps when returning from AllAppsActivity
        setupPriorityApps()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Do nothing - prevent back button from closing launcher
    }
}
