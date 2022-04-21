package com.webserveis.app.aboutscreen

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class WebViewDialog : DialogFragment() {

    companion object {
        private val TAG = WebViewDialog::class.java.simpleName
        const val ARG_TITLE = "ARG_TITLE"
        const val ARG_URL = "ARG_KEY_FILE"

        @JvmStatic
        fun newInstance(title: String, url: String): WebViewDialog {
            val dialog = WebViewDialog()

            val args = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_URL, url)
            }

            dialog.arguments = args
            return dialog

        }

    }

/*    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_licenses, container, false)
    }*/

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return activity?.let {

            val args = requireArguments()
            val title = args.getString(ARG_TITLE)
            val urlStr = args.getString(ARG_URL, "")

            val dialogView = LayoutInflater.from(it).inflate(R.layout.dialog_licenses, null)

            val progressBar = dialogView.findViewById<ProgressBar>(R.id.progress)
            val webView = dialogView.findViewById<WebView>(R.id.web_view)
            webView.settings.setSupportMultipleWindows(true)
            webView.webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, newProgress: Int) {

                    // Keep WebView hidden when the url is loading
                    webView.visibility = View.INVISIBLE

                    // Make ProgressBar visible
                    progressBar.visibility = View.VISIBLE
                    progressBar.progress = 0
                    progressBar.incrementProgressBy(newProgress)
                    if (newProgress == 100 && progressBar.visibility == View.VISIBLE) {
                        // Make ProgressBar invisible and WebView visible
                        progressBar.visibility = View.INVISIBLE
                        webView.visibility = View.VISIBLE
                    }
                }

                override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
                    view?.let { wv ->
                        val result = wv.hitTestResult
                        val data = result.extra
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(data))
                        if (browserIntent.resolveActivity(requireContext().packageManager) != null) {
                            requireContext().startActivity(browserIntent)
                        } else {
                            val href = wv.handler.obtainMessage()
                            wv.requestFocusNodeHref(href)
                            val url = href.data?.getString("url")
                            Log.w(TAG, "Unknown type url($url)")
                        }
                    }
                    return false
                    //return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
                }
            }

            //"file:///android_asset/open_source_licenses.html"
            webView.loadUrl(Uri.parse(urlStr).toString())
            webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    view.loadUrl(url)
                    return true
                }


            }


            val dialog = MaterialAlertDialogBuilder(requireActivity())
                .setTitle(title)
                .setView(dialogView)
                .setNeutralButton(" ") { _dialog, _ ->
                    AboutUtils.openUrlWebBrowser(requireContext(), urlStr)
                    _dialog?.dismiss()
                }
                .setPositiveButton(android.R.string.ok) { _dialog, _ -> _dialog?.dismiss() }

            return dialog.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }


    override fun onStart() {
        super.onStart()
        //(dialog as AlertDialog).window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        (dialog as AlertDialog).getButton(AlertDialog.BUTTON_NEUTRAL).apply {
            this.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_open_in_new_24), null, null, null
            )

        }

    }

}