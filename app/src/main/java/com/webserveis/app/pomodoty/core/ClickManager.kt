package com.webserveis.app.pomodoty.core

import android.content.SharedPreferences

/*
val counterClicks = CounterClicks({ println("Hello world: $it") })
counterClicks.click()
 */
class ClickManager(
    private val sp: SharedPreferences,
    private val triggerTask: (Int) -> Unit,
    private val triggerClick: Int = TRIGGER_CLICK,
    private val uid: String? = null
) {
    companion object {
        private const val KEY_CLICKS_COUNT = "clicks_count"
        private val TRIGGER_CLICK = 5
    }

    val clickCount: Int
        get() = sp.getInt(KEY_CLICKS_COUNT + uid, 0)

    init {
        evaluateTrigger()
    }

    fun incCount() {
        sp.edit().putInt(KEY_CLICKS_COUNT + uid, clickCount + 1).apply()
        evaluateTrigger()
    }

    fun resetCount() {
        sp.edit().putInt(KEY_CLICKS_COUNT + uid, 0).apply()
    }

    private fun evaluateTrigger() {
        if (clickCount >= triggerClick) {
            triggerTask(clickCount)
            resetCount()
        }
    }

}