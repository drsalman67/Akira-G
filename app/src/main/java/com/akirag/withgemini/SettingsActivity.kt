package com.akirag.withgemini

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.akirag.withgemini.utils.Prefs

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val switchBattery = findViewById<Switch>(R.id.switch_battery_color)
        val switchNotif = findViewById<Switch>(R.id.switch_notif_light)
        val switchCharge = findViewById<Switch>(R.id.switch_charge_anim)

        switchBattery.isChecked = Prefs.isBatteryColorEnabled(this)
        switchNotif.isChecked = Prefs.isNotifLightEnabled(this)
        switchCharge.isChecked = Prefs.isChargeAnimEnabled(this)

        switchBattery.setOnCheckedChangeListener { _, isChecked -> Prefs.setBatteryColorEnabled(this, isChecked) }
        switchNotif.setOnCheckedChangeListener { _, isChecked -> Prefs.setNotifLightEnabled(this, isChecked) }
        switchCharge.setOnCheckedChangeListener { _, isChecked -> Prefs.setChargeAnimEnabled(this, isChecked) }

        // Phase 7: 7 Neon Colors Logic
        val colors = mapOf(
            R.id.c_green to "#39FF14",
            R.id.c_cyan to "#00FFFF",
            R.id.c_blue to "#0096FF",
            R.id.c_purple to "#BC13FE",
            R.id.c_pink to "#FF10F0",
            R.id.c_orange to "#FF5F1F",
            R.id.c_red to "#FF3131"
        )

        for ((id, hexCode) in colors) {
            findViewById<Button>(id).setOnClickListener {
                Prefs.setDefaultColor(this, hexCode)
                Toast.makeText(this, "Color updated to $hexCode", Toast.LENGTH_SHORT).show()
                // Instant update ke liye FloatingService ko signal bhej rahe hain
                sendBroadcast(Intent("AKIRA_UPDATE_COLOR"))
            }
        }
    }
}
