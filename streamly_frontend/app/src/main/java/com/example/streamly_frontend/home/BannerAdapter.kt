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
 * Focus behavior: no elevation, no glow, no background change, no scaling.
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
        // Ensure the item occupies the entire page/container (width and height)
        view.layoutParams = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return BannerVH(view)
    }

    override fun onBindViewHolder(holder: BannerVH, position: Int) {
        holder.bind(getItem(position))
    }

    class BannerVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val img: ImageView = itemView.findViewById(R.id.banner_image)

        init {
            // Remove elevation/scale/glow/background changes on focus.
            itemView.elevation = 0f
            itemView.setBackgroundResource(android.R.color.transparent)
            itemView.setOnFocusChangeListener(null)
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
