package com.webserveis.app.pomodoty.core

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.webserveis.app.pomodoty.BuildConfig

/*
https://blog.teamtreehouse.com/making-sharedpreferences-easy-with-kotlin
https://blogs.naxam.net/sharedpreferences-made-easy-with-kotlin-generics-extensions-6189d8902fb0
 */
abstract class PrefsHelper(val context: Context) {

    companion object {
        const val PREFS_FILENAME = BuildConfig.APPLICATION_ID + ".prefs"
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
    private val defaultSharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun getSharedPreferences(): SharedPreferences {
        return sharedPreferences
    }

    fun getDefaultSharedPreferences(): SharedPreferences {
        return defaultSharedPreferences
    }
    fun getDefaultSharedPreferencesName(): String = context.packageName + "_preferences"

    fun clearSharedPreferences() {
        sharedPreferences.edit().clear().apply()
    }

    fun clearDefaultSharedPreferences() {
        defaultSharedPreferences.edit().clear().apply()
    }


}

