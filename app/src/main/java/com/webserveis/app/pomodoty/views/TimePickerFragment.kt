package com.webserveis.app.pomodoty.views

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import com.webserveis.app.pomodoty.R
import java.util.*

/*
https://www.section.io/engineering-education/safe-args-in-android/
https://proandroiddev.com/android-fragments-fragment-result-805a6b2522ea
import android.text.format.DateFormat
DateFormat.is24HourFormat
https://www.tutorialsbuzz.com/2019/09/android-timepicker-dialog-styling.html
 */
class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    companion object {
        const val REQUEST_KEY = "DIALOG_TIME_REQUEST_KEY"
        const val DEFAULT_ARG = "DIALOG_TIME_DEFAULT_ARG"
        const val IS_24H_FORMAT = "DIALOG_TIME_24H"

        fun newInstance(requestKey: String, date: Date? = null, is24hFormat: Boolean = true): TimePickerFragment {
            val dialog = TimePickerFragment()

            val args = Bundle()
            args.putLong(DEFAULT_ARG, date?.time ?: Calendar.getInstance().timeInMillis)
            args.putString(REQUEST_KEY, requestKey)
            args.putBoolean(IS_24H_FORMAT, is24hFormat)

            dialog.arguments = args

            return dialog

        }

    }

    interface OnTimePickerListener {
        fun onTimeSelected(requestKey: String, date: Date)
    }

    private var callback: OnTimePickerListener? = null

    fun setOnTimeSetListener(callback: OnTimePickerListener) {
        this.callback = callback
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val args = requireArguments()
        val cal = Calendar.getInstance()
        cal.timeZone = TimeZone.getTimeZone("UTC")
        val date = args.getLong(DEFAULT_ARG)
        cal.timeInMillis = date
        val (hours, minutes) = Pair(cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE))
        // Create a new instance of TimePickerDialog and return it

        return TimePickerDialog(
            requireActivity(),
            R.style.ClockTimePickerDialog,
            this,
            hours, minutes, args.getBoolean(IS_24H_FORMAT)
        )
    }

    override fun onTimeSet(timePicker: TimePicker?, selectedHour: Int, selectedMinute: Int) {
        callback?.let {
            val requestKey = arguments?.getString(TimePickerFragment.REQUEST_KEY) ?: TimePickerFragment.REQUEST_KEY
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR, selectedHour)
            cal.set(Calendar.MINUTE, selectedMinute)
            val date = cal.time

            it.onTimeSelected(requestKey, date)
        }

    }

}