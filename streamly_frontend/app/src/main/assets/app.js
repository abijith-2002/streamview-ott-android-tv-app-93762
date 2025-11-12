(function () {
  // Basic keyboard focus styling for demonstration (non-functional TV nav placeholder)
  document.addEventListener('keydown', function (e) {
    if (e.key === 'Tab') {
      document.body.classList.add('kbd-nav');
    }
  });
})();
