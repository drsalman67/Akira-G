package com.akirag.withgemini.utils

import android.content.Context
import android.content.SharedPreferences

object Prefs {
    private const val PREF_NAME = "AkiraG_Prefs"
    private const val KEY_SAVED_APP = "saved_app_package"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveAppPackage(context: Context, packageName: String) {
        getPrefs(context).edit().putString(KEY_SAVED_APP, packageName).apply()
    }

    fun getSavedAppPackage(context: Context): String? {
        return getPrefs(context).getString(KEY_SAVED_APP, null)
    }
}
