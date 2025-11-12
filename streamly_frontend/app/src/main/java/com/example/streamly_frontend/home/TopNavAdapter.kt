package com.example.streamly_frontend.home

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.streamly_frontend.R

/**
 * Adapter for top navigation categories.
 * Stabilizes item width to prevent shifting when focus toggles bold.
 *
 * PUBLIC_INTERFACE
 * Constants:
 * - SEARCH_ITEM: Special token to render a focusable search icon item in the top nav list.
 */
class TopNavAdapter(
    items: List<String>,
    private val onClick: (String) -> Unit
) : ListAdapter<String, TopNavAdapter.TopNavVH>(TopNavDiff) {

    companion object {
        // PUBLIC_INTERFACE
        const val SEARCH_ITEM: String = "__SEARCH__"
    }

    // Cache of precomputed max widths (normal vs bold) per label to avoid reflow on focus
    private val measuredWidthsPx = HashMap<String, Int>()

    init {
        submitList(items)
    }

    object TopNavDiff : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopNavVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_top_nav_tab, parent, false)
        return TopNavVH(view, ::measureAndCacheWidth, ::getCachedWidthFor)
    }

    override fun onBindViewHolder(holder: TopNavVH, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    private fun measureAndCacheWidth(textView: TextView, label: String) {
        if (label == SEARCH_ITEM) return
        if (measuredWidthsPx.containsKey(label)) return

        // Clone paint to measure text at same size for normal and bold
        val basePaint = Paint(textView.paint)
        basePaint.isFakeBoldText = false
        basePaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

        val normalWidth = basePaint.measureText(label)

        val boldPaint = Paint(textView.paint)
        // Use fake bold to approximate bold metrics without forcing re-layout on OEMs
        boldPaint.isFakeBoldText = true
        boldPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)

        val boldWidth = boldPaint.measureText(label)

        val paddingStart = textView.paddingStart
        val paddingEnd = textView.paddingEnd

        // Include label paddings; container margins are applied externally
        val maxPx = kotlin.math.ceil(kotlin.math.max(normalWidth, boldWidth).toDouble()).toInt() + paddingStart + paddingEnd
        measuredWidthsPx[label] = maxPx
    }

    private fun getCachedWidthFor(label: String): Int? = measuredWidthsPx[label]

    class TopNavVH(
        itemView: View,
        private val measureWidth: (TextView, String) -> Unit,
        private val getWidth: (String) -> Int?
    ) : RecyclerView.ViewHolder(itemView) {
        private val label: TextView = itemView.findViewById(R.id.top_nav_label)
        private val icon: ImageView = itemView.findViewById(R.id.top_nav_icon)

        fun bind(text: String, onClick: (String) -> Unit) {
            val isSearch = text == SEARCH_ITEM

            // Focusability for TV
            itemView.isFocusable = true
            itemView.isFocusableInTouchMode = true

            // Prepare label/icon visibility and content
            if (isSearch) {
                label.visibility = View.GONE
                icon.visibility = View.VISIBLE
                itemView.contentDescription = itemView.context.getString(R.string.top_navigation) + " búsqueda"
                // For icon item, ensure a stable min width matching text items' height-based paddings
                val lp = itemView.layoutParams as? RecyclerView.LayoutParams
                if (lp != null) {
                    // keep wrap_content; icon view already has symmetric padding to match text tabs
                    itemView.layoutParams = lp
                }
            } else {
                label.visibility = View.VISIBLE
                icon.visibility = View.GONE
                label.text = text
                label.maxLines = 1
                label.isSingleLine = true
                label.ellipsize = TextUtils.TruncateAt.END // safety, should rarely trigger
                label.includeFontPadding = false // reduce internal font padding variability
                itemView.contentDescription = text

                // Precompute and cache width (max of normal/bold)
                measureWidth(label, text)
                val cached = getWidth(text) ?: 0

                // Lock the container's width so it doesn't change on focus
                val itemLp = (itemView.layoutParams as? RecyclerView.LayoutParams)
                if (itemLp != null && cached > 0) {
                    itemLp.width = cached
                    itemView.layoutParams = itemLp
                }

                // Also lock the label width to prevent internal reflow
                if (cached > 0) {
                    label.layoutParams = label.layoutParams.apply {
                        width = cached
                        // Keep existing height
                    }
                }
            }

            // Avoid any scaling on focus to keep pill/background size stable
            itemView.setOnFocusChangeListener { v, hasFocus ->
                v.scaleX = 1.0f
                v.scaleY = 1.0f
                if (!isSearch) {
                    // Apply bold emphasis without changing size (width is locked)
                    // Use fake bold to avoid font substitution/layout shifts on OEMs
                    label.paint.isFakeBoldText = hasFocus
                    label.invalidate()
                }
            }

            // Inter-item spacing strictly via margins; ensure stable spacing regardless of focus
            val density = itemView.resources.displayMetrics.density
            val spacingPx = (12f * density).toInt()
            val params = (itemView.layoutParams as? RecyclerView.LayoutParams)
            val pos = bindingAdapterPosition
            if (params != null && pos != RecyclerView.NO_POSITION) {
                params.marginStart = if (pos == 0) 0 else spacingPx
                params.marginEnd = 0
                itemView.layoutParams = params
            }

            // Click -> callback with meaningful value
            itemView.setOnClickListener { onClick(if (isSearch) "Search" else text) }

            // Intercept DPAD navigation at edges to prevent wrapping
            itemView.setOnKeyListener { _, keyCode, event ->
                if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyListener false
                val rv = itemView.parent as? RecyclerView ?: return@setOnKeyListener false
                val adapter = rv.adapter ?: return@setOnKeyListener false
                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return@setOnKeyListener false
                val lastIndex = adapter.itemCount - 1
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> position == 0
                    KeyEvent.KEYCODE_DPAD_RIGHT -> position == lastIndex
                    else -> false
                }
            }
        }
    }
}
