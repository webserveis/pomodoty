package com.webserveis.app.pomodoty.ui.pomotimer

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.webserveis.app.pomodoty.R

class MaterialAlertDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {


        val dialog = MaterialAlertDialogBuilder(
            requireActivity()
        ).apply {
            setTitle(getText(R.string.pomodoro_running_title))
            setMessage(getText(R.string.pomodoro_running_summary))
            setIcon(R.drawable.ic_outline_timer_24)
                .setPositiveButton(android.R.string.ok,
                    DialogInterface.OnClickListener { dialog, which ->
                        //botón OK pulsado
                    })
            setOnKeyListener { _, keyCode, keyEvent ->
                if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_UP) {
                    dismiss()
                    true
                } else false
            }

        }.create()

        return dialog
    }

}