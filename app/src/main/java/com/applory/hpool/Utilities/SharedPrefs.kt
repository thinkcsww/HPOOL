package com.applory.hpool.Utilities

import android.content.Context
import android.content.SharedPreferences

class SharedPrefs (context: Context) {

    val PREFS_FILENAME = "prefs"
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0)

    val IS_JOINED = "isJoined"
    val ROOM_ID = "roomId"
    val NICKNAME = "nickname"

    var isJoined: Boolean
        get() = prefs.getBoolean(IS_JOINED, false)
        set(value) = prefs.edit().putBoolean(IS_JOINED, value).apply()

    var roomId: String
        get() = prefs.getString(ROOM_ID, "empty")
        set(value) = prefs.edit().putString(ROOM_ID, value).apply()

    var nickname: String
        get() = prefs.getString(NICKNAME, "empty    ")
        set(value) = prefs.edit().putString(NICKNAME, value).apply()
}