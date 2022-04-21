package com.webserveis.app.pomodoty.core.ext

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration

fun Context.isTablet(): Boolean {
    val xlarge = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_XLARGE
    val large =
        resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_LARGE
    return xlarge || large
}

@SuppressLint("SourceLockedOrientationActivity")
fun Activity.setPortraitSmartphone() {
    val isTablet = this.isTablet()//resources.getBoolean(R.bool.isTablet)
    val orientation = resources.configuration.orientation
    if (isTablet) {
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
    } else if (orientation != Configuration.ORIENTATION_PORTRAIT) {
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}

@Suppress("DEPRECATION")
fun <T> Context.isServiceRunning(service: Class<T>): Boolean {
    return (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
        .getRunningServices(Integer.MAX_VALUE)
        .any { it.service.className == service.name }
}