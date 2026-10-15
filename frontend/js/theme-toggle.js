// theme-toggle.js — Dark/Light theme manager for public & auth pages
(function () {
  const savedTheme = localStorage.getItem('theme') || 'light';
  document.documentElement.setAttribute('data-theme', savedTheme);

  function initToggle() {
    const btn = document.getElementById('themeToggleBtn');
    if (!btn) return;

    function renderIcon(theme) {
      btn.textContent = theme === 'dark' ? '☀️' : '🌙';
      btn.setAttribute('title', theme === 'dark' ? 'Switch to Light Mode' : 'Switch to Dark Mode');
    }

    renderIcon(document.documentElement.getAttribute('data-theme') || 'light');

    btn.addEventListener('click', function () {
      const current = document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'light';
      const next = current === 'dark' ? 'light' : 'dark';
      document.documentElement.setAttribute('data-theme', next);
      localStorage.setItem('theme', next);
      renderIcon(next);
    });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initToggle);
  } else {
    initToggle();
  }
})();
