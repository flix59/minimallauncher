package com.example.minimal_launcher

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.minimal_launcher.adapters.AppListAdapter
import com.example.minimal_launcher.models.AppInfo
import com.example.minimal_launcher.services.AppDiscoveryService
import com.example.minimal_launcher.utils.PreferenceManager

class AllAppsActivity : Activity(), AppListAdapter.OnAppLongClickListener {
    private lateinit var allAppsRecyclerView: RecyclerView
    private lateinit var allAppsAdapter: AppListAdapter
    private lateinit var searchEditText: EditText
    private lateinit var backButton: TextView

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

    override fun onAppLongClick(app: AppInfo) {
        val isPriority = preferenceManager.isPriorityApp(app.packageName)

        if (isPriority) {
            showRemoveFromPriorityDialog(app)
        } else {
            showAddToPriorityDialog(app)
        }
    }

    private fun showAddToPriorityDialog(app: AppInfo) {
        if (!preferenceManager.canAddMorePriorityApps()) {
            Toast.makeText(
                this,
                "Maximum ${preferenceManager.getMaxPriorityApps()} priority apps allowed",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Add to Priority")
            .setMessage("Add ${app.appName} to priority apps?")
            .setPositiveButton("Add") { _, _ ->
                if (preferenceManager.addPriorityApp(app.packageName)) {
                    Toast.makeText(this, "${app.appName} added to priority", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to add to priority", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRemoveFromPriorityDialog(app: AppInfo) {
        AlertDialog.Builder(this)
            .setTitle("Remove from Priority")
            .setMessage("Remove ${app.appName} from priority apps?")
            .setPositiveButton("Remove") { _, _ ->
                if (preferenceManager.removePriorityApp(app.packageName)) {
                    Toast.makeText(this, "${app.appName} removed from priority", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to remove from priority", Toast.LENGTH_SHORT).show()
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
