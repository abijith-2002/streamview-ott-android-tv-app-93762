# Home (Native Leanback)

This module replaces the WebView-based home with a native Android TV implementation.

- HomeActivity: FragmentActivity hosting HomeFragment.
- HomeFragment: BrowseSupportFragment with:
  - Center-like top navigation labels shown in title area (minimalist).
  - Full-width hero/banner via BackgroundManager.
  - Content rails using ListRow with ImageCardView Presenter.
  - D-pad focus and visual scaling come from Leanback.

Navigation is wired from SplashActivity to HomeActivity. Details/playback navigation hooks are left as TODOs to wire into the app's details/playback activities when added.
