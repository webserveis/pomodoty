package com.webserveis.app.ratethisapp

import android.content.*
import android.content.SharedPreferences.Editor
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.TimeUnit


class RateThisApp(val context: Context) {
    companion object {
        private val TAG = RateThisApp::class.java.simpleName

        private const val PREF_NAME = "RateThisApp"
        private const val KEY_INSTALL_DATE = "rta_install_date"
        private const val KEY_LAUNCH_TIMES = "rta_launch_times"
        private const val KEY_OPT_OUT = "rta_opt_out"
        private const val KEY_ASK_LATER_DATE = "rta_ask_later_date"
    }

    private var mInstallDate: Date = Date()
    private var mLaunchTimes = 0
    private var mOptOut = false
    private var mAskLaterDate: Date = Date()

    private var sBuilder: Builder = Builder()
    private var sCallback: Callback? = null

    // Weak ref to avoid leaking the context
    private var sDialogRef: WeakReference<AlertDialog>? = null

    private val MYDEBUG = false

    fun init(builder: Builder) {
        sBuilder = builder
    }


    /**
     * Set callback instance.
     * The callback will receive yes/no/later events.
     * @param callback
     */
    fun setCallback(callback: Callback) {
        sCallback = callback
    }

    /**
     * Call this API when the launcher activity is launched.<br></br>
     * It is better to call this API in onCreate() of the launcher activity.
     */
    init {
        val pref: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = pref.edit()
        // If it is the first launch, save the date in shared preference.
        if (pref.getLong(KEY_INSTALL_DATE, 0) == 0L) {
            storeInstallDate(editor)
        }
        // Increment launch times
        var launchTimes = pref.getInt(KEY_LAUNCH_TIMES, 0)
        launchTimes++
        editor.putInt(KEY_LAUNCH_TIMES, launchTimes)

        log("Launch times; $launchTimes")
        editor.apply()
        mInstallDate = Date(pref.getLong(KEY_INSTALL_DATE, 0))
        mLaunchTimes = pref.getInt(KEY_LAUNCH_TIMES, 0)
        mOptOut = pref.getBoolean(KEY_OPT_OUT, false)
        mAskLaterDate = Date(pref.getLong(KEY_ASK_LATER_DATE, 0))
        printStatus()
    }

    /**
     * Show the rate dialog if the criteria is satisfied.
     * @return true if shown, false otherwise.
     */
    fun showRateDialogIfNeeded(): Boolean {
        return if (shouldShowRateDialog()) {
            showRateDialog()
            true
        } else {
            false
        }
    }

    /**
     * Show the rate dialog if the criteria is satisfied.
     * @param themeId Theme ID
     * @return true if shown, false otherwise.
     */
    fun showRateDialogIfNeeded(themeId: Int): Boolean {
        return if (shouldShowRateDialog()) {
            showRateDialog(themeId)
            true
        } else {
            false
        }
    }

    /**
     * Check whether the rate dialog should be shown or not.
     * Developers may call this method directly if they want to show their own view instead of
     * dialog provided by this library.
     * @return
     */
    private fun shouldShowRateDialog(): Boolean {
        return if (mOptOut) {
            false
        } else {
            if (mLaunchTimes >= sBuilder.mCriteriaLaunchTimes) {
                return true
            }
            val threshold: Long = TimeUnit.DAYS.toMillis(sBuilder.mCriteriaInstallDays.toLong()) // msec
            Date().time - mInstallDate.time >= threshold &&
                    Date().time - mAskLaterDate.time >= threshold
        }
    }

    /**
     * Show the rate dialog
     */
    private fun showRateDialog() {
        val builder = MaterialAlertDialogBuilder(context)
        showRateDialog(builder)
    }

    /**
     * Show the rate dialog
     * @param themeId
     */
    private fun showRateDialog(themeId: Int) {
        val builder = MaterialAlertDialogBuilder(context, themeId)
        showRateDialog(builder)
    }

    /**
     * Stop showing the rate dialog
     */
    fun stopRateDialog() {
        setOptOut(true)
    }

    /**
     * Get count number of the rate dialog launches
     * @return
     */
    fun getLaunchCount(): Int {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pref.getInt(KEY_LAUNCH_TIMES, 0)
    }

