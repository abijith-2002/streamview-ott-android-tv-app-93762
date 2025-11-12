(function () {
  // Read payload from query or Android bridge
  function parsePayload() {
    // 1) Bridge variable set by inline bootstrap
    if (typeof window.__ANDROID_BRIDGE_PAYLOAD__ === 'string' && window.__ANDROID_BRIDGE_PAYLOAD__.length > 0) {
      try { return JSON.parse(window.__ANDROID_BRIDGE_PAYLOAD__); } catch {}
    }
    // 2) Query param ?payload=<encoded JSON>
    try {
      var q = new URLSearchParams(window.location.search);
      var p = q.get('payload');
      if (p) {
        return JSON.parse(decodeURIComponent(p));
      }
    } catch {}
    return {};
  }

  function text(el, value) {
    if (!el) return;
    el.textContent = value || el.textContent || '';
  }

  function setImage(selectorOrEl, src) {
    try {
      var el = (typeof selectorOrEl === 'string') ? document.querySelector(selectorOrEl) : selectorOrEl;
      if (el && src) el.setAttribute('src', src);
    } catch {}
  }

  function populate(data) {
    // Map known placeholders from the static HTML
    // Title h1 (Gladiador II)
    var h1 = document.querySelector('h1.typo-32');
    text(h1, data.title || 'Contenido');

    // Original title (first item in info row) - if tags contain original title we could patch; with minimal approach, only synopsis/tags/runtime.
    // Synopsis paragraph
    var p = document.querySelector('section[aria-label="Panel de metadatos"] p.typo-37');
    text(p, data.synopsis || 'Descripción no disponible.');

    // Background artwork image at top of DOM
    var bg = document.querySelector('main img.abs.img');
    if (data.image) {
      setImage(bg, data.image);
    }

    // Runtime (find the element with aria-label="Duración")
    var runtimeEl = Array.from(document.querySelectorAll('section[aria-label="Panel de metadatos"] .typo-33'))
      .find(function (n) { return n.getAttribute('aria-label') === 'Duración'; });
    if (runtimeEl && data.runtime) {
      text(runtimeEl, data.runtime);
    }

    // Genres/tags (aria-label="Géneros")
    var genresEl = Array.from(document.querySelectorAll('section[aria-label="Panel de metadatos"] .typo-33'))
      .find(function (n) { return n.getAttribute('aria-label') === 'Géneros'; });
    if (genresEl && Array.isArray(data.tags) && data.tags.length) {
      text(genresEl, data.tags.join(', '));
    }

    // Initial focus
    setTimeout(function () {
      var first = document.querySelector('[aria-label="Reanudar"]') ||
                  document.querySelector('[aria-label="Reanudar desde el inicio"]') ||
                  document.querySelector('.panel-button');
      if (first && typeof first.focus === 'function') first.focus();
    }, 120);
  }

  var data = parsePayload();
  document.addEventListener('DOMContentLoaded', function () { populate(data); });

  // Fallback clock/date logic (kept minimal)
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
      }catch{
        var months=['ene.','feb.','mar.','abr.','may.','jun.','jul.','ago.','sep.','oct.','nov.','dic.'];
        d.textContent=now.getDate()+' '+months[now.getMonth()];
      }
    }
  }
  updateClock();
  setInterval(updateClock, 30000);
})();
