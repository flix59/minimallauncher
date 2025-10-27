package com.example.minimal_launcher.models

import android.graphics.drawable.Drawable

data class AppInfo(
    val appName: String,
    val packageName: String,
    val icon: Drawable?
) {
    val firstLetter: String = if (appName.isNotEmpty()) {
        appName.first().uppercase()
    } else {
        "?"
    }

    var isPriority: Boolean = false

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AppInfo) return false
        return packageName == other.packageName
    }

    override fun hashCode(): Int {
        return packageName.hashCode()
    }
}
