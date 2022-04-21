package com.webserveis.app.pomodoty

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class MyApplication : Application() {

    companion object {
        lateinit var instance: MyApplication
            private set

        lateinit var prefs: Prefs
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        initPreferences()
    }

    private fun initPreferences() {
        prefs = Prefs(applicationContext)

        try {
            when (prefs.darkTheme) {
                "MODE_NIGHT_NO" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                "MODE_NIGHT_YES" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                "MODE_DEFAULT_SYSTEM" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        } catch (e: ClassCastException) {
            prefs.clearSharedPreferences()
            prefs.clearDefaultSharedPreferences()
        }

    }


}