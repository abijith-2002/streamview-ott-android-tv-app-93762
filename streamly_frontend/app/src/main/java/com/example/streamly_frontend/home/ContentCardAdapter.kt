package com.example.streamly_frontend.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.streamly_frontend.R

/**
 * Adapter for focusable content cards.
 * Uses centerCrop images and DP sizing matching Figma-inspired proportions for TV.
 */
class ContentCardAdapter(
    private val items: List<ContentItem>,
    private val onItemEnter: ((item: ContentItem, adapterPosition: Int) -> Unit)? = null
) : ListAdapter<ContentItem, ContentCardAdapter.CardVH>(CardDiff) {

    init {
        submitList(items)
    }

    object CardDiff : DiffUtil.ItemCallback<ContentItem>() {
        override fun areItemsTheSame(oldItem: ContentItem, newItem: ContentItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ContentItem, newItem: ContentItem): Boolean = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_content_card, parent, false)
        return CardVH(view)
    }

    override fun onBindViewHolder(holder: CardVH, position: Int) {
        holder.bind(getItem(position))
        // Key handler for DPAD_CENTER/ENTER to open Content Info
        holder.itemView.setOnKeyListener { v, keyCode, event ->
            if (event.action != android.view.KeyEvent.ACTION_DOWN) return@setOnKeyListener false
            return@setOnKeyListener when (keyCode) {
                android.view.KeyEvent.KEYCODE_DPAD_CENTER, android.view.KeyEvent.KEYCODE_ENTER -> {
                    val pos = holder.bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        onItemEnter?.invoke(getItem(pos), pos)
                        true
                    } else false
                }
                else -> false
            }
        }
    }

    class CardVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val poster: ImageView = itemView.findViewById(R.id.card_poster)
        private val title: TextView = itemView.findViewById(R.id.card_title)

        init {
            // Elevation-only focus + gentle scale for visibility on TV.
            val baseElevation = 0f
            val focusedElevation = 16f // dp-like visual; used as px here for simplicity on View

            itemView.elevation = baseElevation
            // Ensure no residual background; background is managed by XML selector which is transparent
            itemView.setBackgroundResource(R.drawable.bg_card_state)

            itemView.setOnFocusChangeListener { v, hasFocus ->
                // Animate slight scale
                v.animate()
                    .scaleX(if (hasFocus) 1.06f else 1.0f)
                    .scaleY(if (hasFocus) 1.06f else 1.0f)
                    .setDuration(120L)
                    .start()

                // Elevation toggle (no border/glow/background change)
                v.elevation = if (hasFocus) focusedElevation else baseElevation
            }
        }

        fun bind(item: ContentItem) {
            title.text = item.title
            itemView.contentDescription = item.title
            itemView.isFocusable = true
            itemView.isFocusableInTouchMode = true

            // Ensure poster has subtle rounded corners while preserving centerCrop
            poster.apply {
                clipToOutline = true
                background = itemView.context.getDrawable(R.drawable.rounded_mask_8dp)
            }

            Glide.with(poster.context)
                .load(item.imageUrl)
                .centerCrop()
                .placeholder(R.drawable.placeholder_poster)
                .into(poster)
        }
    }
}
