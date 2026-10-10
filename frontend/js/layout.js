// layout.js — Shared layout injector + Route Guard + Theme Manager
(function() {
  // 1. Initialize Theme early
  const savedTheme = localStorage.getItem('theme') || 'light';
  document.documentElement.setAttribute('data-theme', savedTheme);

  /* ── Route Guard ────────────────────────────────────────────────── */
  window.requireAuth = function(allowedRoles) {
    var token = localStorage.getItem("jwtToken") || sessionStorage.getItem("jwtToken") || localStorage.getItem("token");
    if (!token) { window.location.href = "login.html"; return false; }
    try {
      var b64 = token.split(".")[1].replace(/-/g,"+").replace(/_/g,"/");
      var payload = JSON.parse(atob(b64));
      if (payload.exp && Date.now()/1000 > payload.exp) {
        ["jwtToken","token","userRole","userName"].forEach(function(k){ localStorage.removeItem(k); });
        sessionStorage.removeItem("jwtToken");
        window.location.href = "login.html?reason=expired"; return false;
      }
      if (allowedRoles && allowedRoles.length) {
        var role = payload.role || "";
        if (allowedRoles.indexOf(role) === -1) {
          var map = {ADMIN:"admin-dashboard.html",TEACHER:"teacher-dashboard.html",STUDENT:"student-dashboard.html"};
          window.location.href = map[role] || "login.html"; return false;
        }
      }
      window.__authPayload = payload;
      return true;
    } catch(e) {
      ["jwtToken","token","userRole","userName"].forEach(function(k){ localStorage.removeItem(k); });
      sessionStorage.removeItem("jwtToken");
      window.location.href = "login.html"; return false;
    }
  };

  /* ── Layout Injection ───────────────────────────────────────────── */
  document.addEventListener("DOMContentLoaded", function() {
    var navEl  = document.getElementById("navbar");
    var sideEl = document.getElementById("sidebar");
    if (navEl) {
      fetch("components/navbar.html")
        .then(function(r){ return r.text(); })
        .then(function(html){ navEl.innerHTML = html; initNavbar(); })
        .catch(function(e){ console.error("Failed to load navbar:", e); });
    }
    if (sideEl) {
      fetch("components/sidebar.html")
        .then(function(r){ return r.text(); })
        .then(function(html){ sideEl.innerHTML = html; initSidebar(); })
        .catch(function(e){ console.error("Failed to load sidebar:", e); });
    }
  });

  /* ── Navbar Initialiser ─────────────────────────────────────────── */
  function initNavbar() {

    // Notification Dropdown Logic
    var bellBtn = document.getElementById("notifBellBtn");
    var notifMenu = document.getElementById("notifMenu");
    var notifBadge = document.getElementById("notifBadge");
    var notifList = document.getElementById("notifList");
    var markAllBtn = document.getElementById("markAllReadBtn");

    if (bellBtn && notifMenu) {
      bellBtn.addEventListener("click", function(e) {
        e.stopPropagation();
        notifMenu.style.display = notifMenu.style.display === "none" ? "block" : "none";
        loadNotifications();
      });
      document.addEventListener("click", function(e) {
        if (!notifMenu.contains(e.target) && e.target !== bellBtn) {
          notifMenu.style.display = "none";
        }
      });
    }

    async function loadNotifications() {
      if (!window.api || !token) return;
      try {
        var notifs = await window.api.get("/api/notifications");
        if (notifs && notifs.length) {
          if (notifBadge) {
            notifBadge.textContent = notifs.length;
            notifBadge.style.display = "inline-block";
          }
          if (notifList) {
            notifList.innerHTML = notifs.slice(0, 5).map(function(n) {
              return '<div style="padding:6px; border-bottom:1px solid var(--border-color); font-size:0.8rem;"><strong>' + (n.title || 'Alert') + '</strong><p style="color:var(--text-muted); margin:2px 0;">' + (n.message || '') + '</p></div>';
            }).join("");
          }
        }
      } catch(e) {}
    }
    if (token) loadNotifications();

    var token     = localStorage.getItem("jwtToken") || sessionStorage.getItem("jwtToken") || localStorage.getItem("token");
    var authEl    = document.getElementById("authButtons");
    var greetEl   = document.getElementById("userGreeting");
    var nameEl    = document.getElementById("userName");
    var roleEl    = document.getElementById("userRole");
    var logoutBtn = document.getElementById("logoutBtn");
    var themeBtn  = document.getElementById("themeToggle");

    // Theme Toggle Handler
    if (themeBtn) {
      themeBtn.addEventListener("click", function() {
        var current = document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'light';
        var next = current === 'dark' ? 'light' : 'dark';
        document.documentElement.setAttribute('data-theme', next);
        localStorage.setItem('theme', next);
        themeBtn.textContent = next === 'dark' ? '☀️' : '🌓';
      });
      themeBtn.textContent = (localStorage.getItem('theme') === 'dark') ? '☀️' : '🌓';
    }

    if (!token) {
      if (authEl) authEl.innerHTML =
        '<a href="login.html" class="btn btn-outline btn-sm">Sign In</a> ' +
        '<a href="register.html" class="btn btn-primary btn-sm">Sign Up</a>';
      if (greetEl)   greetEl.style.display  = "none";
      if (logoutBtn) logoutBtn.style.display = "none";
      var sb = document.querySelector(".sidebar");
      if (sb) sb.style.display = "none";
      return;
    }

    try {
      var b64     = token.split(".")[1].replace(/-/g,"+").replace(/_/g,"/");
      var payload = JSON.parse(atob(b64));
      if (nameEl) nameEl.textContent = payload.name || payload.email || "User";
      if (roleEl && payload.role) {
        roleEl.textContent = payload.role;
        roleEl.className   = "badge " + (payload.role === 'ADMIN' ? 'badge-danger' : payload.role === 'TEACHER' ? 'badge-primary' : 'badge-success');
      }
      if (greetEl)   greetEl.style.display  = "inline-flex";
      if (logoutBtn) logoutBtn.style.display = "inline-flex";
      if (authEl)    authEl.innerHTML        = "";
    } catch(e) { console.error("initNavbar decode error", e); }

    if (logoutBtn) {
      logoutBtn.addEventListener("click", function() {
        ["jwtToken","token","userRole","userName"].forEach(function(k){ localStorage.removeItem(k); });
        sessionStorage.removeItem("jwtToken");
        window.location.href = "login.html";
      });
    }
  }

  /* ── Sidebar Initialiser ────────────────────────────────────────── */
  function initSidebar() {
    var token = localStorage.getItem("jwtToken") || sessionStorage.getItem("jwtToken") || localStorage.getItem("token");
    if (!token) { var s = document.querySelector(".sidebar"); if (s) s.style.display = "none"; return; }
    var role = "";
    try { role = JSON.parse(atob(token.split(".")[1].replace(/-/g,"+").replace(/_/g,"/"))).role || ""; } catch(e){ return; }
    var navLinks = document.querySelector(".sidebar-nav");
    if (!navLinks) return;
    var allItems = {
      ADMIN:[
        {l:"📊 Dashboard",h:"admin-dashboard.html"},{l:"👥 Users",h:"admin-users.html"},
        {l:"🏢 Departments",h:"admin-departments.html"},{l:"📅 Sessions",h:"admin-sessions.html"},
        {l:"🕒 Timetable",h:"admin-timetable.html"},{l:"⚙️ Attendance Rules",h:"admin-attendance-rules.html"},
        {l:"📈 Reports",h:"admin-reports.html"},{l:"📜 Audit Log",h:"admin-audit-log.html"}
      ],
      TEACHER:[
        {l:"📊 Dashboard",h:"teacher-dashboard.html"},{l:"📚 My Courses",h:"teacher-courses.html"},
        {l:"✅ Attendance",h:"teacher-attendance.html"}
      ],
      STUDENT:[
        {l:"📊 Dashboard",h:"student-dashboard.html"},{l:"📚 My Courses",h:"student-dashboard.html"},
        {l:"➕ Join Course",h:"student-join-course.html"},{l:"✅ Attendance",h:"student-attendance.html"}
      ]
    };
    var items = allItems[role] || [];
    var cur   = window.location.pathname.split("/").pop();
    var html  = "";
    items.forEach(function(it){
      var active = cur === it.h ? ' class="active"' : "";
      html += '<li><a href="' + it.h + '"' + active + '>' + it.l + '</a></li>';
    });
    navLinks.innerHTML = html;
  }
})();
