package com.example.streamly_frontend

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.FragmentActivity

/**
 * PUBLIC_INTERFACE
 * SplashActivity
 * A minimalist splash screen for Android TV showing "Streamly" centered on a plain background.
 *
 * Behavior:
 * - Displays for approximately 3 seconds.
 * - Navigates to MainActivity and finishes to prevent back navigation returning to splash.
 *
 * TV-specific notes:
 * - Uses FragmentActivity (Leanback-friendly).
 * - Locked to landscape via manifest.
 */
class SplashActivity : FragmentActivity() {

    private val splashDurationMs: Long = 3000L
    private val handler = Handler(Looper.getMainLooper())
    private val navigateRunnable = Runnable {
        // Navigate to the main/home screen and finish Splash to avoid back returning here
        val intent = Intent(this@SplashActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set simple layout with centered "Streamly" text
        setContentView(R.layout.activity_splash)
        // Post delayed navigation
        handler.postDelayed(navigateRunnable, splashDurationMs)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up posted callbacks to avoid leaks
        handler.removeCallbacks(navigateRunnable)
    }
}
