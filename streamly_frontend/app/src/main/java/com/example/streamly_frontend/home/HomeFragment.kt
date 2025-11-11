package com.example.streamly_frontend.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.streamly_frontend.databinding.FragmentHomeBinding

/**
 * PUBLIC_INTERFACE
 * HomeFragment is the main Android TV home screen using a single vertical RecyclerView:
 * sections: [Title+Navbar, Banner pager, Content rails...].
 *
 * The brand and top navbar now scroll with the list (no fixed pinning).
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var sectionsAdapter: HomeSectionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupSectionsList()
    }

    private fun setupSectionsList() {
        // Header categories for top navigation (first section)
        val categories = listOf(
            TopNavAdapter.SEARCH_ITEM,
            "Inicio", "Películas", "Series", "TV en vivo", "Kids", "Mis Contenidos"
        )

        // Banner and rails
        val banners = MockBannerData.generate(12)
        val rows = listOf(
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

        val sections = mutableListOf<SectionItem>()
        sections.add(SectionItem.HeaderSection(categories))
        sections.add(SectionItem.BannerSection(banners))
        rows.forEach { sections.add(SectionItem.ContentRowSection(it)) }

        sectionsAdapter = HomeSectionsAdapter(sections)

        binding.railsRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = sectionsAdapter
            isFocusable = true
            isFocusableInTouchMode = true
            descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
            clipToPadding = false
            clipChildren = false
            (parent as? ViewGroup)?.let { p ->
                p.clipToPadding = false
                p.clipChildren = false
            }
        }
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
