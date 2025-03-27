 package com.example.myapplication.utils

import android.content.Context
import androidx.core.content.edit

object TokenManager {
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_FIRST_LAUNCH = "first_launch"

    fun saveToken(context: Context, token: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_TOKEN, token) }
    }

    fun getToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_TOKEN, null)
    }

    fun clearToken(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { remove(KEY_TOKEN) }
    }

    fun isFirstLaunch(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isFirst = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        if (isFirst) {
            prefs.edit { putBoolean(KEY_FIRST_LAUNCH, false) }
        }
        return isFirst
    }
}