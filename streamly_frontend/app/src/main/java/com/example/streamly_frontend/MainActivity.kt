package com.example.streamly_frontend

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.FragmentActivity

/**
 * PUBLIC_INTERFACE
 * MainActivity (Home)
 * Hosts the generated Home Page (assets/home-page-1-2.html) inside a TV-optimized WebView.
 *
 * Behavior:
 * - Loads /assets/home-page-1-2.html as the default Home screen.
 * - Ensures local assets (/assets/*.css, *.js, figmaimages) resolve via the Android assets folder.
 * - Preserves D-pad remote behavior: DPAD_CENTER and ENTER trigger click on focused actionable elements.
 * - Exposes a "Home" navigation via loadHome() which (re)loads the Home screen.
 *
 * Note:
 * - This uses FragmentActivity per Leanback guidance.
 * - All focusable elements within the HTML have tabindex/role and CSS focus outlines already defined.
 */
class MainActivity : FragmentActivity() {

    private lateinit var webView: WebView

    // PUBLIC_INTERFACE
    fun loadHome() {
        /** Reloads the Home screen route. */
        webView.loadUrl("file:///android_asset/home-page-1-2.html")
    } // end loadHome

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.homeWebView)

        // Configure WebView for TV: local assets, JS enabled for scaling script, no zoom UI.
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            mediaPlaybackRequiresUserGesture = true
            builtInZoomControls = false
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
            allowFileAccess = true
            allowContentAccess = true
        }

        // Serve files from the app's assets folder; keep navigation within WebView for hash links
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }
        }

        // Improve D-pad handling: ensure WebView takes focus
        webView.isFocusable = true
        webView.isFocusableInTouchMode = true
        webView.requestFocus(View.FOCUS_FORWARD)

        // Load the Home screen
        loadHome()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Map TV remote "OK" to invoke click on the focused element inside the WebView
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                webView.dispatchKeyEvent(event)
                true
            }
            KeyEvent.KEYCODE_BACK -> {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}
