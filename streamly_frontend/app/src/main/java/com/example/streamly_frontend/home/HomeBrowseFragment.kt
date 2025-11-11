package com.example.streamly_frontend.home

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.Presenter
import com.example.streamly_frontend.R

/**
 * HomeBrowseFragment constructs the Home screen rails using Leanback mapped to Figma:
 * - Top Navigation row (Inicio, Películas, Series, TV en vivo, Kids, Mis Contenidos)
 * - Highlights banner hint row (non-interactive placeholder to reflect hero presence)
 * - Content rails: "Seguí viendo", "Películas para ti", "Series destacadas", "Canales de TV"
 *
 * Focus visuals match Ocean theme; overscan-safe paddings are provided in the Activity layout.
 */
class HomeBrowseFragment : BrowseSupportFragment() {

    // Top navigation per Figma
    private val topNav = listOf("Inicio", "Películas", "Series", "TV en vivo", "Kids", "Mis Contenidos")

    // Rails matching the design references (Spanish labels)
    private val rails = listOf(
        "Seguí viendo",
        "Películas para ti",
        "Series destacadas",
        "Canales de TV"
    )

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupUi()
        buildRows()
    }

    private fun setupUi() {
        // Disable side headers: design shows horizontal rails occupying the screen
        headersState = HEADERS_DISABLED
        isHeadersTransitionOnBackEnabled = false

        title = resources.getString(R.string.home_title)
        brandColor = ContextCompat.getColor(requireContext(), R.color.tv_accent)
        searchAffordanceColor = ContextCompat.getColor(requireContext(), R.color.tv_primary)
        // Minimalist/Ocean: black background is already applied via theme
    }

    private fun buildRows() {
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter().apply {
            // Slightly larger row height spacing to mimic Figma vertical rhythm
            shadowEnabled = false
            selectEffectEnabled = false
        })

        // Row 0: Top navigation as a horizontal rail (focusable pills/text)
        val menuAdapter = ArrayObjectAdapter(TopMenuPresenter())
        topNav.forEachIndexed { index, label ->
            menuAdapter.add(TopMenuItem(label = label, isActive = index == 0))
        }
        rowsAdapter.add(ListRow(HeaderItem(0, ""), menuAdapter))

        // Row 1: Highlights banner hint (non-interactive; one wide item)
        val highlightAdapter = ArrayObjectAdapter(HeroBannerHintPresenter())
        highlightAdapter.add(Unit) // single placeholder item
        rowsAdapter.add(ListRow(HeaderItem(1, ""), highlightAdapter))

        // Content rows: sizes reflect Figma (continue watching first)
        val posterPresenter = PosterCardPresenter(
            cardWidth = 320, // safe 16:9 within overscan; visual scale from Figma 412/474 widths
            cardHeight = 180,
            titleLines = 1
        )

        rails.forEachIndexed { rIndex, railName ->
            val contentAdapter = ArrayObjectAdapter(posterPresenter)
            // Use 10 items; first rail shows progress conceptually
            val itemCount = if (rIndex == 0) 10 else 12
            repeat(itemCount) { i ->
                val title = when {
                    rIndex == 0 && i < SAMPLE_TITLES_CONTINUE.size -> SAMPLE_TITLES_CONTINUE[i]
                    else -> SAMPLE_TITLES_GENERIC[i % SAMPLE_TITLES_GENERIC.size]
                }
                contentAdapter.add(
                    SimpleContentCardData(
                        title = title,
                        subtitle = railName,
                        // Mark some items as "progress-enabled" to mimic Figma bar
                        progress = if (rIndex == 0) ((i + 1) * 7 % 100) else null
                    )
                )
            }
            rowsAdapter.add(ListRow(HeaderItem(2 + rIndex.toLong(), railName), contentAdapter))
        }

        adapter = rowsAdapter
    }

    companion object {
        private val SAMPLE_TITLES_CONTINUE = listOf("Rogue One", "Ex Machina", "Sing Street", "2012", "Ad Astra")
        private val SAMPLE_TITLES_GENERIC = listOf(
            "Arrival", "Moonlight", "The Martian", "Blade Runner 2049",
            "La La Land", "Gravity", "Inception", "Interstellar"
        )
    }
}

/**
 * Top navigation pill/text item for the first row.
 */
data class TopMenuItem(val label: String, val isActive: Boolean)

/**
 * Presenter for Top Navigation items.
 * Displays a rounded pill background for the active item and lighter text for inactive items,
 * matching the Figma header nav styling and Ocean minimalist palette.
 */
