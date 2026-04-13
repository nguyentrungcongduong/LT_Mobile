package com.gymapp.android.data.local

import android.content.Context

object Prefs {
    private const val PREF_NAME = "app_prefs"

    fun setHasSetupGoal(context: Context, userId: String, value: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("goal_$userId", value).apply()
    }

    fun hasSetupGoal(context: Context, userId: String): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("goal_$userId", false)
    }
}