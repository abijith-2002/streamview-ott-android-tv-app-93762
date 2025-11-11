package com.example.streamly_frontend.home

/**
 * PUBLIC_INTERFACE
 * Represents a 4:1 banner card item for the home banner rail.
 */
data class BannerItem(
    val id: String,
    val title: String,
    val imageUrl: String
)

/**
 * PUBLIC_INTERFACE
 * Generates mock 4:1 banner data for development and previews.
 */
object MockBannerData {
    private val placeholders4by1 = listOf(
        "https://picsum.photos/seed/banner1/1600/400",
        "https://picsum.photos/seed/banner2/1600/400",
        "https://picsum.photos/seed/banner3/1600/400",
        "https://picsum.photos/seed/banner4/1600/400",
        "https://picsum.photos/seed/banner5/1600/400",
        "https://picsum.photos/seed/banner6/1600/400",
        "https://picsum.photos/seed/banner7/1600/400",
        "https://picsum.photos/seed/banner8/1600/400",
        "https://picsum.photos/seed/banner9/1600/400",
        "https://picsum.photos/seed/banner10/1600/400"
    )

    // PUBLIC_INTERFACE
    fun generate(count: Int): List<BannerItem> {
        /** Generate a list of mock 4:1 banners. */
        return (0 until count).map {
            val idx = it % placeholders4by1.size
            BannerItem(
                id = "banner_$it",
                title = "Banner $it",
                imageUrl = placeholders4by1[idx]
            )
        }
    }
}
