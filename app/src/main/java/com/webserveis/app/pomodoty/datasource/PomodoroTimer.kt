package com.webserveis.app.pomodoty.datasource

import android.os.CountDownTimer
import android.util.Log

/*
https://stackoverflow.com/questions/36140791/how-to-implement-builder-pattern-in-kotlin
Usage: val car = Car.build { model = "X" }
 */
class PomodoroTimer(
    private val focusTime: Long,
    private val shortBreakTime: Long,
    private val longBreakTime: Long,
    private val longBreakAfter: Int
) {

    private constructor(builder: Builder) : this(builder.focusTime, builder.shortBreakTime, builder.longBreakTime, builder.longBreakAfter)

    companion object {
        private const val TAG = "PomodoroTimer"
        inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()

    }

    class Builder {
        var focusTime: Long = 20000L
        var shortBreakTime: Long = 5000L
        var longBreakTime: Long = 10000L
        var longBreakAfter: Int = 4

        fun build() = PomodoroTimer(this)
    }

    private var intervalList: MutableList<Pair<IntervalType, Long>> = arrayListOf()
    private var currentIndex: Int = 0
    fun getInterval(index: Int = currentIndex): Triple<IntervalType, Long, Long?> {
        if (index < 0 && index > intervalList.size) return Triple(IntervalType.EMPTY, 0, 0)
        return Triple(intervalList[index].first, intervalList[index].second, lastTime)
    }

    private val countDownInterval = 1000L
    private var isRunning: Boolean = false
    var callback: OnPomodoroListener? = null

    private var timer: CountDownTimer? = null
    private var lastTime: Long? = null

    //stats
    private var focusCount: Int = 0
    fun getFocusCount() = focusCount
    private var focusTimeTotal: Long = 0
    private var breakTimeTotal: Long = 0


    init {
        Log.d(TAG, "Init ")
        repeat(longBreakAfter) {
            intervalList.add(Pair(IntervalType.FOCUS, focusTime))
            if (it == longBreakAfter - 1) {
                intervalList.add(Pair(IntervalType.LONG_BREAK, longBreakTime))
            } else {
                intervalList.add(Pair(IntervalType.SHORT_BREAK, shortBreakTime))
            }
        }
        setupTimer()
    }

    private fun setupTimer(defaultTime: Long? = null) {
        timer?.cancel()
        val currentInterval = intervalList[currentIndex]
        val cTime = defaultTime ?: currentInterval.second + countDownInterval

        timer = object : CountDownTimer(cTime, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                //Log.d(TAG, "onTick() called with: millisUntilFinished = $millisUntilFinished")
                //isRunning = true
                lastTime = millisUntilFinished
                if (currentInterval.first == IntervalType.FOCUS) {
                    focusTimeTotal += countDownInterval
                } else {
                    breakTimeTotal += countDownInterval
                }
                val percent: Float = getPercentFromMillis(getInterval().second, millisUntilFinished)
                callback?.onTick(millisUntilFinished, percent)
            }

            override fun onFinish() {
                Log.d(TAG, "onFinish() called")
                if (getInterval(currentIndex).first == IntervalType.FOCUS) focusCount++
                //isRunning = false
                callback?.onIntervalFinished(currentIndex)
                nextInterval()
            }
        }

        if (isRunning) timer?.start()
    }

    fun start() {
        if (!isRunning) {
            setupTimer(lastTime)
            timer?.start()
            isRunning = true
            callback?.onStart()
        }
    }

    fun stop() {
        if (isRunning) {
            timer?.cancel()
            isRunning = false
            callback?.onStop()
        }
    }

    fun nextInterval() {
        currentIndex++
        currentIndex %= intervalList.size
        lastTime = null
        setupTimer()
        callback?.onNextInterval(currentIndex)
    }

    fun reset() {
        lastTime = null
        timer?.cancel()
        isRunning = false
        currentIndex = 0
        focusCount = 0
        focusTimeTotal = 0
        breakTimeTotal = 0
        setupTimer()
        callback?.onReset()
    }

    fun isRunning(): Boolean {
        return isRunning
    }

    fun getPercentFromMillis(millisMax: Long, millis: Long): Float {
        val aMillis = if (millis > millisMax) millisMax else millis
        return (360F * aMillis) / millisMax
    }

    fun setTimeByPercent(percent : Float): Long {
        val millisMax = getInterval().second
        val newTime = ((millisMax * percent) / 360F).toLong()
        lastTime = newTime
        return newTime
    }

    enum class IntervalType(val value: Int) {
        EMPTY(0),
        FOCUS(1),
        SHORT_BREAK(2),
        LONG_BREAK(3)
    }

    interface OnPomodoroListener {
        fun onStart() {}
        fun onTick(millisUntilFinished: Long, percent: Float) {}
        fun onStop() {}
        fun onNextInterval(intervalIndex: Int) {}
        fun onIntervalFinished(intervalIndex: Int) {}
        fun onReset() {}
    }
}