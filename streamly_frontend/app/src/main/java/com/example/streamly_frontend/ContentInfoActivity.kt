package com.example.streamly_frontend

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.FragmentActivity

/**
 * PUBLIC_INTERFACE
 * ContentInfoActivity hosts the HTML Content Info screen (assets/content-info-1-539.html) in a WebView.
 *
 * Data Contract:
 * - Accepts metadata via Intent extras and forwards them through:
 *    1) URL query parameter "payload" as URL-encoded JSON.
 *    2) A postMessage bridge fired on page load via JS interface.
 *
 * Intent Extras:
 *  - EXTRA_TITLE (String)
 *  - EXTRA_SYNOPSIS (String)
 *  - EXTRA_IMAGE_URL (String)
 *  - EXTRA_TAGS (String[]) optional
 *  - EXTRA_RUNTIME (String) optional
 *  - EXTRA_ROW_INDEX (Int) rail index to restore focus on back
 *  - EXTRA_ITEM_INDEX (Int) item index within rail to restore focus on back
 *
 * Focus Restore Contract:
 * - On BACK press, the activity sets result with row/item indices so Home can restore focus.
 *
 * Manifest:
 * - No special intent filter required. Ensure INTERNET permission already exists.
 */
class ContentInfoActivity : FragmentActivity() {

    private lateinit var webView: WebView

    companion object {
        // PUBLIC_INTERFACE
        const val EXTRA_TITLE = "extra_title"
        // PUBLIC_INTERFACE
        const val EXTRA_SYNOPSIS = "extra_synopsis"
        // PUBLIC_INTERFACE
        const val EXTRA_IMAGE_URL = "extra_image_url"
        // PUBLIC_INTERFACE
        const val EXTRA_TAGS = "extra_tags"
        // PUBLIC_INTERFACE
        const val EXTRA_RUNTIME = "extra_runtime"
        // PUBLIC_INTERFACE
        const val EXTRA_ROW_INDEX = "extra_row_index"
        // PUBLIC_INTERFACE
        const val EXTRA_ITEM_INDEX = "extra_item_index"

        // PUBLIC_INTERFACE
        fun buildIntent(
            ctx: Context,
            title: String?,
            synopsis: String?,
            imageUrl: String?,
            tags: ArrayList<String>?,
            runtime: String?,
            rowIndex: Int,
            itemIndex: Int
        ): Intent {
            val intent = Intent(ctx, ContentInfoActivity::class.java)
            intent.putExtra(EXTRA_TITLE, title)
            intent.putExtra(EXTRA_SYNOPSIS, synopsis)
            intent.putExtra(EXTRA_IMAGE_URL, imageUrl)
            if (tags != null) intent.putStringArrayListExtra(EXTRA_TAGS, tags)
            if (!runtime.isNullOrBlank()) intent.putExtra(EXTRA_RUNTIME, runtime)
            intent.putExtra(EXTRA_ROW_INDEX, rowIndex)
            intent.putExtra(EXTRA_ITEM_INDEX, itemIndex)
            return intent
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        // Use app theme
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        webView.setBackgroundColor(Color.BLACK)
        setContentView(webView)

        val title = intent.getStringExtra(EXTRA_TITLE)
        val synopsis = intent.getStringExtra(EXTRA_SYNOPSIS)
        val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)
        val tags = intent.getStringArrayListExtra(EXTRA_TAGS) ?: arrayListOf()
        val runtime = intent.getStringExtra(EXTRA_RUNTIME)

        val payloadJson = buildPayloadJson(title, synopsis, imageUrl, tags, runtime)

        configureWebView(payloadJson)

        // Load local asset with payload via query parameter
        // Note: the JS will also receive the payload via bridge.
        val encoded = Uri.encode(payloadJson)
        webView.loadUrl("file:///android_asset/content-info-1-539.html?payload=$encoded")
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                // Let WebView handle ENTER clicks on focused buttons
                false
            }
            KeyEvent.KEYCODE_BACK -> {
                deliverFocusRestoreResult()
                finish()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    private fun deliverFocusRestoreResult() {
        val rowIndex = intent.getIntExtra(EXTRA_ROW_INDEX, -1)
        val itemIndex = intent.getIntExtra(EXTRA_ITEM_INDEX, -1)
        val result = Intent().apply {
            putExtra(EXTRA_ROW_INDEX, rowIndex)
            putExtra(EXTRA_ITEM_INDEX, itemIndex)
        }
        setResult(RESULT_OK, result)
    }

    @SuppressLint("AddJavascriptInterface")
    private fun configureWebView(payloadJson: String) {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            mediaPlaybackRequiresUserGesture = false
            cacheMode = WebSettings.LOAD_DEFAULT
            setSupportZoom(false)
            useWideViewPort = true
            loadWithOverviewMode = true
        }
        // Avoid external navigation
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return url?.startsWith("file:///android_asset/")?.not() == true
            }
        }
        webView.webChromeClient = WebChromeClient()

        // Bridge to send payload after DOMContentLoaded
        webView.addJavascriptInterface(object {
            // PUBLIC_INTERFACE
            @JavascriptInterface
            fun getInitialPayload(): String {
                return payloadJson
            }
        }, "AndroidBridge")
    }

    private fun buildPayloadJson(
        title: String?,
        synopsis: String?,
        imageUrl: String?,
        tags: List<String>?,
        runtime: String?
    ): String {
        // Minimal escaping for quotes/newlines
        fun esc(s: String?): String {
            if (s == null) return ""
            return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
        }
        val safeTitle = esc(title)
        val safeSynopsis = esc(synopsis)
        val safeImage = esc(imageUrl)
        val tagsJson = (tags ?: emptyList()).joinToString(
            prefix = "[", postfix = "]"
        ) { "\"${esc(it)}\"" }
        val safeRuntime = esc(runtime)

        return """{
  "title":"$safeTitle",
  "synopsis":"$safeSynopsis",
  "image":"$safeImage",
  "tags":$tagsJson,
  "runtime":"$safeRuntime"
}""".trimIndent()
    }
}
