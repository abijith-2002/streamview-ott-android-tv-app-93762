package com.example.streamly_frontend

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.FragmentActivity

/**
 * PUBLIC_INTERFACE
 * SplashActivity is the Android TV entry point that displays a minimalist splash
 * screen with the text "Streamly" centered, following the Ocean Professional theme.
 *
 * Behavior:
 * - Shows splash for approximately 3 seconds without blocking the UI thread.
 * - After delay, navigates to MainActivity (home screen).
 *
 * No D-pad focus is required on the splash screen.
 */
class SplashActivity : FragmentActivity() {

    private val splashDurationMs: Long = 3000L
    private val handler = Handler(Looper.getMainLooper())
    private var didNavigate = false

    private val navigateRunnable = Runnable {
        if (!didNavigate && !isFinishing) {
            didNavigate = true
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            // Clear splash from back stack to avoid going back to it
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply splash theme to avoid layout flicker
        setTheme(R.style.SplashTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        // Post a delayed navigation without blocking the UI thread
        handler.postDelayed(navigateRunnable, splashDurationMs)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(navigateRunnable)
    }
}
