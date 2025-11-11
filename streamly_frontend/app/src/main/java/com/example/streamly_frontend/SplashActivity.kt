package com.example.streamly_frontend

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * SplashActivity shows the Streamly brand for ~3 seconds and navigates to HomeActivity.
 * TV-friendly: landscape, no user interaction; finishes to prevent back navigation to Splash.
 */
class SplashActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use a very lightweight view for splash to avoid layout inflation overhead
        setContentView(R.layout.activity_splash)

        // Delay for 3 seconds using lifecycleScope to avoid leaks
        lifecycleScope.launch {
            delay(3000L)
            // Navigate to HomeActivity and finish this activity so back won't return here
            startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
            finish()
        }
    }
}
