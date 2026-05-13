package com.akirag.withgemini

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.akirag.withgemini.service.FloatingService

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnStart = findViewById<Button>(R.id.btnStartService)
        val btnNotif = findViewById<Button>(R.id.btnNotificationPermission)

        btnStart.setOnClickListener {
            if (checkOverlayPermission()) {
                startService(Intent(this, FloatingService::class.java))
                Toast.makeText(this, "Beast Mode ON!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                requestOverlayPermission()
            }
        }

        btnNotif.setOnClickListener {
            if (!isNotificationServiceEnabled()) {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                Toast.makeText(this, "Akira-G ko allow karo!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Permission pehle se ON hai mere bhai!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else true
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, 100)
            Toast.makeText(this, "Display over other apps allow karo!", Toast.LENGTH_LONG).show()
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val pkgName = packageName
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat?.contains(pkgName) == true
    }
}
