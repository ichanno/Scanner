package com.dri.scanner.ui.webview

import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class CustomWebViewClient(private val activity: AppCompatActivity) : WebViewClient() {
    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        val url = request?.url.toString()

        if (url.startsWith("http://") || url.startsWith("https://")) {
            // Load regular http or https URLs in the WebView
            return false
        } else {
            // Handle other URL schemes by opening them in an external browser
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            activity.startActivity(intent)
            return true
        }
    }

    override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            // Load regular http or https URLs in the WebView
            return false
        } else {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            activity.startActivity(intent)
            return true
        }
    }
}
