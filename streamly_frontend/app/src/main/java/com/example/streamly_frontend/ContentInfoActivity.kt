package com.example.streamly_frontend

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide

/**
 * PUBLIC_INTERFACE
 * ContentInfoActivity shows a native Android TV Content Info screen (no WebView).
 *
 * Intent Extras:
 *  - EXTRA_TITLE (String)
 *  - EXTRA_SYNOPSIS (String)
 *  - EXTRA_IMAGE_URL (String)
 *  - EXTRA_TAGS (ArrayList<String>) optional
 *  - EXTRA_RUNTIME (String) optional
 *  - EXTRA_ROW_INDEX (Int) rail index to restore focus on back
 *  - EXTRA_ITEM_INDEX (Int) item index within rail to restore focus on back
 *
 * Focus Restore Contract:
 * - On BACK press, the activity sets result with row/item indices so Home can restore focus.
 */
class ContentInfoActivity : FragmentActivity() {

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

    private lateinit var backdrop: ImageView
    private lateinit var titleText: TextView
    private lateinit var metadataText: TextView
    private lateinit var synopsisText: TextView
    private lateinit var actionPlay: View
    private lateinit var actionRecord: View
    private lateinit var clockText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Native fixed canvas layout (scaled 0.7x of original Figma)
        setContentView(R.layout.activity_content_info_native)

        backdrop = findViewById(R.id.info_backdrop)
        titleText = findViewById(R.id.info_title)
        metadataText = findViewById(R.id.info_metadata)
        synopsisText = findViewById(R.id.info_synopsis)
        actionPlay = findViewById(R.id.btn_play)
        actionRecord = findViewById(R.id.btn_record)
        clockText = findViewById(R.id.info_clock)

        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        val synopsis = intent.getStringExtra(EXTRA_SYNOPSIS) ?: ""
        val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL) ?: ""
        val tags = intent.getStringArrayListExtra(EXTRA_TAGS) ?: arrayListOf()
        val runtime = intent.getStringExtra(EXTRA_RUNTIME)

        titleText.text = title
        val chips = tags.joinToString(" • ")
        val runtimePart = if (!runtime.isNullOrBlank()) " • $runtime" else ""
        metadataText.text = (chips + runtimePart).trim()

        synopsisText.text = synopsis
        synopsisText.ellipsize = TextUtils.TruncateAt.END

        Glide.with(this)
            .load(imageUrl)
            .centerCrop()
            .placeholder(R.drawable.placeholder_poster)
            .into(backdrop)

        // DPAD focus defaults to Play button
        actionPlay.requestFocus()

        // Ensure proper focus movement
        actionPlay.nextFocusRightId = R.id.btn_record
        actionRecord.nextFocusLeftId = R.id.btn_play

        // Basic click handlers
        actionPlay.setOnClickListener {
            // In real app this would start playback; for now, just finish while preserving focus restore.
            deliverFocusRestoreResult()
            finish()
        }
        actionRecord.setOnClickListener {
            // Stub for record action
            it.isSelected = !it.isSelected
        }

        // Update a simple clock (HH:MM). For demo, set once; production may use a timer.
        val time = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        clockText.text = time
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
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
}
