package com.akirag.withgemini.apppicker

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akirag.withgemini.R
import com.akirag.withgemini.utils.Prefs

data class AppInfo(val name: String, val packageName: String, val icon: Drawable)

class AppPickerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_picker)

        val rvApps = findViewById<RecyclerView>(R.id.rvApps)
        rvApps.layoutManager = LinearLayoutManager(this)

        val installedApps = getInstalledApps()
        
        rvApps.adapter = AppAdapter(installedApps) { selectedApp ->
            // Jab koi app select hogi, hum uska package name save kar lenge
            Prefs.saveAppPackage(this, selectedApp.packageName)
            finish() // Screen close
        }
    }

    private fun getInstalledApps(): List<AppInfo> {
        val pm = packageManager
        val appsList = mutableListOf<AppInfo>()
        
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        
        val allApps = pm.queryIntentActivities(intent, 0)
        for (resolveInfo in allApps) {
            val name = resolveInfo.loadLabel(pm).toString()
            val packageName = resolveInfo.activityInfo.packageName
            val icon = resolveInfo.loadIcon(pm)
            appsList.add(AppInfo(name, packageName, icon))
        }
        return appsList.sortedBy { it.name.lowercase() }
    }

    // Adapter Class idhar hi hai taake multiple files na banani pade
    inner class AppAdapter(
        private val apps: List<AppInfo>,
        private val onItemClick: (AppInfo) -> Unit
    ) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

        inner class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val icon: ImageView = view.findViewById(R.id.ivAppIcon)
            val name: TextView = view.findViewById(R.id.tvAppName)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
            return AppViewHolder(view)
        }

        override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
            val app = apps[position]
            holder.icon.setImageDrawable(app.icon)
            holder.name.text = app.name
            holder.itemView.setOnClickListener { onItemClick(app) }
        }

        override fun getItemCount() = apps.size
    }
}
