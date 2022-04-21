package com.webserveis.app.pomodoty.views

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import com.webserveis.app.pomodoty.R
import java.util.*

/*
https://www.section.io/engineering-education/safe-args-in-android/
https://proandroiddev.com/android-fragments-fragment-result-805a6b2522ea
https://www.tutorialsbuzz.com/2019/09/android-datepicker-dialog-styling-kotlin.html
 */
class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    companion object {
        const val REQUEST_KEY = "DIALOG_DATE_REQUEST_KEY"
        const val DEFAULT_ARG = "DIALOG_DATE_DEFAULT_ARG"

        fun newInstance(requestKey: String, date: Date? = null): DatePickerFragment {
            val dialog = DatePickerFragment()

            val args = Bundle()
            args.putLong(DEFAULT_ARG, date?.time ?: Calendar.getInstance().timeInMillis)
            args.putString(REQUEST_KEY, requestKey)

            dialog.arguments = args

            return dialog

        }

    }

    interface OnDatePickerListener {
        fun onDateSelected(requestKey: String, date: Date)
    }

    private var callback: OnDatePickerListener? = null

    fun setOnDatePickerListener(callback: OnDatePickerListener) {
        this.callback = callback
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val args = requireArguments()
        val cal = Calendar.getInstance()
        val date = args.getLong(DEFAULT_ARG)
        cal.timeInMillis = date
        val (year, month, day) = Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        // Create a new instance of DatePickerDialog and return it

        return DatePickerDialog(
            requireActivity(),
            R.style.CalendarDatePickerDialog,
            this,
            year, month, day
        )
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {

        callback?.let {
            val requestKey = arguments?.getString(REQUEST_KEY) ?: REQUEST_KEY
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val date = cal.time

            it.onDateSelected(requestKey, date)
        }
    }

}