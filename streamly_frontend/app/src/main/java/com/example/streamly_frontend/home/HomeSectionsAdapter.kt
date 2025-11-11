package com.example.streamly_frontend.home

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.streamly_frontend.R

/**
 * PUBLIC_INTERFACE
 * HomeSectionsAdapter renders the Home screen's single vertical list of sections:
 * - A banner section (horizontal pager with 4:1 banners) as the first item.
 * - Multiple content rows (title + horizontal list of cards).
 *
 * This keeps banner in the vertical scroll, so it scrolls off-screen with content.
 */
class HomeSectionsAdapter(
    items: List<SectionItem>
) : ListAdapter<SectionItem, RecyclerView.ViewHolder>(SectionDiff) {

    companion object {
        private const val VIEW_TYPE_BANNER = 1
        private const val VIEW_TYPE_ROW = 2
    }

    init {
        submitList(items)
    }

    object SectionDiff : DiffUtil.ItemCallback<SectionItem>() {
        override fun areItemsTheSame(oldItem: SectionItem, newItem: SectionItem): Boolean {
            return when {
                oldItem is SectionItem.BannerSection && newItem is SectionItem.BannerSection -> true
                oldItem is SectionItem.ContentRowSection && newItem is SectionItem.ContentRowSection ->
                    oldItem.row.title == newItem.row.title
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: SectionItem, newItem: SectionItem): Boolean = oldItem == newItem
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SectionItem.BannerSection -> VIEW_TYPE_BANNER
            is SectionItem.ContentRowSection -> VIEW_TYPE_ROW
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_BANNER -> {
                val rv = RecyclerView(parent.context).apply {
                    // Do not assign a layout id that doesn't exist in this context; focus handled programmatically
                    layoutParams = RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    clipToPadding = false
                    clipChildren = false
                    isFocusable = true
                    isFocusableInTouchMode = true
                    contentDescription = parent.context.getString(R.string.banner_rail)
                }
                BannerSectionVH(rv)
            }
            VIEW_TYPE_ROW -> {
                val view = inflater.inflate(R.layout.item_content_row, parent, false)
                ContentRowSectionVH(view)
            }
            else -> throw IllegalArgumentException("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is SectionItem.BannerSection -> (holder as BannerSectionVH).bind(item.items)
            is SectionItem.ContentRowSection -> (holder as ContentRowSectionVH).bind(item.row, isFirstRow = position == 1)
        }
    }

    class BannerSectionVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recycler: RecyclerView = itemView as RecyclerView
        private val snapHelper = PagerSnapHelper()

        fun bind(banners: List<BannerItem>) {
            // LayoutManager horizontal with pager snapping
            recycler.apply {
                layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                adapter = BannerAdapter(banners)
                descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
                isFocusable = true
                isFocusableInTouchMode = true
                clipToPadding = false
                clipChildren = false

                // Size and side paddings based on screen to achieve 1744x444 on 1080p and 4:1 aspect
                post {
                    val dm = resources.displayMetrics
                    val screenPxW = dm.widthPixels
                    val screenPxH = dm.heightPixels
                    val density = dm.density

                    val logicalW = (screenPxW / density).toInt()
                    val logicalH = (screenPxH / density).toInt()

                    val targetW1080 = 1744
                    val targetH1080 = 444
                    val baselineW = 1920f
                    val widthRatio = targetW1080 / baselineW
                    val aspect = 4f

                    val lp = layoutParams as ViewGroup.LayoutParams
                    if ((screenPxW == 1920 && screenPxH == 1080) ||
                        (logicalW == 1920 && logicalH == 1080)
                    ) {
                        lp.width = targetW1080
                        lp.height = targetH1080
                    } else {
                        val computedW = (screenPxW * widthRatio).toInt()
                        val computedH = (computedW / aspect).toInt()
                        lp.width = computedW
                        lp.height = computedH
                    }
                    layoutParams = lp

                    val sidePad = ((screenPxW - lp.width) / 2).toInt().coerceAtLeast(0)
                    setPadding(sidePad, paddingTop, sidePad, paddingBottom)
                    clipToPadding = false
                    clipChildren = false
                    requestLayout()
                }

                if (onFlingListener == null) {
                    snapHelper.attachToRecyclerView(this)
                }

                if (itemDecorationCount == 0) {
                    val spacingPx = (resources.displayMetrics.density * 16).toInt()
                    addItemDecoration(object : RecyclerView.ItemDecoration() {
                        override fun getItemOffsets(
                            outRect: Rect,
                            view: View,
                            parent: RecyclerView,
                            state: RecyclerView.State
                        ) {
                            val position = parent.getChildAdapterPosition(view)
                            val count = parent.adapter?.itemCount ?: 0
                            val left = if (position == 0) 0 else spacingPx / 2
                            val right = if (position == count - 1) 0 else spacingPx / 2
                            outRect.set(left, 0, right, 0)
                        }
                    })
                }

                setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        getChildAt(0)?.requestFocus()
                    }
                }

                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(rv, newState)
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            val child = snapHelper.findSnapView(layoutManager)
                            child?.requestFocus()
                        }
                    }
                })
            }
        }
    }

    class ContentRowSectionVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.row_title)
        private val innerRecycler: RecyclerView = itemView.findViewById(R.id.row_inner_recycler)

        fun bind(row: ContentRow, isFirstRow: Boolean) {
            title.text = row.title
            innerRecycler.apply {
                layoutManager = LinearLayoutManager(itemView.context, RecyclerView.HORIZONTAL, false)
                adapter = ContentCardAdapter(row.items)
                descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
                isFocusable = true
                isFocusableInTouchMode = true
                // Up from the very first row goes to top nav
                nextFocusUpId = if (isFirstRow) R.id.top_nav_recycler else View.NO_ID

                setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        val rails = (itemView.parent as? RecyclerView) ?: return@setOnFocusChangeListener
                        rails.clipToPadding = false
                        rails.clipChildren = false
                        (rails.parent as? ViewGroup)?.let { parent ->
                            parent.clipToPadding = false
                            parent.clipChildren = false
                        }
                        val rect = Rect()
                        title.getDrawingRect(rect)

                        val density = (itemView.resources?.displayMetrics?.density ?: 1f)
                        val headerDp = 52f
                        val marginDp = 24f
                        val railsPadTopDp = 24f
                        val safetyDp = 8f
                        val extraTopPx = ((headerDp + marginDp + railsPadTopDp + safetyDp) * density).toInt()

                        rect.top = (rect.top - extraTopPx).coerceAtLeast(0)
                        rect.bottom = (rect.bottom - extraTopPx).coerceAtLeast(rect.top)
                        rails.requestChildRectangleOnScreen(itemView, rect, true)
                    }
                }
            }
        }
    }
}

/**
 * PUBLIC_INTERFACE
 * Sealed model for Home vertical sections: Banner + Rows.
 */
sealed class SectionItem {
    data class BannerSection(val items: List<BannerItem>) : SectionItem()
    data class ContentRowSection(val row: ContentRow) : SectionItem()
}
