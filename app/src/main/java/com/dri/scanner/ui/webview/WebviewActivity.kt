package com.dri.scanner.ui.webview

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dri.scanner.R
import com.dri.scanner.ui.main.MainActivity


class WebviewActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_URL = "extra_url"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        val webView = findViewById<WebView>(R.id.webView)
        webView.settings.javaScriptEnabled = true

//        val customWebViewClient = CustomWebViewClient(this)
//        webView.webViewClient = customWebViewClient

//        webView.clearCache(true)
//        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
//        WebView.setWebContentsDebuggingEnabled(true)

        val url = intent.getStringExtra(EXTRA_URL)

        if (url != null) {

            webView.loadUrl(url)

            webView.webViewClient = object : WebViewClient() {

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    // Check if the request is for the main frame (top-level page)
                    if (request?.isForMainFrame == true) {
                        // Handle the error for the main frame (top-level page)

                        // You can log the error details to Logcat
                        Log.e("WebViewError", "Error loading page: ${request.url}")
                        Log.e("WebViewError", "Error description: ${error?.description}")
                        Log.e("WebViewError", "Error code: ${error?.errorCode}")

                        // Display an error message to the user
                        val errorMessage =
                            "Failed to load the webpage. Please check your internet connection."
                        // Show the error message using a Toast or some other UI element
                        Toast.makeText(view?.context, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            }

            webView.webChromeClient = object : WebChromeClient() {
                override fun onJsAlert(
                    view: WebView,
                    url: String,
                    message: String,
                    result: android.webkit.JsResult
                ): Boolean {
                    Toast.makeText(this@WebviewActivity, message, Toast.LENGTH_LONG).show()
                    result.confirm()
                    return true
                }
            }

        } else {
            // Handle the case where there is no URL provided
            // You can show an error message or load a default URL
        }

    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        super.onBackPressed()

        // Launch the MainActivity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)

        // Finish the current activity
        finish()
    }

}