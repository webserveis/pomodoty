package com.webserveis.app.pomodoty

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.webserveis.app.pomodoty.databinding.ActivitySplashScreenBinding
import com.webserveis.app.pomodoty.ui.WelcomeActivity


class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadSplashScreen()

    }

    private fun loadSplashScreen() {


        val delayTime = resources.getInteger(android.R.integer.config_longAnimTime).toLong() * 2
        Handler(Looper.getMainLooper()).postDelayed({

            if (MyApplication.prefs.isShowWelcome) {
                launchWelcomeScreen()
            } else {
                launchMainScreen()
            }

        }, delayTime)
    }

    private fun launchMainScreen() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        this.finish()
    }

    private fun launchWelcomeScreen() {
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        this.finish()
    }
}