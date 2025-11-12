package com.example.streamly_frontend.home

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
 * Provides TV focusability and visual feedback via selector drawable.
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

    init {
        submitList(items)
    }

    object TopNavDiff : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopNavVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_top_nav_tab, parent, false)
        return TopNavVH(view)
    }

    override fun onBindViewHolder(holder: TopNavVH, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    class TopNavVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val label: TextView = itemView.findViewById(R.id.top_nav_label)
        private val icon: ImageView = itemView.findViewById(R.id.top_nav_icon)

        fun bind(text: String, onClick: (String) -> Unit) {
            // Configure as search icon if token matches
            val isSearch = text == SEARCH_ITEM
            if (isSearch) {
                label.visibility = View.GONE
                icon.visibility = View.VISIBLE
                itemView.contentDescription = itemView.context.getString(R.string.top_navigation) + " búsqueda"
            } else {
                label.visibility = View.VISIBLE
                icon.visibility = View.GONE
                label.text = text
                // Keep single line without ellipsize to avoid truncation; width is wrap_content
                label.maxLines = 1
                label.isSingleLine = true
                label.ellipsize = null
                itemView.contentDescription = text
            }

            // Focusability for TV
            itemView.isFocusable = true
            itemView.isFocusableInTouchMode = true

            // Remove scale animations to ensure the focus pill/background stays within the padded container
            itemView.setOnFocusChangeListener { v, hasFocus ->
                // Maintain stable size; rely solely on the pill background and bold text for focus
                v.scaleX = 1.0f
                v.scaleY = 1.0f

                // Toggle bold via TextAppearance state list already set in XML.
                // No explicit programmatic change to avoid relayout width shifts.
                if (!isSearch) {
                    // Force label to re-apply state for some OEMs where textAppearance state doesn't refresh
                    label.isSelected = hasFocus
                }
            }

            // Inter-item spacing strictly via margins; no edge gaps (first/last = 0)
            val density = itemView.resources.displayMetrics.density
            val spacingPx = (12f * density).toInt() // spacing between items
            val params = (itemView.layoutParams as? RecyclerView.LayoutParams)
            val pos = bindingAdapterPosition
            val parentRv = itemView.parent as? RecyclerView
            val count = parentRv?.adapter?.itemCount ?: -1
            if (params != null && pos != RecyclerView.NO_POSITION) {
                // Apply spacing only on the start side (LTR). This yields inter-item gaps without trailing/leading edges.
                params.marginStart = if (pos == 0) 0 else spacingPx
                params.marginEnd = 0
                itemView.layoutParams = params
            }

            // Click -> callback with meaningful value
            itemView.setOnClickListener { onClick(if (isSearch) "Search" else text) }

            // Intercept DPAD navigation at edges to prevent wrapping (unchanged behavior after navbar sizing tweaks)
            itemView.setOnKeyListener { _, keyCode, event ->
                if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyListener false
                val rv = itemView.parent as? RecyclerView ?: return@setOnKeyListener false
                val adapter = rv.adapter ?: return@setOnKeyListener false
                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return@setOnKeyListener false
                val lastIndex = adapter.itemCount - 1
                return@setOnKeyListener when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_LEFT -> position == 0
                    KeyEvent.KEYCODE_DPAD_RIGHT -> position == lastIndex
                    else -> false
                }
            }
        }
    }
}
