package com.example.minimal_launcher.services

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.example.minimal_launcher.models.AppInfo

class AppDiscoveryService(private val context: Context) {
    private val packageManager: PackageManager = context.packageManager

    fun getAllInstalledApps(): List<AppInfo> {
        val apps = mutableListOf<AppInfo>()

        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = packageManager.queryIntentActivities(mainIntent, 0)

        for (resolveInfo in resolveInfos) {
            val appName = resolveInfo.loadLabel(packageManager).toString()
            val packageName = resolveInfo.activityInfo.packageName

            // Skip our own launcher from the list
            if (packageName == context.packageName) {
                continue
            }

            try {
                val icon = resolveInfo.loadIcon(packageManager)
                val appInfo = AppInfo(appName, packageName, icon)
                apps.add(appInfo)
            } catch (e: Exception) {
                // Skip apps that can't load their icon
                continue
            }
        }

        // Sort alphabetically by app name
        return apps.sortedBy { it.appName.lowercase() }
    }

    fun getPriorityApps(allApps: List<AppInfo>, priorityPackageNames: List<String>): List<AppInfo> {
        val priorityApps = mutableListOf<AppInfo>()

        for (packageName in priorityPackageNames) {
            val app = allApps.find { it.packageName == packageName }
            if (app != null) {
                app.isPriority = true
                priorityApps.add(app)
            }
        }

        return priorityApps
    }

    fun searchApps(allApps: List<AppInfo>, query: String?): List<AppInfo> {
        if (query.isNullOrBlank()) {
            return allApps
        }

        val lowerQuery = query.lowercase()
        return allApps.filter { app ->
            app.appName.lowercase().contains(lowerQuery)
        }
    }
}
