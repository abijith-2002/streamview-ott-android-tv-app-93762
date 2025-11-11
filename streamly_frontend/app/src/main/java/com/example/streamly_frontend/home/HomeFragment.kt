package com.example.streamly_frontend.home

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Rect
import com.example.streamly_frontend.R
import com.example.streamly_frontend.databinding.FragmentHomeBinding

/**
 * PUBLIC_INTERFACE
 * HomeFragment is the main Android TV home screen implemented natively using RecyclerView.
 *
 * Summary:
 * - Displays a top navigation bar (horizontally scrollable) and content rails (vertical list of rows).
 * - Each content row contains a horizontal list of focusable content cards suitable for D-pad navigation.
 *
 * Parameters:
 * - None
 *
 * Returns:
 * - A Fragment view that serves as the app's home screen.
 *
 * Ocean Professional minimalist theme:
 * - Colors pulled from colors.xml: ocean_primary, ocean_secondary, ocean_background, ocean_surface, ocean_text.
 * - Typography uses TextAppearance styles defined in styles.xml.
 *
 * Focus and accessibility:
 * - Cards are focusable with state list background and scale on focus.
 * - contentDescription set for images and importantForAccessibility marked to ensure screen readers behavior on TV devices.
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var topNavAdapter: TopNavAdapter
    private lateinit var contentRowAdapter: ContentRowAdapter
    private lateinit var bannerAdapter: BannerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupBrand()
        setupTopNav()
        setupBannerRail()
        setupContentRails()
    }

    private fun setupBrand() {
        val tv = binding.brandText
        // Build "Claro-video" with "Claro-" colored #E1251B and full text bold via TextView attributes.
        val full = "Claro-video"
        val spannable = SpannableString(full)
        // Use the specified brand color #9B0F0F
        val brandRed = ContextCompat.getColor(requireContext(), R.color.brand_claro_red)
        val prefix = "Claro-"
        val end = prefix.length.coerceAtMost(full.length)
        spannable.setSpan(ForegroundColorSpan(brandRed), 0, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        // Remaining "video" inherits TextView textColor (ocean_text)
        tv.text = spannable
        // Accessibility label
        tv.contentDescription = "Claro video"
    }

    private fun setupTopNav() {
        // Restricted categories for top navigation bar with a leading Search item
        // The adapter will render a search icon for the first "Search" pseudo-item
        val categories = listOf(
            TopNavAdapter.SEARCH_ITEM, // special marker to render a focusable search icon
            "Inicio", "Películas", "Series", "TV en vivo", "Kids", "Mis Contenidos"
        )
        topNavAdapter = TopNavAdapter(categories) { /* onClick category - can filter rails later */ }

        binding.topNavRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = topNavAdapter
            descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
            isFocusable = true
            isFocusableInTouchMode = true
            clipToPadding = false
            clipChildren = false

            // Extra safeguard: consume DPAD_LEFT/RIGHT when focus at edges (unchanged with navbar height/padding adjustments)
            setOnKeyListener { v, keyCode, event ->
                if (event.action != android.view.KeyEvent.ACTION_DOWN) return@setOnKeyListener false
                val rv = v as RecyclerView
                val lm = rv.layoutManager as? LinearLayoutManager ?: return@setOnKeyListener false
                val first = lm.findFirstCompletelyVisibleItemPosition().takeIf { it != RecyclerView.NO_POSITION } ?: lm.findFirstVisibleItemPosition()
                val last = lm.findLastCompletelyVisibleItemPosition().takeIf { it != RecyclerView.NO_POSITION } ?: lm.findLastVisibleItemPosition()
                val total = rv.adapter?.itemCount ?: return@setOnKeyListener false
                return@setOnKeyListener when (keyCode) {
                    android.view.KeyEvent.KEYCODE_DPAD_LEFT -> first == 0
                    android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> last == total - 1
                    else -> false
                }
            }
        }

        // Ensure first visible item is initially focusable for D-pad (Search icon)
        binding.topNavRecycler.viewTreeObserver.addOnGlobalLayoutListener {
            if (binding.topNavRecycler.childCount > 0) {
                binding.topNavRecycler.getChildAt(0)?.requestFocus()
            }
        }
    }

    private fun setupBannerRail() {
        // Mock banner data using 4:1 images
        val banners = MockBannerData.generate(12)

        bannerAdapter = BannerAdapter(banners)
        binding.bannerRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = bannerAdapter
            descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
            isFocusable = true
            isFocusableInTouchMode = true

            // Do not allow neighbors to peek in; viewport shows only the focused page
            clipToPadding = false
            clipChildren = false

            // Snap one item per page
            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(this)

            // ItemDecoration to remove any inter-item spacing
            if (itemDecorationCount == 0) {
                addItemDecoration(object : RecyclerView.ItemDecoration() {
                    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                        // No gaps on either side, so only the snapped item is visible
                        outRect.set(0, 0, 0, 0)
                    }
                })
            }

            // Ensure the centered/snapped item gets focus when list gains focus
            setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    val lm = layoutManager as? LinearLayoutManager ?: return@setOnFocusChangeListener
                    val pos = lm.findFirstCompletelyVisibleItemPosition()
                        .takeIf { it != RecyclerView.NO_POSITION }
                        ?: lm.findFirstVisibleItemPosition()
                    if (pos != RecyclerView.NO_POSITION) {
                        getChildAt(0)?.requestFocus()
                    }
                }
            }
            // Up goes to top nav; down to rails is configured in XML via nextFocus*
        }
    }

    private fun setupContentRails() {
        // Mock data: a few rails with posters
        val mockRows = listOf(
            ContentRow(
                title = "Seguí viendo",
                items = MockContentData.generate(10)
            ),
            ContentRow(
                title = "Destacados",
                items = MockContentData.generate(12)
            ),
            ContentRow(
                title = "Recomendados para ti",
                items = MockContentData.generate(14)
            )
        )

        contentRowAdapter = ContentRowAdapter(mockRows)

        binding.railsRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = contentRowAdapter
            isFocusable = true
            isFocusableInTouchMode = true
            descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
            // Avoid clipping so headers are not hidden when near edges
            clipToPadding = false
            clipChildren = false

            // Also ensure the parent container does not clip
            (parent as? ViewGroup)?.let { p ->
                p.clipToPadding = false
                p.clipChildren = false
            }

            // Keep proper next focus to top bar when pressing UP on first row
            setOnFocusChangeListener { _, _ -> /* no-op, but could handle focus hint */ }
        }

        // Accessibility: group lists appropriately
        binding.railsRecycler.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/**
 * Simple mock models and generator
 */
data class ContentItem(
    val id: String,
    val title: String,
    val imageUrl: String
)

data class ContentRow(
    val title: String,
    val items: List<ContentItem>
)

object MockContentData {
    private val placeholders = listOf(
        "https://picsum.photos/seed/streamly1/640/360",
        "https://picsum.photos/seed/streamly2/640/360",
        "https://picsum.photos/seed/streamly3/640/360",
        "https://picsum.photos/seed/streamly4/640/360",
        "https://picsum.photos/seed/streamly5/640/360",
        "https://picsum.photos/seed/streamly6/640/360",
        "https://picsum.photos/seed/streamly7/640/360",
        "https://picsum.photos/seed/streamly8/640/360",
        "https://picsum.photos/seed/streamly9/640/360",
        "https://picsum.photos/seed/streamly10/640/360",
    )

    fun generate(count: Int): List<ContentItem> {
        return (0 until count).map {
            val idx = it % placeholders.size
            ContentItem(
                id = "id_$it",
                title = "Título $it",
                imageUrl = placeholders[idx]
            )
        }
    }
}