    private fun showRateDialog(builder: MaterialAlertDialogBuilder) {
        if (sDialogRef != null && sDialogRef?.get() != null) {
            // Dialog is already present
            return
        }
        val titleId = if (sBuilder.mTitleId != 0) sBuilder.mTitleId else R.string.rta_dialog_title
        val messageId = if (sBuilder.mMessageId != 0) sBuilder.mMessageId else R.string.rta_dialog_message
        val cancelButtonID = if (sBuilder.mCancelButton != 0) sBuilder.mCancelButton else R.string.rta_dialog_cancel
        val thanksButtonID = if (sBuilder.mNoButtonId != 0) sBuilder.mNoButtonId else R.string.rta_dialog_no
        val rateButtonID = if (sBuilder.mYesButtonId != 0) sBuilder.mYesButtonId else R.string.rta_dialog_ok
        builder.setTitle(titleId)
        builder.setMessage(messageId)
        when (sBuilder.mCancelMode) {
            Builder.CANCEL_MODE_BACK_KEY_OR_TOUCH_OUTSIDE -> builder.setCancelable(true) // It's the default anyway
            Builder.CANCEL_MODE_BACK_KEY -> {
                builder.setCancelable(false)
                builder.setOnKeyListener { dialog, keyCode, _ ->
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        dialog.cancel()
                        true
                    } else {
                        false
                    }
                }
            }
            Builder.CANCEL_MODE_NONE -> builder.setCancelable(false)
        }
        builder.setPositiveButton(rateButtonID) { _, _ ->
            if (sCallback != null) {
                sCallback!!.onRateNowClicked()
            }
            val appPackage = context.packageName
            var url: String? = "market://details?id=$appPackage"
            if (!TextUtils.isEmpty(sBuilder.mUrl)) {
                url = sBuilder.mUrl
            }
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (anfe: ActivityNotFoundException) {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + context.packageName)))
            }
            setOptOut(true)
        }
        builder.setNeutralButton(cancelButtonID) { _, _ ->
            if (sCallback != null) {
                sCallback!!.onLaterClicked()
            }
            clearSharedPreferences()
            storeAskLaterDate()
        }
        builder.setNegativeButton(thanksButtonID) { _, _ ->
            if (sCallback != null) {
                sCallback!!.onNoThanksClicked()
            }
            setOptOut(true)
        }
        builder.setOnCancelListener {
            if (sCallback != null) {
                sCallback!!.onLaterClicked()
            }
            clearSharedPreferences()
            storeAskLaterDate()
        }
        builder.setOnDismissListener { sDialogRef?.clear() }
        sDialogRef = WeakReference(builder.show())
    }


    /**
     * Clear data in shared preferences.<br></br>
     * This API is called when the "Later" is pressed or canceled.
     */
    private fun clearSharedPreferences() {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.remove(KEY_INSTALL_DATE)
        editor.remove(KEY_LAUNCH_TIMES)
        editor.apply()
    }

    /**
     * Set opt out flag.
     * If it is true, the rate dialog will never shown unless app data is cleared.
     * This method is called when Yes or No is pressed.
     * @param optOut
     */
    private fun setOptOut(optOut: Boolean) {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putBoolean(KEY_OPT_OUT, optOut)
        editor.apply()
        mOptOut = optOut
    }

    /**
     * Store install date.
     * Install date is retrieved from package manager if possible.
     * @param editor
     */
    private fun storeInstallDate(editor: Editor) {
        var installDate = Date()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            val packMan = context.packageManager
            try {
                val pkgInfo = packMan.getPackageInfo(context.packageName, 0)
                installDate = Date(pkgInfo.firstInstallTime)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
        }
        editor.putLong(KEY_INSTALL_DATE, installDate.time)
        log("First install: $installDate")
    }

    /**
     * Store the date the user asked for being asked again later.
     */
    private fun storeAskLaterDate() {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putLong(KEY_ASK_LATER_DATE, System.currentTimeMillis())
        editor.apply()
    }

    /**
     * Print values in SharedPreferences (used for debug)
     */
    private fun printStatus() {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        log("*** RateThisApp Status ***")
        log("Install Date: " + Date(pref.getLong(KEY_INSTALL_DATE, 0)))
        log("Launch Times: " + pref.getInt(KEY_LAUNCH_TIMES, 0))
        log("Opt out: " + pref.getBoolean(KEY_OPT_OUT, false))
    }

    /**
     * Print log if enabled
     */
    private fun log(message: String) {
        if (MYDEBUG) {
            Log.v(TAG, message)
        }
    }

    class Builder(internal val mCriteriaInstallDays: Int = 7, internal val mCriteriaLaunchTimes: Int = 10) {

        companion object {
            const val CANCEL_MODE_BACK_KEY_OR_TOUCH_OUTSIDE = 0
            const val CANCEL_MODE_BACK_KEY = 1
            const val CANCEL_MODE_NONE = 2
        }

        internal var mUrl: String? = null
        internal var mTitleId = 0
        internal var mMessageId = 0
        internal var mYesButtonId = 0
        internal var mNoButtonId = 0
        internal var mCancelButton = 0
        internal var mCancelMode = CANCEL_MODE_BACK_KEY_OR_TOUCH_OUTSIDE

        /**
         * Set title string ID.
         * @param stringId
         */
        fun setTitle(@StringRes stringId: Int) {
            mTitleId = stringId
        }

        /**
         * Set message string ID.
         * @param stringId
         */
        fun setMessage(@StringRes stringId: Int) {
            mMessageId = stringId
        }

        /**
         * Set rate now string ID.
         * @param stringId
         */
        fun setYesButtonText(@StringRes stringId: Int) {
            mYesButtonId = stringId
        }

        /**
         * Set no thanks string ID.
         * @param stringId
         */
        fun setNoButtonText(@StringRes stringId: Int) {
            mNoButtonId = stringId
        }

        /**
         * Set cancel string ID.
         * @param stringId
         */
        fun setCancelButtonText(@StringRes stringId: Int) {
            mCancelButton = stringId
        }

        /**
         * Set navigation url when user clicks rate button.
         * Typically, url will be https://play.google.com/store/apps/details?id=PACKAGE_NAME for Google Play.
         * @param url
         */
        fun setUrl(url: String?) {
            mUrl = url
        }

        /**
         * Set the cancel mode; namely, which ways the user can cancel the dialog.
         * @param cancelMode
         */
        fun setCancelMode(cancelMode: Int) {
            mCancelMode = cancelMode
        }

    }


    interface Callback {
        /**
         * "Rate now" event
         */
        fun onRateNowClicked()

        /**
         * "No, thanks" event
         */
        fun onNoThanksClicked()

        /**
         * "Later" event
         */
        fun onLaterClicked()
    }
}