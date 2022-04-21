package com.webserveis.app.pomodoty.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.webserveis.app.pomodoty.MainActivity
import com.webserveis.app.pomodoty.MyApplication
import com.webserveis.app.pomodoty.core.ext.setPortraitSmartphone
import com.webserveis.app.pomodoty.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.setPortraitSmartphone()
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnContinue.setOnClickListener {

            MyApplication.prefs.isShowWelcome = false

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            this.finish()

        }
    }
}