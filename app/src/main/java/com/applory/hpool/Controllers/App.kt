package com.applory.hpool.Controllers

import android.app.Application
import com.applory.hpool.Utilities.SharedPrefs

class App : Application(){
    companion object {
        lateinit var prefs: SharedPrefs
    }

    override fun onCreate() {
        prefs = SharedPrefs(applicationContext)
        super.onCreate()
    }
}