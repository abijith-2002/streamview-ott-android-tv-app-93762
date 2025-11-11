package com.example.streamly_frontend

import android.os.Bundle
import androidx.fragment.app.FragmentActivity

/**
 * HomeActivity hosts the HomeBrowseFragment which renders TV rails using Leanback components.
 * Ensures D-pad navigation and Ocean Professional theme colors via styles.
 */
class HomeActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
    }
}
