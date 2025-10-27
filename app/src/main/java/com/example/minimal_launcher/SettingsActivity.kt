package com.example.minimal_launcher

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class SettingsActivity : Activity() {
    private lateinit var closeButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        closeButton = findViewById(R.id.btn_close)
        closeButton.setOnClickListener {
            finish()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finish()
    }
}
