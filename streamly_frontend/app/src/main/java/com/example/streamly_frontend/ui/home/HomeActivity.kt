package com.example.streamly_frontend.ui.home

import android.os.Bundle
import androidx.fragment.app.FragmentActivity

// PUBLIC_INTERFACE
class HomeActivity : FragmentActivity() {
    /**
     * Native entry activity for the Home experience on Android TV.
     *
     * Hosts HomeFragment (Leanback/BrowseSupportFragment) programmatically to control timing
     * and avoid TitleView nulls during inflation.
     *
     * Navigation:
     * - From SplashActivity -> HomeActivity
     * - HomeFragment handles row/item selection to details/playback screens.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.streamly_frontend.R.layout.activity_home)

        if (supportFragmentManager.findFragmentByTag("home_fragment") == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    com.example.streamly_frontend.R.id.home_root,
                    HomeFragment(),
                    "home_fragment"
                )
                .commitNow() // ensure fragment view is created synchronously for Leanback setup
        }
    }
}
