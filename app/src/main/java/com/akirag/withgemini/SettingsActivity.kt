package com.akirag.withgemini

import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.akirag.withgemini.utils.Prefs

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Switches ko dhoondhna
        val switchBattery = findViewById<Switch>(R.id.switch_battery_color)
        val switchNotif = findViewById<Switch>(R.id.switch_notif_light)
        val switchCharge = findViewById<Switch>(R.id.switch_charge_anim)

        // Purani memory check karke switches ko on/off rakhna
        switchBattery.isChecked = Prefs.isBatteryColorEnabled(this)
        switchNotif.isChecked = Prefs.isNotifLightEnabled(this)
        switchCharge.isChecked = Prefs.isChargeAnimEnabled(this)

        // Jab user switch dabaye, toh memory update karna
        switchBattery.setOnCheckedChangeListener { _, isChecked ->
            Prefs.setBatteryColorEnabled(this, isChecked)
        }
        
        switchNotif.setOnCheckedChangeListener { _, isChecked ->
            Prefs.setNotifLightEnabled(this, isChecked)
        }
        
        switchCharge.setOnCheckedChangeListener { _, isChecked ->
            Prefs.setChargeAnimEnabled(this, isChecked)
        }
    }
}
