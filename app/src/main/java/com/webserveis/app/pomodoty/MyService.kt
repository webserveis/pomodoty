package com.webserveis.app.pomodoty

import android.app.*
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.webserveis.app.pomodoty.core.ext.vibrate
import com.webserveis.app.pomodoty.datasource.PomodoroTimer
import java.util.concurrent.TimeUnit


class MyService : Service() {

    companion object {
        val TAG = MyService::class.java.simpleName
        const val CHANNEL_ID = "ForegroundService Kotlin"
    }

    private val myBinder = MyLocalBinder()

    var pomodoro: PomodoroTimer? = null
    var clientCallback: PomodoroTimer.OnPomodoroListener? = null

    override fun onCreate() {
        Log.i(TAG, "onCreate")
        //super.onCreate()
        val startIntent = Intent(applicationContext, MyService::class.java)
        startIntent.putExtra("inputExtra", "aaaaa")
        ContextCompat.startForegroundService(applicationContext, startIntent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand")
        //do heavy work on a background thread
        val input: String? = intent?.getStringExtra("inputExtra")
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getText(R.string.app_name))
            .setContentText(input)
            .setSmallIcon(R.drawable.ic_outline_timer_24)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)

        pomodoro = PomodoroTimer.build {
            focusTime = TimeUnit.MINUTES.toMillis(MyApplication.prefs.pomodoroFocusLenght.toLong())
            shortBreakTime = TimeUnit.MINUTES.toMillis(MyApplication.prefs.pomodoroShortBreakLenght.toLong())
            longBreakTime = TimeUnit.MINUTES.toMillis(MyApplication.prefs.pomodoroLongBreakLenght.toLong())
            longBreakAfter = MyApplication.prefs.pomodoroLongBreakDelay
        }

        initPomodoro()
        pomodoro?.start()

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.i(TAG, "onBind")
        return myBinder
    }

    override fun onDestroy() {
        super.onDestroy()
        pomodoro?.reset()
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager: NotificationManager? = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

    inner class MyLocalBinder : Binder() {
        fun getService(): MyService {
            return this@MyService
        }
    }

    fun registerOnPomodoroListener(callback: PomodoroTimer.OnPomodoroListener) {
        clientCallback = callback
    }

    fun unregisterOnPomodoroListener() {
        clientCallback = null
    }

    private fun initPomodoro() {
        pomodoro?.callback = object : PomodoroTimer.OnPomodoroListener {
            override fun onStart() {
                super.onStart()
                clientCallback?.onStart()
            }

            override fun onTick(millisUntilFinished: Long, percent: Float) {
                super.onTick(millisUntilFinished, percent)
                clientCallback?.onTick(millisUntilFinished, percent)
                Log.d(TAG, "onTick() called with: millisUntilFinished = $millisUntilFinished, percent = $percent")
            }

            override fun onStop() {
                super.onStop()
                clientCallback?.onStop()
            }

            override fun onIntervalFinished(intervalIndex: Int) {
                super.onIntervalFinished(intervalIndex)
                clientCallback?.onIntervalFinished(intervalIndex)
                if (MyApplication.prefs.isPomodoroNotifyVibrate) {
                    applicationContext.vibrate()
                }
                if (MyApplication.prefs.isPomodoroNotifySound) {
                    playBeep()
                }
                pomodoro?.stop()

            }

            override fun onNextInterval(intervalIndex: Int) {
                super.onNextInterval(intervalIndex)

                clientCallback?.onNextInterval(intervalIndex)

                if (MyApplication.prefs.isPomodoroAutoStart) {
                    pomodoro?.start()
                }

                Handler(Looper.getMainLooper()).postDelayed({

                }, 1000)

            }

            override fun onReset() {
                super.onReset()
                clientCallback?.onReset()
            }
        }

    }

    private fun playBeep() {
        try {
            val mediaPlayer = MediaPlayer()
            val descriptor: AssetFileDescriptor = assets.openFd("desk-bell-small-sound-effect.mp3")
            mediaPlayer.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
            descriptor.close()
            mediaPlayer.prepare()
            mediaPlayer.setVolume(1F, 1F)
            mediaPlayer.isLooping = false
            mediaPlayer.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}