package com.example.minimal_launcher.adapters

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.minimal_launcher.R
import com.example.minimal_launcher.models.AppInfo

class AppListAdapter(private val context: Context) :
    RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

    private var apps: List<AppInfo> = emptyList()
    private var longClickListener: OnAppLongClickListener? = null

    interface OnAppLongClickListener {
        fun onAppLongClick(app: AppInfo)
    }

    fun setApps(apps: List<AppInfo>) {
        this.apps = apps
        notifyDataSetChanged()
    }

    fun setOnAppLongClickListener(listener: OnAppLongClickListener) {
        this.longClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(apps[position])
    }

    override fun getItemCount(): Int = apps.size

    inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val firstLetterView: TextView = itemView.findViewById(R.id.tv_first_letter)
        private val appNameView: TextView = itemView.findViewById(R.id.tv_app_name)

        fun bind(app: AppInfo) {
            firstLetterView.text = app.firstLetter
            appNameView.text = app.appName

            // Click to launch app
            itemView.setOnClickListener {
                launchApp(app)
            }

            // Long click for priority management
            itemView.setOnLongClickListener {
                longClickListener?.onAppLongClick(app)
                true
            }
        }

        private fun launchApp(app: AppInfo) {
            try {
                val pm = context.packageManager
                val launchIntent = pm.getLaunchIntentForPackage(app.packageName)

                if (launchIntent != null) {
                    context.startActivity(launchIntent)
                } else {
                    Toast.makeText(context, "Cannot launch ${app.appName}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error launching ${app.appName}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
