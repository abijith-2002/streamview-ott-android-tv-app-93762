(function () {
  'use strict';

  var DESIGN_W = 1920;
  var DESIGN_H = 1080;

  // PUBLIC_INTERFACE
  function fitContainScale() {
    /** Compute fit-contain scale within overscan-safe viewport and apply transform. */
    try {
      // Ensure no scroll at runtime as well
      document.documentElement.style.overflow = 'hidden';
      document.body.style.overflow = 'hidden';

      var viewport = document.querySelector('.ci-viewport');
      var canvas = document.querySelector('.ci-canvas');

      if (!viewport || !canvas) {
        return;
      }

      // Read overscan from CSS var if present; default to 0.06
      var overscan = 0.06;
      try {
        var styles = getComputedStyle(viewport);
        var raw = styles.getPropertyValue('--overscan').trim();
        if (raw) {
          var parsed = parseFloat(raw);
          if (!isNaN(parsed)) {
            overscan = parsed;
          }
        }
      } catch (e) {
        // ignore and use default
      }

      // Compute available size by subtracting padding used as overscan-safe margin
      var vw = window.innerWidth;
      var vh = window.innerHeight;

      var padV = vh * overscan; // top and bottom each
      var padH = vw * overscan; // left and right each
      var availW = Math.max(0, vw - 2 * padH);
      var availH = Math.max(0, vh - 2 * padV);

      var scale = Math.min(availW / DESIGN_W, availH / DESIGN_H);
      if (!isFinite(scale) || scale <= 0) {
        scale = 1;
      }

      // Apply translate3d(0,0,0) for GPU compositing and set transform-origin to top-left
      canvas.style.transformOrigin = '0 0';
      canvas.style.transform = 'translate3d(0,0,0) scale(' + scale + ')';

      // Explicitly set size to the design dimensions
      canvas.style.width = DESIGN_W + 'px';
      canvas.style.height = DESIGN_H + 'px';

      // Reduced motion: avoid transitions
      var reduceMotion = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
      if (reduceMotion) {
        canvas.style.transition = 'none';
      } else {
        canvas.style.transition = 'transform 120ms linear';
      }

      // Clamp outline-offset for focused element to keep ring inside canvas
      try {
        var focused = document.activeElement;
        if (focused && focused !== document.body) {
          var cs = getComputedStyle(focused);
          if (parseFloat(cs.outlineOffset) > 0) {
            focused.style.outlineOffset = '0px';
          }
        }
      } catch (e) { /* ignore */ }
    } catch (err) {
      // swallow errors to avoid breaking UI
    }
  }

  function initPopulation() {
    // Populate dynamic fields from payload if provided (preserve prior behavior)
    function parsePayload() {
      if (typeof window.__ANDROID_BRIDGE_PAYLOAD__ === 'string' && window.__ANDROID_BRIDGE_PAYLOAD__.length > 0) {
        try { return JSON.parse(window.__ANDROID_BRIDGE_PAYLOAD__); } catch (e) {}
      }
      try {
        var q = new URLSearchParams(window.location.search);
        var p = q.get('payload');
        if (p) return JSON.parse(decodeURIComponent(p));
      } catch (e) {}
      return {};
    }

    function text(el, value) { if (el) el.textContent = value || el.textContent || ''; }
    function setImage(selectorOrEl, src) {
      try {
        var el = (typeof selectorOrEl === 'string') ? document.querySelector(selectorOrEl) : selectorOrEl;
        if (el && src) el.setAttribute('src', src);
      } catch (e) {}
    }

    var data = parsePayload();

    // Title
    var h1 = document.querySelector('h1.typo-32');
    text(h1, data.title || h1 && h1.textContent || 'Contenido');

    // Synopsis
    var p = document.querySelector('section[aria-label="Panel de metadatos"] p.typo-37');
    text(p, data.synopsis || p && p.textContent || 'Descripción no disponible.');

    // Background artwork (first absolute img in main)
    var bg = document.querySelector('main img.abs.img');
    if (data.image) setImage(bg, data.image);

    // Runtime
    var runtimeEl = Array.from(document.querySelectorAll('section[aria-label="Panel de metadatos"] .typo-33'))
      .find(function (n) { return n.getAttribute('aria-label') === 'Duración'; });
    if (runtimeEl && data.runtime) text(runtimeEl, data.runtime);

    // Genres/tags
    var genresEl = Array.from(document.querySelectorAll('section[aria-label="Panel de metadatos"] .typo-33'))
      .find(function (n) { return n.getAttribute('aria-label') === 'Géneros'; });
    if (genresEl && Array.isArray(data.tags) && data.tags.length) text(genresEl, data.tags.join(', '));

    // Initial focus
    setTimeout(function () {
      var first = document.querySelector('[aria-label="Reanudar"]') ||
                  document.querySelector('[aria-label="Reanudar desde el inicio"]') ||
                  document.querySelector('.panel-button');
      if (first && typeof first.focus === 'function') first.focus();
    }, 120);
  }

  function init() {
    // Enforce no-scroll immediately
    document.documentElement.style.overflow = 'hidden';
    document.body.style.overflow = 'hidden';

    fitContainScale();
    initPopulation();

    // Recompute on resize and orientation change
    window.addEventListener('resize', fitContainScale, { passive: true });
    window.addEventListener('orientationchange', fitContainScale, { passive: true });

    // Some Android TV WebViews can fire a late layout; schedule microtask and raf
    setTimeout(fitContainScale, 0);
    if (typeof requestAnimationFrame === 'function') {
      requestAnimationFrame(fitContainScale);
    }

    // Clock/date updates every 30s (preserve existing behavior)
    function pad(n){return String(n).padStart(2,'0');}
    function updateClock(){
      var now=new Date();
      var c=document.getElementById('ci-clock');
      var d=document.getElementById('ci-date');
      if(c){ c.textContent=pad(now.getHours())+':'+pad(now.getMinutes()); }
      if(d){
        try{
          var fmt=new Intl.DateTimeFormat('es-ES',{day:'numeric',month:'short'});
          var txt=fmt.format(now);
          d.textContent=txt.endsWith('.')?txt:(txt+'.');
        }catch(e){
          var months=['ene.','feb.','mar.','abr.','may.','jun.','jul.','ago.','sep.','oct.','nov.','dic.'];
          d.textContent=now.getDate()+' '+months[now.getMonth()];
        }
      }
    }
    updateClock();
    var timer = setInterval(updateClock, 30000);
    window.addEventListener('pagehide', function(){ clearInterval(timer); }, { once: true });
  }

  document.addEventListener('DOMContentLoaded', init);
  if (document.readyState === 'interactive' || document.readyState === 'complete') {
    init();
  }

  // Expose for debugging if needed
  window.ciFitContainScale = fitContainScale;
})();
