package com.example.streamly_frontend.home

import android.graphics.Rect
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.streamly_frontend.R

/**
 * PUBLIC_INTERFACE
 * HomeSectionsAdapter renders the Home screen's single vertical list of sections:
 * - A header section (brand title + centered pill top navbar).
 * - A banner section (horizontal pager with 4:1 banners).
 * - Multiple content rows (title + horizontal list of cards).
 *
 * This keeps the brand and navbar in the vertical scroll (no static pinning).
 */
class HomeSectionsAdapter(
    items: List<SectionItem>
) : ListAdapter<SectionItem, RecyclerView.ViewHolder>(SectionDiff) {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_BANNER = 1
        private const val VIEW_TYPE_ROW = 2
    }

    init {
        submitList(items)
    }

    object SectionDiff : DiffUtil.ItemCallback<SectionItem>() {
        override fun areItemsTheSame(oldItem: SectionItem, newItem: SectionItem): Boolean {
            return when {
                oldItem is SectionItem.HeaderSection && newItem is SectionItem.HeaderSection -> true
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
            is SectionItem.HeaderSection -> VIEW_TYPE_HEADER
            is SectionItem.BannerSection -> VIEW_TYPE_BANNER
            is SectionItem.ContentRowSection -> VIEW_TYPE_ROW
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                // Inflate a compact container with brand + navbar
                val view = inflater.inflate(R.layout.item_home_header, parent, false)
                HeaderSectionVH(view)
            }
            VIEW_TYPE_BANNER -> {
                val rv = RecyclerView(parent.context).apply {
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
            is SectionItem.HeaderSection -> (holder as HeaderSectionVH).bind(item)
            is SectionItem.BannerSection -> (holder as BannerSectionVH).bind(item.items, nextFocusUpToHeader = true)
            is SectionItem.ContentRowSection -> (holder as ContentRowSectionVH).bind(item.row, isFirstRow = position == 2)
        }
    }

    /**
     * Header Section: Brand title + Top Navbar in a centered pill container.
     * Preserves navbar styles and DPAD behavior (edge left/right no-op).
     */
    class HeaderSectionVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val brand: TextView = itemView.findViewById(R.id.brand_text_scrolling)
        private val topNav: RecyclerView = itemView.findViewById(R.id.top_nav_recycler_scrolling)

        // Tracks whether we've ever auto-focused the nav after entering it once
        private var hasEverFocusedNav = false

        // Flag to request centering to 'Inicio' on next focus entry only (used when view is rebound)
        private var recenterOnNextFocus: Boolean = false

        fun bind(header: SectionItem.HeaderSection) {
            // Brand styling with "Claro-" tinted brand red
            val ctx = itemView.context
            val full = "Claro-video"
            val spannable = SpannableString(full)
            val brandRed = ContextCompat.getColor(ctx, R.color.brand_claro_red)
            val prefix = "Claro-"
            val end = prefix.length.coerceAtMost(full.length)
            spannable.setSpan(ForegroundColorSpan(brandRed), 0, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            brand.text = spannable
            brand.contentDescription = "Claro video"

            // Navbar setup
            val categories = header.categories
            val adapter = TopNavAdapter(categories) { /* future: filter rails */ }
            topNav.apply {
                // Use a Flow-like layout that wraps items into multiple rows if needed.
                // Since RecyclerView doesn't have FlowLayoutManager by default, we simulate wrapping
                // by using a Flexbox-like approach with a custom LayoutManager fallback to GridAutoFit.
                // Here, we approximate using a GridLayoutManager with dynamic span count based on width.
                val gridLm = object : androidx.recyclerview.widget.GridLayoutManager(ctx, 1, RecyclerView.HORIZONTAL, false) {
                    private var lastWidth = -1
                    private var lastSpan = -1
                    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
                        super.onLayoutChildren(recycler, state)
                        val w = width
                        if (w > 0 && w != lastWidth && adapter != null) {
                            lastWidth = w
                            // Estimate item width using a baseline (label ~16sp + 20dp padding ≈ 64-120dp).
                            // Compute a span count that auto-fits items per row, forcing wrap to multiple rows.
                            val density = context.resources.displayMetrics.density
                            val estItemMinDp = 88f // min pill width target
                            val estItemMinPx = (estItemMinDp * density).toInt().coerceAtLeast(1)
                            val newSpan = (w / estItemMinPx).coerceAtLeast(1)
                            if (newSpan != lastSpan) {
                                lastSpan = newSpan
                                spanCount = newSpan
                                orientation = RecyclerView.VERTICAL
                                requestLayout()
                            }
                        }
                    }
                }
                layoutManager = gridLm
                this.adapter = adapter
                descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
                isFocusable = true
                isFocusableInTouchMode = true
                clipToPadding = false
                clipChildren = false

                // DPAD LEFT/RIGHT edge handling no longer needed; wrapping ensures visibility without horizontal scroll.
                setOnKeyListener(null)

                // Helper to find and focus 'Inicio'
                fun focusInicio() {
                    val inicioIndex = categories.indexOfFirst { it.equals("Inicio", ignoreCase = true) }
                    if (inicioIndex >= 0) {
                        // For GridLayoutManager (vertical with wrapping), just scroll to position
                        scrollToPosition(inicioIndex)
                        post {
                            val vh = findViewHolderForAdapterPosition(inicioIndex)
                            vh?.itemView?.requestFocus()
                        }
                    }
                }

                // Detect navbar focus entry from outside: when navbar itself gains focus and no child has it,
                // or when a previous rebind requested a re-center.
                setOnFocusChangeListener { rvView, hasFocus ->
                    if (hasFocus) {
                        val rv = rvView as RecyclerView
                        val currentFocused = rv.findFocus()
                        if (recenterOnNextFocus || (!hasEverFocusedNav || currentFocused == null)) {
                            focusInicio()
                            hasEverFocusedNav = true
                            recenterOnNextFocus = false
                        }
                    }
                }

                // If users navigate UP from banner into header, ensure we re-center to 'Inicio'
                // without affecting left/right within the navbar
                setOnKeyListener { v, keyCode, event ->
                    if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyListener false
                    when (keyCode) {
                        // Do not consume LEFT/RIGHT, keep prior handler decision
                        KeyEvent.KEYCODE_DPAD_UP -> {
                            // Coming from below into the header area; request focus to 'Inicio'
                            focusInicio()
                            true
                        }
                        else -> false
                    }
                }

                // When adapter is rebound (e.g., RecyclerView recycles the header on scroll), mark to re-center next time navbar gains focus
                this.adapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                    override fun onChanged() {
                        recenterOnNextFocus = true
                    }
                    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                        recenterOnNextFocus = true
                    }
                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                        recenterOnNextFocus = true
                    }
                    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                        recenterOnNextFocus = true
                    }
                })
            }

            // Also set a focus change listener on the header container: when header root itself gains focus (from outside),
            // scroll and focus 'Inicio'. This helps when focus lands on the header container via DPAD_UP from rails.
            itemView.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    recenterOnNextFocus = true
                    topNav.requestFocus()
                }
            }
        }
    }

    class BannerSectionVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recycler: RecyclerView = itemView as RecyclerView
        private val snapHelper = PagerSnapHelper()

        fun bind(banners: List<BannerItem>, nextFocusUpToHeader: Boolean) {
            recycler.apply {
                layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                adapter = BannerAdapter(banners)
                descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
                isFocusable = true
                isFocusableInTouchMode = true
                clipToPadding = false
                clipChildren = false

                // With a wrapping navbar, allow natural focus up without forcing a specific child id.
                if (nextFocusUpToHeader) {
                    nextFocusUpId = View.NO_ID
                }

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
                // Up from the very first row goes to banner (which itself goes up to header)
                nextFocusUpId = if (isFirstRow) View.NO_ID else View.NO_ID

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
 * Sealed model for Home vertical sections: Header + Banner + Rows.
 */
sealed class SectionItem {
    data class HeaderSection(val categories: List<String>) : SectionItem()
    data class BannerSection(val items: List<BannerItem>) : SectionItem()
    data class ContentRowSection(val row: ContentRow) : SectionItem()
}
