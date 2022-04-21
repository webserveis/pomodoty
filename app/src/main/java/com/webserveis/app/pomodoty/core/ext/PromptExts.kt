package com.webserveis.app.pomodoty.core.ext

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.annotation.StringRes
import com.webserveis.app.pomodoty.R
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun Context?.toast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) =
    this?.let { Toast.makeText(it, text, duration).show() }

fun Context?.toast(@StringRes textId: Int, duration: Int = Toast.LENGTH_SHORT) =
    this?.let { Toast.makeText(it, textId, duration).show() }

/**
 * Método de extensión para hacer vibrar.
 */
@RequiresPermission(android.Manifest.permission.VIBRATE)
fun Context.vibrate(pattern: LongArray = longArrayOf(0, 150)) {
    val vibrator = applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator? ?: return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(
            VibrationEffect.createWaveform(pattern, VibrationEffect.DEFAULT_AMPLITUDE)
        )

    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(pattern, -1)
    }
}

/**
 * Extensión Kotlin sacudir una vista.
 */
fun View.shake() {
    val objAnim = ObjectAnimator.ofFloat(
        this,
        View.TRANSLATION_X,
        0F, 15F, -15F, 15F, -15F, 6F, -6F, 3F, -3F, 0F
    ).setDuration(750)
    objAnim.start()
}

fun View.clicks(): Flow<Unit> = callbackFlow {
    setOnClickListener {
        this.trySend(Unit).isSuccess
    }
    awaitClose { setOnClickListener(null) }
}

