package com.example.streamly_frontend.ui.home

import android.os.Bundle
import androidx.fragment.app.FragmentActivity

// PUBLIC_INTERFACE
class HomeActivity : FragmentActivity() {
    /**
     * Native entry activity for the Home experience on Android TV.
     *
     * Replaces the previous WebView-based Home. Hosts HomeFragment (Leanback/BrowseSupportFragment).
     *
     * Navigation:
     * - From SplashActivity -> HomeActivity
     * - HomeFragment handles row/item selection to details/playback screens.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.streamly_frontend.R.layout.activity_home)
        // Fragment is attached via XML.
    }
}
