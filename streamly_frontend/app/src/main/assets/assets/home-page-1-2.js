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

  // PUBLIC_INTERFACE
  function initHomePageInteractions() {
    /** Initialize interactions and responsive scaling for the Home Page.
     * - TV-friendly focus ring via CSS classes (no inline styling)
     * - Keyboard activation for buttons/links (Enter/Space/DPAD Center)
     * - Responsive scaler preserving composition with safe-areas
     */
    applyResponsiveScale();
    window.addEventListener('resize', applyResponsiveScale);

    // Prevent accidental horizontal scrolling due to transforms or focus jumps
    document.documentElement.style.overflowX = 'hidden';
    document.body.style.overflowX = 'hidden';

    // Ensure focus outline visible using CSS class; no inline style needed
    var focusables = document.querySelectorAll('#home-page-1-2 [role="link"], #home-page-1-2 [role="button"], #home-page-1-2 a, #home-page-1-2 [tabindex="0"]');
    focusables.forEach(function (el) {
      el.addEventListener('keydown', function (e) {
        var isActivateKey = (e.key === 'Enter' || e.key === ' ' || e.key === 'DPAD_CENTER');
        var isActionable = (el.getAttribute('role') === 'button' || el.tagName === 'A');
        if (isActivateKey && isActionable) {
          e.preventDefault();
          el.click();
        }
      });
    });

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
