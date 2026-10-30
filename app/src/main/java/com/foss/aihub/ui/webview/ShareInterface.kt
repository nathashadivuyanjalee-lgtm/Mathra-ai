package com.foss.aihub.ui.webview

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface

class ShareInterface(private val context: Context) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun share(title: String, text: String, url: String) {
        mainHandler.post {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TITLE, title)
                putExtra(Intent.EXTRA_TEXT, buildShareText(title, text, url))
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share via"))
        }
    }

    private fun buildShareText(title: String, text: String, url: String): String {
        val parts = mutableListOf<String>()
        if (title.isNotEmpty()) parts.add(title)
        if (text.isNotEmpty()) parts.add(text)
        if (url.isNotEmpty()) parts.add(url)
        return parts.joinToString("\n\n")
    }
}