class TopMenuPresenter : Presenter() {
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val ctx = parent.context
        val container = FrameLayout(ctx).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            val p = (16 * 3) // about 48px horizontal inset to reflect pill width; scaled for TV dp
            setPadding(p, 16, p, 16)
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        val tv = TextView(ctx).apply {
            textSize = 24f // close to Figma 29px but TV-appropriate
            setTextColor(ContextCompat.getColor(ctx, R.color.tv_secondary))
            gravity = Gravity.CENTER
        }
        container.addView(tv)
        container.tag = tv
        return ViewHolder(container)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val data = item as TopMenuItem
        val container = viewHolder.view as FrameLayout
        val tv = container.tag as TextView
        tv.text = data.label

        val ctx = container.context
        val activeBg = ContextCompat.getColor(ctx, R.color.tv_accent)
        val inactiveText = ContextCompat.getColor(ctx, R.color.tv_secondary)
        val activeText = ContextCompat.getColor(ctx, R.color.tv_primary)

        // Simple rounded pill using background color + rounded ripple-like highlight on focus
        val bg = if (data.isActive) ColorDrawable(activeBg) else ColorDrawable(Color.TRANSPARENT)
        container.background = bg
        tv.setTextColor(if (data.isActive) activeText else inactiveText)

        container.setOnFocusChangeListener { v, hasFocus ->
            if (data.isActive) {
                // Active remains accent; slightly increase alpha on focus
                (v.background as? ColorDrawable)?.color = activeBg
            } else {
                // Inactive shows subtle focus backdrop according to Ocean accent
                (v.background as? ColorDrawable)?.color = if (hasFocus) {
                    Color.parseColor("#333333")
                } else {
                    Color.TRANSPARENT
                }
            }
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) = Unit
}

/**
 * Simple hero banner hint presenter to visually mark the highlight band per Figma.
 * Non-interactive spacer/banner row with subtle accent border.
 */
class HeroBannerHintPresenter : Presenter() {
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val ctx = parent.context
        val view = View(ctx).apply {
            // approximate 1744x444 scaled down for TV rows height; Leanback rows height is constrained,
            // so we simulate as a thin banner stripe to hint presence.
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 48)
            setBackgroundColor(Color.TRANSPARENT)
            // add top/bottom border effect by overlay color drawable on focus of rows
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        // Add subtle separator color to hint banner region
        viewHolder.view.setBackgroundColor(Color.parseColor("#1AFFFFFF"))
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) = Unit
}

/**
 * Simple data holder for content cards with optional progress percentage.
 */
data class SimpleContentCardData(
    val title: String,
    val subtitle: String,
    val progress: Int? = null
)

/**
 * Presenter for posters/cards with TV focus styling and optional progress bar
 * to mirror "Seguí viendo" rail design cues.
 */
class PosterCardPresenter(
    private val cardWidth: Int,
    private val cardHeight: Int,
    private val titleLines: Int = 1
) : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val ctx = parent.context
        val cardView = object : ImageCardView(ctx) {
            override fun setSelected(selected: Boolean) {
                super.setSelected(selected)
                // Focus/selection visual feedback (Ocean accent)
                val outline = if (selected) ContextCompat.getColor(context, R.color.tv_accent) else Color.TRANSPARENT
                setInfoAreaBackgroundColor(Color.TRANSPARENT)
                setBackgroundColor(outline)
            }
        }
        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        // Figma shows 16:9 posters with info name strip; Leanback's title/content area suits this
        cardView.setMainImageDimensions(cardWidth, cardHeight)
        cardView.setBackgroundColor(Color.TRANSPARENT)
        cardView.infoVisibility = ImageCardView.CARD_REGION_VISIBLE_ALWAYS
        // Typography approximations per design tokens
        cardView.titleText = ""
        cardView.contentText = ""

        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val data = item as SimpleContentCardData
        val cardView = viewHolder.view as ImageCardView

        // Titles approximating typo_23 and subtitle per rail
        cardView.titleText = data.title
        cardView.contentText = data.subtitle

        val overlay = ContextCompat.getColor(cardView.context, R.color.tv_overlay)
        cardView.mainImage = ColorDrawable(overlay)

        // Ensure info area uses transparent bg and rely on focus bg to highlight
        cardView.setInfoAreaBackgroundColor(Color.TRANSPARENT)

        // We cannot add custom child views easily to ImageCardView; emulate progress using content text suffix
        if (data.progress != null) {
            cardView.contentText = "${data.subtitle}  •  ${data.progress}%"
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        cardView.mainImage = null
    }
}
