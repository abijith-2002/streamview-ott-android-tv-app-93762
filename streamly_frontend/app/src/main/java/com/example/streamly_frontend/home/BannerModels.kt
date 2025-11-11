package com.example.streamly_frontend.home

/**
 * PUBLIC_INTERFACE
 * Represents a 4:3 banner card item for the home banner rail.
 */
data class BannerItem(
    val id: String,
    val title: String,
    val imageUrl: String
)

/**
 * PUBLIC_INTERFACE
 * Generates mock 4:3 banner data for development and previews.
 */
object MockBannerData {
    private val placeholders4by3 = listOf(
        "https://picsum.photos/seed/banner1/800/600",
        "https://picsum.photos/seed/banner2/800/600",
        "https://picsum.photos/seed/banner3/800/600",
        "https://picsum.photos/seed/banner4/800/600",
        "https://picsum.photos/seed/banner5/800/600",
        "https://picsum.photos/seed/banner6/800/600",
        "https://picsum.photos/seed/banner7/800/600",
        "https://picsum.photos/seed/banner8/800/600",
        "https://picsum.photos/seed/banner9/800/600",
        "https://picsum.photos/seed/banner10/800/600"
    )

    // PUBLIC_INTERFACE
    fun generate(count: Int): List<BannerItem> {
        /** Generate a list of mock 4:3 banners. */
        return (0 until count).map {
            val idx = it % placeholders4by3.size
            BannerItem(
                id = "banner_$it",
                title = "Banner $it",
                imageUrl = placeholders4by3[idx]
            )
        }
    }
}
