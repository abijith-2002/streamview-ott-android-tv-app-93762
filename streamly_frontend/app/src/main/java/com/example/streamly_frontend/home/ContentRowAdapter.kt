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
                // Direct next focus up goes to top nav for first row
                nextFocusUpId = R.id.top_nav_recycler

                // Ensure when any child inside this row gains focus, the row title is visible.
                // We perform requestChildRectangleOnScreen for the 'title' view against the parent rails RecyclerView.
                setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        // Parent vertical rails RecyclerView
                        val rails = (itemView.parent as? RecyclerView) ?: return@setOnFocusChangeListener
                        // Compute the title's rect relative to this row's root (itemView)
                        val rect = Rect()
                        title.getDrawingRect(rect)
                        // Ask rails to bring the rectangle of this row (itemView) containing the title into view.
                        // requestChildRectangleOnScreen expects the child (itemView) and a rect in child's coordinates.
                        // Add a small top offset so the title is clearly visible.
                        val extraTop = ((itemView.resources?.displayMetrics?.density ?: 1f) * 8f).toInt() // 8dp
                        rect.top = (rect.top - extraTop).coerceAtLeast(0)
                        rails.requestChildRectangleOnScreen(itemView, rect, true)
                    }
                }
            }
        }
    }
}
