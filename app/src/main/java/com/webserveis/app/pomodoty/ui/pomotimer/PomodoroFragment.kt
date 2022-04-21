package com.webserveis.app.pomodoty.ui.pomotimer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import com.webserveis.app.pomodoty.MyApplication
import com.webserveis.app.pomodoty.MyService
import com.webserveis.app.pomodoty.R
import com.webserveis.app.pomodoty.core.ext.isServiceRunning
import com.webserveis.app.pomodoty.databinding.FragmentPomodoroTimerBinding
import com.webserveis.app.pomodoty.datasource.PomodoroTimer
import me.tankery.lib.circularseekbar.CircularSeekBar
import me.tankery.lib.circularseekbar.CircularSeekBar.OnCircularSeekBarChangeListener
import java.util.concurrent.TimeUnit

class PomodoroFragment : Fragment(), ServiceConnection {

    companion object {
        private val TAG = PomodoroFragment::class.java.simpleName
    }

    private var _binding: FragmentPomodoroTimerBinding? = null
    private val binding get() = _binding!!

    private var isBound: Boolean = false
    private var myServiceMy: MyService? = null

    private var pomodoro: PomodoroTimer? = null

    private val callback = object : PomodoroTimer.OnPomodoroListener {
        override fun onStart() {
            super.onStart()
            renderButtonsUI(pomodoro?.isRunning() == true)
        }

        override fun onTick(millisUntilFinished: Long, percent: Float) {
            super.onTick(millisUntilFinished, percent)
            renderTimeRemainingUI(millisUntilFinished, percent)

        }

        override fun onStop() {
            super.onStop()
            renderButtonsUI(pomodoro?.isRunning() == true)

        }

        override fun onIntervalFinished(intervalIndex: Int) {
            super.onIntervalFinished(intervalIndex)
            val interval = pomodoro?.getInterval(intervalIndex)
            Log.d(TAG, "onIntervalFinished() called with: interval = $interval")

            renderButtonsUI(pomodoro?.isRunning() == true)

        }

        override fun onNextInterval(intervalIndex: Int) {
            super.onNextInterval(intervalIndex)
            //Log.d(TAG, "onNextInterval() called with: intervalIndex = $intervalIndex")
            pomodoro?.let {
                var (interval, millisTotal, millisUntilFinished) = it.getInterval(intervalIndex)
                Log.d(TAG, "onNextInterval() called $interval, $millisTotal, $millisUntilFinished")
                millisUntilFinished = millisUntilFinished ?: millisTotal
                renderDisplayUI(interval)
                renderTimeRemainingUI(millisUntilFinished, 360F)
            }
            Handler(Looper.getMainLooper()).postDelayed({
                //pomodoro?.start()
            }, 1000)

        }

        override fun onReset() {
            super.onReset()

            resetDisplayUI()


        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        /*val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {

            if (requireContext().isServiceRunning(MyService::class.java)) {
                showPomodoroCloseDialog()

            } else {
                if (!findNavController().navigateUp()) {
                    if (isEnabled) {
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        }*/

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPomodoroTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        if (requireContext().isServiceRunning(MyService::class.java)) {
            requireContext().bindService(Intent(requireContext(), MyService::class.java), this, Context.BIND_AUTO_CREATE)
            myServiceMy?.registerOnPomodoroListener(callback)
        }

    }

    override fun onStop() {
        super.onStop()
        unbindSafely()
        myServiceMy?.unregisterOnPomodoroListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        unbindSafely()
        myServiceMy?.unregisterOnPomodoroListener()

    }

    override fun onDetach() {
        super.onDetach()
        myServiceMy?.clientCallback = null
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        val item = menu.findItem(R.id.action_notify_sound)
        updateNotifySound(item)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_pomodoro, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_notify_sound -> {
                MyApplication.prefs.isPomodoroNotifySound = !MyApplication.prefs.isPomodoroNotifySound
                updateNotifySound(item)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!requireContext().isServiceRunning(MyService::class.java)) {
            resetDisplayUI()
        }

        binding.root.keepScreenOn = MyApplication.prefs.isPomodoroKeepScreenOn

        binding.btnStart.setOnClickListener {

            //pomodoro.start()
            //MyService.startService(requireContext(), "Foreground Service is running...")
            if (requireContext().isServiceRunning(MyService::class.java)) {
                if (pomodoro?.isRunning() == true) {
                    pomodoro?.stop()
                } else {
                    pomodoro?.start()
                }
            } else {

                requireContext().bindService(Intent(requireContext(), MyService::class.java), this, Context.BIND_AUTO_CREATE)
            }

        }

        binding.btnNext.setOnClickListener {
            pomodoro?.nextInterval()
        }

        binding.btnReset.setOnClickListener {
            pomodoro?.reset()
            myServiceMy?.stopSelf()
            unbindSafely()
        }

        /*binding.btnStatus.setOnClickListener {
            var s = "isRunning: " + pomodoro?.isRunning().toString()
            s += " interval" + pomodoro?.getInterval()
            s += " count" + pomodoro?.getFocusCount()
            binding.tvStatus.text = s
        }*/
        //mViewModel.getItemByUID("ca412c9a-7083-3185-7c25-06f805951957")

        binding.circularSeekBar.setOnSeekBarChangeListener(object : OnCircularSeekBarChangeListener {
            override fun onProgressChanged(circularSeekBar: CircularSeekBar?, progress: Float, fromUser: Boolean) {
                //if (progress > 0) circularSeekBar!!.progress = Math.floor(progress.toDouble()).toFloat()
                if (fromUser) {
                    val millis: Long = pomodoro?.setTimeByPercent(progress) ?: 1000L
                    renderTimeRemainingUI(millis, progress)
                }
            }

            override fun onStopTrackingTouch(seekBar: CircularSeekBar) {}
            override fun onStartTrackingTouch(seekBar: CircularSeekBar) {}
        })

    }

    override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
        Log.i(TAG, "Service connected.")

        val binder = service as MyService.MyLocalBinder
        myServiceMy = binder.getService()

        isBound = true
        pomodoro = myServiceMy?.pomodoro!!

        myServiceMy?.registerOnPomodoroListener(callback)


        pomodoro?.let {
            var (interval, millisTotal, millisUntilFinished) = it.getInterval()
            millisUntilFinished = millisUntilFinished ?: millisTotal
            renderDisplayUI(interval)
            renderButtonsUI(it.isRunning())
            val percent = it.getPercentFromMillis(millisTotal, millisUntilFinished)
            renderTimeRemainingUI(millisUntilFinished, percent)
        }


    }

