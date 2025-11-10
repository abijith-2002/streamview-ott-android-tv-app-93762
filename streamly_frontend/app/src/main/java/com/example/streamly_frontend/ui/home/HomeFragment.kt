package com.example.streamly_frontend.ui.home

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.leanback.app.BackgroundManager
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import com.example.streamly_frontend.R

/**
 * Browse-style Home fragment with:
 * - Top navigation bar (Inicio, Películas, Series, TV en vivo, Kids, Mis Contenidos)
 * - Full-width hero/banner background using BackgroundManager
 * - Content rails (ArrayObjectAdapter + ListRow)
 * - D-pad focus with Leanback's default focus scaling and highlight
 */
class HomeFragment : BrowseSupportFragment(), OnItemViewClickedListener, OnItemViewSelectedListener {

    private lateinit var rowsAdapter: ArrayObjectAdapter
    private lateinit var backgroundManager: BackgroundManager

    // Hold a reference to the created root view for lifecycle-safe postings
    private var rootViewRef: View? = null

    private val topNavLabels = listOf(
        "Inicio", "Películas", "Series", "TV en vivo", "Kids", "Mis Contenidos"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prepareBackground()
        // Defer UI wiring that touches TitleView until view is created
        setOnItemViewClickedListener(this)
        setOnItemViewSelectedListener(this)
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Cache the root view reference for lifecycle-safe operations
        rootViewRef = view
        // Use the provided 'view' parameter for any direct operations on the fragment view.
        setupUIAndTitleSafely(view)
        buildRows()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear view reference to avoid leaks
        rootViewRef = null
    }

    private fun prepareBackground() {
        backgroundManager = BackgroundManager.getInstance(activity)
        backgroundManager.attach(activity?.window)
        // Full-width hero/banner impression
        backgroundManager.color = Color.BLACK
        // Optionally load a drawable banner; fall back to color if none
        backgroundManager.drawable = ContextCompat.getDrawable(requireContext(), R.drawable.hero_banner_placeholder)
    }

    /**
     * Sets up UI and title-related properties safely once the view is created.
     * Ensures any posted work is guarded with isAdded and uses requireView().
     */
    private fun setupUIAndTitleSafely(rootView: View) {
        // Keep title empty; use headers/rows. Configure after TitleView exists.
        title = ""

        // Headers alignment and brand color accents
        brandColor = ContextCompat.getColor(requireContext(), R.color.tv_accent)
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true

        // Build a top navigation bar AFTER TitleView is available
        buildTopNavBarIfReady(rootView)

        // Ensure overscan-safe padding only when the fragment is attached and has a view
        if (isAdded) {
            // Post using the provided rootView and keep a cache for future retries
            rootView.post { setTitlePaddingForTV() }
        }
    }

    private fun buildTopNavBarIfReady(rootView: View) {
        // Guard against null TitleViewAdapter/TitleView during lifecycle race conditions
        val adapter = titleViewAdapter
        // TitleViewAdapter#view is not a public property; avoid unresolved reference to a bare 'view'
        // Instead, guard only on adapter being null and rely on posting to retry when needed.
        if (adapter == null) {
            // TitleView not yet ready; try again on next frame
            if (isAdded) {
                // Prefer cached rootViewRef; fallback to requireView() safely
                val postTarget = rootViewRef ?: requireView()
                postTarget.post { buildTopNavBarIfReady(postTarget) }
            }
            return
        }

        // Disable badge and search affordance if not used to avoid NPEs on uninitialized views
        badgeDrawable = null
        setOnSearchClickedListener { /* No-op: search not implemented yet */ }

        // Set a single centered title text by joining labels with spacing
        title = topNavLabels.joinToString("    ")
        setTitlePaddingForTV()
    }

    private fun setTitlePaddingForTV() {
        // Typography: larger text for TV, add insets to keep overscan-safe
        val px = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 24f, resources.displayMetrics
        ).toInt()
        setBadgeDrawable(null)
        if (isAdded) {
            // requireView() is safe here because we already checked isAdded
            requireView().setPadding(px, px, px, 0)
        }
    }

    private fun buildRows() {
        rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

        // Row 1: Seguí viendo (Continue Watching)
        rowsAdapter.add(buildCardRow("Seguí viendo", sampleItems("Rogue One", "Ex Machina", "Sing Street", "2012", "Ad Astra")))

        // Row 2: Canales de TV (Live TV)
        rowsAdapter.add(buildCardRow("Canales de TV", sampleItems("Marca Claro Radio", "E.T.", "Noticias 24", "Música en Vivo")))

        adapter = rowsAdapter
    }

    private fun buildCardRow(headerTitle: String, items: List<CardItem>): ListRow {
        val header = HeaderItem(headerTitle)
        val cardPresenter = HeroCardPresenter()
        val listRowAdapter = ArrayObjectAdapter(cardPresenter)
        items.forEach { listRowAdapter.add(it) }
        return ListRow(header, listRowAdapter)
    }

    private fun sampleItems(vararg titles: String): List<CardItem> {
        return titles.mapIndexed { index, t ->
            CardItem(
                id = index.toLong(),
                title = t,
                description = "Descripción $index",
                imageRes = when (index % 3) {
                    0 -> R.drawable.sample_poster_1
                    1 -> R.drawable.sample_poster_2
                    else -> R.drawable.sample_poster_3
                }
            )
        }
    }

    override fun onItemClicked(
        itemViewHolder: Presenter.ViewHolder?,
        item: Any?,
        rowViewHolder: RowPresenter.ViewHolder?,
        row: Row?
    ) {
        // TODO: Wire to details and playback activities when available
        // For now, we show a simple click feedback via headers state change
        // Could navigate like: startActivity(Intent(requireContext(), DetailsActivity::class.java).putExtra("id", (item as CardItem).id))
    }

    override fun onItemSelected(
        itemViewHolder: Presenter.ViewHolder?,
        item: Any?,
        rowViewHolder: RowPresenter.ViewHolder?,
        row: Row?
    ) {
        // Update hero/banner when selection changes to simulate full-width banner
        if (item is CardItem) {
            // Set a different drawable or keep placeholder
            backgroundManager.drawable = ContextCompat.getDrawable(requireContext(), item.imageRes)
        }
    }
}

/**
 * Simple data model for cards.
 */
data class CardItem(
    val id: Long,
    val title: String,
    val description: String,
    val imageRes: Int
)

/**
 * Presenter for 16:9 posters with focus scaling and title.
 * Uses ImageCardView for default Leanback behavior.
 */
class HeroCardPresenter : Presenter() {
    override fun onCreateViewHolder(parent: android.view.ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            setMainImageDimensions(
                dp(parent, 320), // width
                dp(parent, 180)  // height (16:9)
            )
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
            setInfoAreaBackgroundColor(Color.parseColor("#28292F"))
            setMainImageScaleType(android.widget.ImageView.ScaleType.CENTER_CROP)
        }
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val card = viewHolder.view as ImageCardView
        val data = item as CardItem
        card.titleText = data.title
        card.contentText = data.description
        card.setMainImage(ContextCompat.getDrawable(card.context, data.imageRes))
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val card = viewHolder.view as ImageCardView
        card.mainImage = null
    }

    private fun dp(parent: android.view.ViewGroup, dp: Int): Int {
        val dm = parent.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), dm).toInt()
    }
}
