package com.example.streamly_frontend.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.streamly_frontend.R

/**
 * Adapter for top navigation categories.
 * Provides TV focusability and visual feedback via selector drawable.
 */
class TopNavAdapter(
    items: List<String>,
    private val onClick: (String) -> Unit
) : ListAdapter<String, TopNavAdapter.TopNavVH>(TopNavDiff) {

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

        fun bind(text: String, onClick: (String) -> Unit) {
            label.text = text
            itemView.isFocusable = true
            itemView.isFocusableInTouchMode = true
            itemView.setOnClickListener { onClick(text) }
            // Ensure accessibility title
            itemView.contentDescription = text
        }
    }
}
