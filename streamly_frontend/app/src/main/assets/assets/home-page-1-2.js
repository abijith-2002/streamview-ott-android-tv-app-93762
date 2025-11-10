(function () {
  'use strict';

  // Compute scale to fit 1920x1080 within viewport while keeping overscan padding
  function applyResponsiveScale() {
    var frame = document.getElementById('responsive-frame');
    var screen = document.getElementById('home-page-1-2');
    if (!frame || !screen) return;

    var cs = getComputedStyle(frame);
    var padLeft = parseFloat(cs.paddingLeft) || 0;
    var padRight = parseFloat(cs.paddingRight) || 0;
    var padTop = parseFloat(cs.paddingTop) || 0;
    var padBottom = parseFloat(cs.paddingBottom) || 0;

    var vw = window.innerWidth - (padLeft + padRight);
    var vh = window.innerHeight - (padTop + padBottom);
    var scale = Math.min(vw / 1920, vh / 1080);
    screen.style.transform = 'scale(' + scale + ')';
  }

  function isActivateKey(e) {
    return e.key === 'Enter' || e.key === ' ' || e.key === 'DPAD_CENTER';
  }

  function smoothCenterIntoView(el) {
    try {
      el.scrollIntoView({ block: 'nearest', inline: 'center', behavior: 'smooth' });
    } catch (e) {
      // Fallback for some WebView implementations
      el.scrollIntoView();
    }
  }

  function attachFocusBehavior() {
    // Scale-up animation is handled by CSS; here we keep navigation centered
    var focusables = document.querySelectorAll('#home-page-1-2 a, #home-page-1-2 [role="button"], #home-page-1-2 [tabindex="0"]');
    focusables.forEach(function (el) {
      el.addEventListener('focus', function () {
        // Try to center focused element if inside scrolling containers (e.g., nav-center)
        var parent = el.parentElement;
        if (parent && parent.classList.contains('nav-center')) {
          // Ensure it's visible without breaking overall centering
          smoothCenterIntoView(el);
        }
      }, { passive: true });
      el.addEventListener('keydown', function (e) {
        if (isActivateKey(e) && (el.getAttribute('role') === 'button' || el.tagName === 'A')) {
          e.preventDefault();
          el.click();
        }
      });
    });
  }

  function prefetchImages() {
    // Prefetch thumbnails that are likely to be focused soon
    var sources = [
      'assets/figmaimages/figma_image_1_8.png',
      'assets/figmaimages/figma_image_1_154.png'
    ];
    sources.forEach(function (src) {
      var img = new Image();
      img.decoding = 'async';
      img.loading = 'lazy';
      img.src = src;
    });
  }

  // PUBLIC_INTERFACE
  function initHomePageInteractions() {
    /** Initialize interactions and responsive scaling for the Home Page.
     * - TV-friendly focus ring via CSS classes (no inline styling)
     * - Keyboard activation for buttons/links (Enter/Space/DPAD Center)
     * - Responsive scaler preserving composition with safe-areas
     * - DPAD focus: scale-up on focus via CSS and smooth center into view
     * - Prefetch a couple of images to reduce focus-latency
     */
    applyResponsiveScale();
    window.addEventListener('resize', applyResponsiveScale);

    document.documentElement.style.overflowX = 'hidden';
    document.body.style.overflowX = 'hidden';

    attachFocusBehavior();
    prefetchImages();

    var playA = document.getElementById('tvA-play-wrap');
    if (playA) {
      playA.addEventListener('click', function () {
        console.log('Play Marca Claro Radio clicked');
      });
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initHomePageInteractions);
  } else {
    initHomePageInteractions();
  }
})();
