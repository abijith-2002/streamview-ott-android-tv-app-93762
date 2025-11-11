package com.example.streamly_frontend.home

import android.graphics.Color
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import com.example.streamly_frontend.R

/**
 * HomeBrowseFragment constructs the Home screen rails using Leanback:
 * - Top category-like rows with content cards
 * - Focus visuals suitable for D-pad TV navigation
 * Placeholder data is used to demonstrate rails and focus behavior.
 */
class HomeBrowseFragment : BrowseSupportFragment() {

    private val categories = listOf("Seguí viendo", "Películas para ti", "Series destacadas", "Canales de TV")

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupUi()
        buildRows()
    }

    private fun setupUi() {
        title = getString(R.string.app_name)
        headersState = HEADERS_DISABLED
        isHeadersTransitionOnBackEnabled = false

        brandColor = ContextCompat.getColor(requireContext(), R.color.tv_accent) // rail accent
        searchAffordanceColor = ContextCompat.getColor(requireContext(), R.color.tv_primary)
    }

    private fun buildRows() {
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

        val cardPresenter = PosterCardPresenter()

        // Build 4 rows with sample items
        categories.forEachIndexed { index, category ->
            val listRowAdapter = ArrayObjectAdapter(cardPresenter)
            val numItems = if (index == 0) 6 else 10
            repeat(numItems) { i ->
                listRowAdapter.add(SimpleContentCardData(title = "Item ${i + 1}", subtitle = category))
            }
            val headerItem = HeaderItem(index.toLong(), category)
            rowsAdapter.add(ListRow(headerItem, listRowAdapter))
        }

        adapter = rowsAdapter
    }
}

/**
 * Simple data holder for cards.
 */
data class SimpleContentCardData(
    val title: String,
    val subtitle: String
)

/**
 * Presenter for posters/cards in rows with TV focus styling.
 * Uses ImageCardView with placeholder colors to avoid external images.
 */
class PosterCardPresenter : Presenter() {

    override fun onCreateViewHolder(parent: android.view.ViewGroup): ViewHolder {
        val cardView = object : ImageCardView(parent.context) {
            override fun setSelected(selected: Boolean) {
                super.setSelected(selected)
                // Focus/selection visual feedback
                val focusColor = ContextCompat.getColor(context, R.color.tv_accent)
                val normalColor = Color.TRANSPARENT
                setInfoAreaBackgroundColor(if (selected) focusColor else normalColor)
            }
        }
        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        cardView.setMainImageDimensions(320, 180) // 16:9 card
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val data = item as SimpleContentCardData
        val cardView = viewHolder.view as ImageCardView
        cardView.titleText = data.title
        cardView.contentText = data.subtitle
        // Use a solid color placeholder as the main image
        val placeholder = android.graphics.drawable.ColorDrawable(
            ContextCompat.getColor(cardView.context, R.color.tv_overlay)
        )
        cardView.mainImage = placeholder
        cardView.setBackgroundColor(Color.TRANSPARENT)
        cardView.setInfoAreaBackgroundColor(Color.TRANSPARENT)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        cardView.mainImage = null
    }
}
