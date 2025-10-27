package com.example.minimal_launcher

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.minimal_launcher.adapters.AppListAdapter
import com.example.minimal_launcher.models.AppInfo
import com.example.minimal_launcher.services.AppDiscoveryService
import com.example.minimal_launcher.utils.PreferenceManager
import kotlin.math.abs

class AllAppsActivity : Activity(), AppListAdapter.OnAppLongClickListener {
    private lateinit var allAppsRecyclerView: RecyclerView
    private lateinit var allAppsAdapter: AppListAdapter
    private lateinit var searchEditText: EditText
    private lateinit var backButton: TextView
    private lateinit var gestureDetector: GestureDetector

    private lateinit var appDiscoveryService: AppDiscoveryService
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var allApps: List<AppInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_apps)

        initializeViews()
        initializeServices()
        setupApps()
        setupSearch()
        setupBackButton()
        setupSwipeGesture()
    }

    private fun initializeViews() {
        allAppsRecyclerView = findViewById(R.id.rv_all_apps)
        searchEditText = findViewById(R.id.et_search)
        backButton = findViewById(R.id.btn_back)

        // Setup RecyclerView
        allAppsRecyclerView.layoutManager = LinearLayoutManager(this)
        allAppsAdapter = AppListAdapter(this)
        allAppsAdapter.setOnAppLongClickListener(this)
        allAppsRecyclerView.adapter = allAppsAdapter
    }

    private fun initializeServices() {
        appDiscoveryService = AppDiscoveryService(this)
        preferenceManager = PreferenceManager(this)
    }

    private fun setupApps() {
        allApps = appDiscoveryService.getAllInstalledApps()
        allAppsAdapter.setApps(allApps)
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim()
                val filteredApps = appDiscoveryService.searchApps(allApps, query)
                allAppsAdapter.setApps(filteredApps)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupBackButton() {
        backButton.setOnClickListener {
            finish()
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
                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
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

    override fun onAppLongClick(app: AppInfo) {
        val isPriority = preferenceManager.isPriorityApp(app.packageName)

        if (isPriority) {
            showRemoveFromPriorityDialog(app)
        } else {
            showAddToPriorityDialog(app)
        }
    }

    private fun showAddToPriorityDialog(app: AppInfo) {
        AlertDialog.Builder(this)
            .setTitle("Add to Home")
            .setMessage("Add ${app.appName} to home screen?")
            .setPositiveButton("Add") { _, _ ->
                if (preferenceManager.addPriorityApp(app.packageName)) {
                    Toast.makeText(this, "${app.appName} added to home", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to add to home", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRemoveFromPriorityDialog(app: AppInfo) {
        AlertDialog.Builder(this)
            .setTitle("Remove from Home")
            .setMessage("Remove ${app.appName} from home screen?")
            .setPositiveButton("Remove") { _, _ ->
                if (preferenceManager.removePriorityApp(app.packageName)) {
                    Toast.makeText(this, "${app.appName} removed from home", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to remove from home", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finish()
    }
}
