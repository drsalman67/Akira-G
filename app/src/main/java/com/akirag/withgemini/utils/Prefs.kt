package com.akirag.withgemini.utils

import android.content.Context
import android.content.SharedPreferences

object Prefs {
    private const val PREF_NAME = "AkiraG_Prefs"
    private const val KEY_SAVED_APP = "saved_app_package"
    
    // Naye Settings Keys
    private const val KEY_BATTERY_COLOR = "battery_color_enabled"
    private const val KEY_NOTIF_LIGHT = "notif_light_enabled"
    private const val KEY_CHARGE_ANIM = "charge_anim_enabled"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // Phase 3 ka purana logic
    fun saveAppPackage(context: Context, packageName: String) {
        getPrefs(context).edit().putString(KEY_SAVED_APP, packageName).apply()
    }

    fun getSavedAppPackage(context: Context): String? {
        return getPrefs(context).getString(KEY_SAVED_APP, null)
    }

    // Phase 6 Toggles Save karna
    fun setBatteryColorEnabled(context: Context, isEnabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_BATTERY_COLOR, isEnabled).apply()
    }
    fun isBatteryColorEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_BATTERY_COLOR, false) // Default Off
    }

    fun setNotifLightEnabled(context: Context, isEnabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_NOTIF_LIGHT, isEnabled).apply()
    }
    fun isNotifLightEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_NOTIF_LIGHT, false) // Default Off
    }

    fun setChargeAnimEnabled(context: Context, isEnabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_CHARGE_ANIM, isEnabled).apply()
    }
    fun isChargeAnimEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_CHARGE_ANIM, false) // Default Off
    }
}