    override fun onServiceDisconnected(className: ComponentName?) {
        Log.w(TAG, "Service disconnected.")
        isBound = false
    }

    private fun unbindSafely() {
        Log.d(TAG, "unbindSafely bound:$isBound")
        if (isBound) {
            requireContext().unbindService(this)
            isBound = false
        }
    }


    private fun renderDisplayUI(interval: PomodoroTimer.IntervalType) {
        if (_binding == null) return
        val circularSeekBar = binding.circularSeekBar
        circularSeekBar.max = 360F

        val s = when (interval) {
            PomodoroTimer.IntervalType.EMPTY -> {
                getString(R.string.pomodoro_init_interval)
            }
            PomodoroTimer.IntervalType.FOCUS -> {
                getString(R.string.pomodoro_stay_focus)
            }
            PomodoroTimer.IntervalType.SHORT_BREAK -> {
                getString(R.string.pomodoro_short_break)
            }
            PomodoroTimer.IntervalType.LONG_BREAK -> {
                getString(R.string.pomodoro_long_break)
            }
        }
        binding.tvLabel.text = s
        val pomodorosCount = pomodoro?.getFocusCount() ?: 0
        val s1 = String.format(getString(R.string.pomodoro_count), pomodorosCount, MyApplication.prefs.pomodoroLongBreakDelay)
        binding.tvStatus.text = s1
    }

    private fun renderTimeRemainingUI(millis: Long, percent: Float? = null) {
        if (_binding == null) return

        Log.d(TAG, "renderTimeRemainingUI() called with: millis = $millis, percent = $percent")
        binding.tvTimeRemaining.text = getTimeRemaining(millis)
        percent?.let {
            binding.circularSeekBar.progress = percent
        }

    }

    private fun renderButtonsUI(isRunning: Boolean, setupInit: Boolean = false) {
        if (_binding == null) return

        Log.d(TAG, "renderButtonsUI() called with: isRunning = $isRunning")
        if (isRunning) {
            binding.btnStart.setIconResource(R.drawable.ic_round_pause_24)
            binding.btnNext.isEnabled = false
            binding.btnReset.isEnabled = true
            binding.circularSeekBar.isEnabled = false

        } else {
            binding.btnStart.setIconResource(R.drawable.ic_round_play_arrow_24)
            binding.circularSeekBar.isEnabled = true
            binding.btnNext.isEnabled = true
            binding.btnReset.isEnabled = true
        }

        if (setupInit) {
            binding.btnNext.isEnabled = false
            binding.btnReset.isEnabled = false
            binding.circularSeekBar.isEnabled = false
        }
    }

    private fun getTimeRemaining(millis: Long): String {
        val minutes: Long = millis / 1000 / 60
        val seconds: Long = millis / 1000 % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun resetDisplayUI() {

        renderDisplayUI(PomodoroTimer.IntervalType.EMPTY)
        renderTimeRemainingUI(TimeUnit.MINUTES.toMillis(MyApplication.prefs.pomodoroFocusLenght.toLong()), 360F)

        renderButtonsUI(false, true)

        /*binding.btnNext.isEnabled = false
        binding.btnReset.isEnabled = false
        binding.circularSeekBar.isEnabled = false*/

    }

    private fun updateNotifySound(item: MenuItem) {
        if (MyApplication.prefs.isPomodoroNotifySound) {
            item.setIcon(R.drawable.ic_round_volume_up_24)
        } else {
            item.setIcon(R.drawable.ic_round_volume_off_24)
        }
    }


}


