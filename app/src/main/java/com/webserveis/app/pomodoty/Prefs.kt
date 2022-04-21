package com.webserveis.app.pomodoty

import android.content.Context
import com.webserveis.app.pomodoty.core.PrefsHelper

/*
https://blog.teamtreehouse.com/making-sharedpreferences-easy-with-kotlin
https://blogs.naxam.net/sharedpreferences-made-easy-with-kotlin-generics-extensions-6189d8902fb0
 */
class Prefs(context: Context) : PrefsHelper(context) {

    val darkTheme: String?
        get() = getDefaultSharedPreferences().getString(context.getString(R.string.pref_app_theme_key), "MODE_DEFAULT_SYSTEM")
        //set(value) = getDefaultSharedPreferences().edit().putString(context.getString(R.string.pref_app_theme_key), value).apply()

    var isShowWelcome: Boolean
        get() = getSharedPreferences().getBoolean("SHOW_WELCOME_SCREEN", true)
        set(value) = getSharedPreferences().edit().putBoolean("SHOW_WELCOME_SCREEN", value).apply()

    //Pomodoro preferences
    val pomodoroFocusLenght: Int
        get() = getDefaultSharedPreferences().getInt(context.getString(R.string.pref_pomodoro_focus_key), 25)

    val pomodoroShortBreakLenght: Int
        get() = getDefaultSharedPreferences().getInt(context.getString(R.string.pref_pomodoro_short_key), 5)

    val pomodoroLongBreakLenght: Int
        get() = getDefaultSharedPreferences().getInt(context.getString(R.string.pref_pomodoro_long_key), 15)

    val pomodoroLongBreakDelay: Int
        get() = getDefaultSharedPreferences().getInt(context.getString(R.string.pref_pomodoro_long_delay_key), 15)

    val isPomodoroAutoStart: Boolean
        get() = getDefaultSharedPreferences().getBoolean(context.getString(R.string.pref_pomodoro_auto_start_key), true)

    var isPomodoroNotifySound: Boolean
        get() = getDefaultSharedPreferences().getBoolean(context.getString(R.string.pref_pomodoro_notify_sound_key), true)
        set(value) = getDefaultSharedPreferences().edit().putBoolean(context.getString(R.string.pref_pomodoro_notify_sound_key), value).apply()

    val isPomodoroNotifyVibrate: Boolean
        get() = getDefaultSharedPreferences().getBoolean(context.getString(R.string.pref_pomodoro_notify_vibrate_key), true)

    val isPomodoroKeepScreenOn: Boolean
        get() = getDefaultSharedPreferences().getBoolean(context.getString(R.string.pref_pomodoro_keep_screen_on_key), false)

}