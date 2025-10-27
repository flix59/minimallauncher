package com.example.minimal_launcher

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.minimal_launcher.adapters.AppListAdapter
import com.example.minimal_launcher.models.AppInfo
import com.example.minimal_launcher.services.AppDiscoveryService
import com.example.minimal_launcher.utils.PreferenceManager

class MainActivity : Activity() {
    private lateinit var priorityAppsRecyclerView: RecyclerView
    private lateinit var priorityAppsAdapter: AppListAdapter
    private lateinit var allAppsButton: TextView

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
    }

    private fun initializeViews() {
        priorityAppsRecyclerView = findViewById(R.id.rv_priority_apps)
        allAppsButton = findViewById(R.id.btn_all_apps)

        // Setup priority apps RecyclerView with 2x3 grid
        priorityAppsRecyclerView.layoutManager = GridLayoutManager(this, 2)
        priorityAppsAdapter = AppListAdapter(this)
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
            val intent = Intent(this, AllAppsActivity::class.java)
            startActivity(intent)
        }
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
