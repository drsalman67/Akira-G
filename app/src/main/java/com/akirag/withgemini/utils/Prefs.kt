package com.akirag.withgemini.utils

import android.content.Context
import android.content.SharedPreferences

object Prefs {
    private const val PREF_NAME = "AkiraG_Prefs"
    private const val KEY_SAVED_APP = "saved_app_package"
    private const val KEY_BATTERY_COLOR = "battery_color_enabled"
    private const val KEY_NOTIF_LIGHT = "notif_light_enabled"
    private const val KEY_CHARGE_ANIM = "charge_anim_enabled"
    private const val KEY_DEFAULT_COLOR = "default_neon_color" // Naya Color Key

    private fun getPrefs(context: Context): SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveAppPackage(context: Context, packageName: String) = getPrefs(context).edit().putString(KEY_SAVED_APP, packageName).apply()
    fun getSavedAppPackage(context: Context): String? = getPrefs(context).getString(KEY_SAVED_APP, null)

    fun setBatteryColorEnabled(context: Context, isEnabled: Boolean) = getPrefs(context).edit().putBoolean(KEY_BATTERY_COLOR, isEnabled).apply()
    fun isBatteryColorEnabled(context: Context): Boolean = getPrefs(context).getBoolean(KEY_BATTERY_COLOR, false)

    fun setNotifLightEnabled(context: Context, isEnabled: Boolean) = getPrefs(context).edit().putBoolean(KEY_NOTIF_LIGHT, isEnabled).apply()
    fun isNotifLightEnabled(context: Context): Boolean = getPrefs(context).getBoolean(KEY_NOTIF_LIGHT, false)

    fun setChargeAnimEnabled(context: Context, isEnabled: Boolean) = getPrefs(context).edit().putBoolean(KEY_CHARGE_ANIM, isEnabled).apply()
    fun isChargeAnimEnabled(context: Context): Boolean = getPrefs(context).getBoolean(KEY_CHARGE_ANIM, false)

    // Phase 7: Color Memory (Default Neon Green hai)
    fun setDefaultColor(context: Context, hexColor: String) = getPrefs(context).edit().putString(KEY_DEFAULT_COLOR, hexColor).apply()
    fun getDefaultColor(context: Context): String = getPrefs(context).getString(KEY_DEFAULT_COLOR, "#39FF14") ?: "#39FF14"
}
