package com.example.streamly_frontend.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.streamly_frontend.R

/**
 * Adapter for 4:1 full-width banner cards.
 * Elevation-only focus; transparent background; matches dark theme.
 */
class BannerAdapter(
    items: List<BannerItem>
) : ListAdapter<BannerItem, BannerAdapter.BannerVH>(BannerDiff) {

    init {
        submitList(items)
    }

    object BannerDiff : DiffUtil.ItemCallback<BannerItem>() {
        override fun areItemsTheSame(oldItem: BannerItem, newItem: BannerItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: BannerItem, newItem: BannerItem): Boolean = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_banner_card, parent, false)
        // Ensure full-width item occupying entire RecyclerView width
        view.layoutParams = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        return BannerVH(view)
    }

    override fun onBindViewHolder(holder: BannerVH, position: Int) {
        holder.bind(getItem(position))
    }

    class BannerVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val img: ImageView = itemView.findViewById(R.id.banner_image)

        init {
            val baseElevation = 0f
            val focusedElevation = 18f
            itemView.elevation = baseElevation
            itemView.setBackgroundResource(R.drawable.bg_banner_card_state)

            itemView.setOnFocusChangeListener { v, hasFocus ->
                v.animate()
                    .scaleX(if (hasFocus) 1.02f else 1.0f)
                    .scaleY(if (hasFocus) 1.02f else 1.0f)
                    .setDuration(120L)
                    .start()
                v.elevation = if (hasFocus) focusedElevation else baseElevation
            }
        }

        fun bind(item: BannerItem) {
            itemView.contentDescription = item.title
            itemView.isFocusable = true
            itemView.isFocusableInTouchMode = true

            // Rounded mask via outline
            img.apply {
                clipToOutline = true
                background = itemView.context.getDrawable(R.drawable.rounded_mask_8dp)
            }

            Glide.with(img.context)
                .load(item.imageUrl)
                .centerCrop()
                .placeholder(R.drawable.placeholder_poster)
                .into(img)
        }
    }
}
