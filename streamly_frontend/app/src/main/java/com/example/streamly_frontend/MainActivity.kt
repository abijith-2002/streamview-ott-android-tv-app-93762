package com.example.streamly_frontend

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.streamly_frontend.home.HomeFragment

/**
 * Main Activity for Android TV
 * Hosts the HomeFragment as the native TV home screen.
 */
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use an empty container activity layout that hosts HomeFragment
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_container, HomeFragment())
                .commitNow()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER -> {
                // Let focused view handle enter (e.g., cards)
                false
            }
            KeyEvent.KEYCODE_BACK -> {
                finish()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 9001 && resultCode == RESULT_OK && data != null) {
            val rowIndex = data.getIntExtra(com.example.streamly_frontend.ContentInfoActivity.EXTRA_ROW_INDEX, -1)
            val itemIndex = data.getIntExtra(com.example.streamly_frontend.ContentInfoActivity.EXTRA_ITEM_INDEX, -1)
            restoreFocus(rowIndex, itemIndex)
        }
    }

    private fun restoreFocus(rowIndex: Int, itemIndex: Int) {
        val frag = supportFragmentManager.findFragmentById(R.id.main_container) as? HomeFragment ?: return
        val root = findViewById<View>(R.id.rails_recycler) as? RecyclerView ?: return
        // Scroll to target section (header=0, banner=1, first row=2)
        if (rowIndex >= 0) {
            val targetPos = rowIndex
            root.post {
                root.scrollToPosition(targetPos)
                root.post {
                    val vh = root.findViewHolderForAdapterPosition(targetPos) as? com.example.streamly_frontend.home.HomeSectionsAdapter.ContentRowSectionVH
                    val inner = vh?.itemView?.findViewById<RecyclerView>(com.example.streamly_frontend.R.id.row_inner_recycler)
                    if (inner != null && itemIndex >= 0) {
                        inner.scrollToPosition(itemIndex)
                        inner.post {
                            inner.findViewHolderForAdapterPosition(itemIndex)?.itemView?.requestFocus()
                        }
                    } else {
                        vh?.itemView?.requestFocus()
                    }
                }
            }
        }
    }
}
