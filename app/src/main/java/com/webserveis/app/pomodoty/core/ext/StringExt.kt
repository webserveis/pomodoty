package com.webserveis.app.pomodoty.core.ext

import android.text.Spanned
import androidx.core.text.HtmlCompat

fun String?.toHtml(): Spanned? {
    if (this.isNullOrEmpty()) return null
    return HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_COMPACT)
}
