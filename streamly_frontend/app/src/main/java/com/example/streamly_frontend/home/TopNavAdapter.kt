package com.example.streamly_frontend.home

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
                itemView.contentDescription = text
            }

            // Focusability for TV
            itemView.isFocusable = true
            itemView.isFocusableInTouchMode = true

            // Click -> callback with meaningful value
            itemView.setOnClickListener { onClick(if (isSearch) "Search" else text) }

            // Ensure left/right focus traversal naturally uses order in RecyclerView
            // Additional padding and background handled via layout/drawable
        }
    }
}
