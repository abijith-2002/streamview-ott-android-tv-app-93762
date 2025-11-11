package com.example.streamly_frontend

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentActivity

/**
 * HomeActivity hosts the HomeBrowseFragment which renders TV rails using Leanback components.
 * Ensures D-pad navigation and Ocean Professional theme colors via styles.
 *
 * Overscan note:
 * We keep 48dp padding on all sides in activity_home.xml to ensure content remains visible on TVs.
 */
class HomeActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Keep stable layout for TV to avoid insets flicker
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        )
        setContentView(R.layout.activity_home)
    }
}
