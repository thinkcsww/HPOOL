package com.applory.hpool.Utilities

import android.content.Context
import android.content.SharedPreferences

class SharedPrefs (context: Context) {

    val PREFS_FILENAME = "prefs"
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0)

    val IS_JOINED = "isJoined"

    var isJoined: Boolean
        get() = prefs.getBoolean(IS_JOINED, false)
        set(value) = prefs.edit().putBoolean(IS_JOINED, value).apply()
}