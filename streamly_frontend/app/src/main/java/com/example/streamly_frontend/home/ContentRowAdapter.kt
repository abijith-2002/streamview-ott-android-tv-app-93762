package com.example.streamly_frontend.home

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.streamly_frontend.R

/**
 * Adapter that renders a list of content rows.
 * Each row has a title and a horizontal list of content cards.
 *
 * Behavior fix:
 * - When focus moves to any card inside a row, we ensure the row title is scrolled into view
 *   within the parent rails RecyclerView by requesting the title's rectangle to be visible.
 *   This avoids the case where "Seguí viendo" becomes clipped/hidden after returning focus
 *   from lower rails.
 */
class ContentRowAdapter(
    rows: List<ContentRow>
) : ListAdapter<ContentRow, ContentRowAdapter.RowVH>(RowDiff) {

    init {
        submitList(rows)
    }

    object RowDiff : DiffUtil.ItemCallback<ContentRow>() {
        override fun areItemsTheSame(oldItem: ContentRow, newItem: ContentRow): Boolean = oldItem.title == newItem.title
        override fun areContentsTheSame(oldItem: ContentRow, newItem: ContentRow): Boolean = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_content_row, parent, false)
        return RowVH(view)
    }

    override fun onBindViewHolder(holder: RowVH, position: Int) {
        holder.bind(getItem(position))
    }

    class RowVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.row_title)
        private val innerRecycler: RecyclerView = itemView.findViewById(R.id.row_inner_recycler)

        fun bind(row: ContentRow) {
            title.text = row.title
            // Horizontal content list
            innerRecycler.apply {
                layoutManager = LinearLayoutManager(itemView.context, RecyclerView.HORIZONTAL, false)
                adapter = ContentCardAdapter(row.items)
                descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
                isFocusable = true
                isFocusableInTouchMode = true
                // Header (brand + navbar) is now part of the unified list above the banner.
                // Do not hardwire focus up to an external pinned view; let RecyclerView handle natural DPAD up traversal.
                nextFocusUpId = View.NO_ID

                // Ensure when any child inside this row gains focus, the row title is visible.
                // We perform requestChildRectangleOnScreen for the 'title' view against the parent rails RecyclerView.
                setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        // Parent vertical rails RecyclerView
                        val rails = (itemView.parent as? RecyclerView) ?: return@setOnFocusChangeListener

                        // Do not clip children/padding while bringing rect on screen
                        rails.clipToPadding = false
                        rails.clipChildren = false
                        (rails.parent as? ViewGroup)?.let { parent ->
                            parent.clipToPadding = false
                            parent.clipChildren = false
                        }

                        // Compute the title's rect relative to this row's root (itemView)
                        val rect = Rect()
                        title.getDrawingRect(rect)

                        // Add a fixed top inset equal to header height + margin (approx):
                        // - Top nav background height: 52dp
                        // - Margin below nav to rails: 24dp
                        // - Rails' own top padding: increased to 24dp in layout
                        // We'll overshoot slightly for safe visibility near overscan.
                        val density = (itemView.resources?.displayMetrics?.density ?: 1f)
                        val headerDp = 52f
                        val marginDp = 24f
                        val railsPadTopDp = 24f
                        val safetyDp = 8f
                        val extraTopPx = ((headerDp + marginDp + railsPadTopDp + safetyDp) * density).toInt()

                        // Shift rect upward by extraTop so title sits fully below the top bar
                        rect.top = (rect.top - extraTopPx).coerceAtLeast(0)
                        rect.bottom = (rect.bottom - extraTopPx).coerceAtLeast(rect.top)

                        // Smooth scroll to reveal with offset
                        rails.requestChildRectangleOnScreen(itemView, rect, true)
                    }
                }
            }
        }
    }
}
