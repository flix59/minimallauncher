package com.example.minimal_launcher.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getPriorityAppPackages(): List<String> {
        val packageSet = sharedPreferences.getStringSet(KEY_PRIORITY_APPS, emptySet()) ?: emptySet()
        return packageSet.toList()
    }

    fun addPriorityApp(packageName: String): Boolean {
        val currentApps = getPriorityAppPackages().toMutableSet()

        // No limit on number of apps
        currentApps.add(packageName)
        return sharedPreferences.edit()
            .putStringSet(KEY_PRIORITY_APPS, currentApps)
            .commit()
    }

    fun removePriorityApp(packageName: String): Boolean {
        val currentApps = getPriorityAppPackages().toMutableSet()
        currentApps.remove(packageName)

        return sharedPreferences.edit()
            .putStringSet(KEY_PRIORITY_APPS, currentApps)
            .commit()
    }

    fun isPriorityApp(packageName: String): Boolean {
        return getPriorityAppPackages().contains(packageName)
    }

    fun getPriorityAppsCount(): Int {
        return getPriorityAppPackages().size
    }

    fun canAddMorePriorityApps(): Boolean {
        return true // No limit
    }

    companion object {
        private const val PREFS_NAME = "minimal_launcher_prefs"
        private const val KEY_PRIORITY_APPS = "priority_apps"
    }
}
