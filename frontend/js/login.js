/**
 * login.js — Comprehensive Role-Based Authentication Controller
 */
document.addEventListener("DOMContentLoaded", function () {
    // 1. Theme initialization
    const savedTheme = localStorage.getItem('theme') || 'light';
    document.documentElement.setAttribute('data-theme', savedTheme);
    const themeBtn = document.getElementById('themeToggle');
    if (themeBtn) {
        themeBtn.textContent = savedTheme === 'dark' ? '☀️' : '🌓';
        themeBtn.addEventListener('click', function() {
            const cur = document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'light';
            const next = cur === 'dark' ? 'light' : 'dark';
            document.documentElement.setAttribute('data-theme', next);
            localStorage.setItem('theme', next);
            themeBtn.textContent = next === 'dark' ? '☀️' : '🌓';
        });
    }

    // 2. DOM Elements
    const loginForm = document.getElementById("loginForm");
    const emailInput = document.getElementById("email");
    const passwordInput = document.getElementById("password");
    const submitBtn = document.getElementById("submitBtn");
    const submitBtnText = document.getElementById("submitBtnText");
    const messageEl = document.getElementById("message");
    const togglePasswordBtn = document.getElementById("togglePasswordBtn");
    const autoFillBtn = document.getElementById("autoFillBtn");
    const roleContextDesc = document.getElementById("roleContextDesc");
    const roleIndicatorBadge = document.getElementById("roleIndicatorBadge");

    const roleTabs = {
        STUDENT: document.getElementById("tabStudent"),
        TEACHER: document.getElementById("tabTeacher"),
        ADMIN: document.getElementById("tabAdmin")
    };

    const roleConfigs = {
        STUDENT: {
            name: "Student",
            email: "student@university.edu",
            password: "password123",
            desc: "🎓 Student Portal: Access Live Classes & Coursework",
            badgeClass: "badge-success",
            badgeText: "Student Portal",
            buttonText: "Sign In to Student Portal",
            targetDashboard: "student-dashboard.html"
        },
        TEACHER: {
            name: "Faculty",
            email: "teacher@university.edu",
            password: "password123",
            desc: "👨‍🏫 Faculty Portal: Course Management & Grading",
            badgeClass: "badge-primary",
            badgeText: "Faculty Portal",
            buttonText: "Sign In to Faculty Portal",
            targetDashboard: "teacher-dashboard.html"
        },
        ADMIN: {
            name: "Admin",
            email: "admin@university.edu",
            password: "password123",
            desc: "🏛️ Admin Console: Campus Governance & Users",
            badgeClass: "badge-danger",
            badgeText: "Administrator Console",
            buttonText: "Sign In to Admin Console",
            targetDashboard: "admin-dashboard.html"
        }
    };

    let activeRole = "STUDENT";

    // 3. Role Tab Switching Handler
    function setActiveRole(role) {
        if (!roleConfigs[role]) return;
        activeRole = role;
        const config = roleConfigs[role];

        // Update tabs active visual state
        Object.entries(roleTabs).forEach(([r, tabEl]) => {
            if (!tabEl) return;
            const isMatch = r === role;
            tabEl.classList.toggle("active", isMatch);
            tabEl.setAttribute("aria-selected", isMatch ? "true" : "false");
        });

        // Update Context Banner
        if (roleContextDesc) roleContextDesc.innerHTML = config.desc;
        if (roleIndicatorBadge) {
            roleIndicatorBadge.textContent = config.badgeText;
            roleIndicatorBadge.className = "badge " + config.badgeClass;
        }
        if (submitBtnText) submitBtnText.textContent = config.buttonText;
        if (emailInput && !emailInput.value) {
            emailInput.placeholder = config.email;
        }
    }

    Object.entries(roleTabs).forEach(([role, tabEl]) => {
        if (tabEl) {
            tabEl.addEventListener("click", () => setActiveRole(role));
        }
    });

    // 4. Auto-Fill Demo Credentials button
    if (autoFillBtn) {
        autoFillBtn.addEventListener("click", function () {
            const config = roleConfigs[activeRole];
            if (emailInput) emailInput.value = config.email;
            if (passwordInput) passwordInput.value = config.password;
            if (window.showToast) {
                showToast(`Filled ${config.name} credentials (${config.email})`, "info");
            }
        });
    }

    // 5. Password Visibility Toggle
    if (togglePasswordBtn && passwordInput) {
        togglePasswordBtn.addEventListener("click", function () {
            const isPassword = passwordInput.getAttribute("type") === "password";
            passwordInput.setAttribute("type", isPassword ? "text" : "password");
            togglePasswordBtn.textContent = isPassword ? "🙈" : "👁️";
            togglePasswordBtn.setAttribute("aria-label", isPassword ? "Hide password" : "Show password");
        });
    }

    // 6. Check URL Parameters for presets
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get("reason") === "expired" && window.showToast) {
        showToast("Your session has expired. Please sign in again.", "warning");
    }
    if (urlParams.get("registered") === "true" && window.showToast) {
        showToast("Account created successfully! Please sign in.", "success");
    }
    const prefillEmail = urlParams.get("email");
    if (prefillEmail && emailInput) {
        emailInput.value = prefillEmail;
    }
    const prefillRole = urlParams.get("role");
    if (prefillRole && roleConfigs[prefillRole.toUpperCase()]) {
        setActiveRole(prefillRole.toUpperCase());
    } else {
        setActiveRole("STUDENT");
    }

    // 7. Form Submission Handler
    if (loginForm) {
        loginForm.addEventListener("submit", async function (event) {
            event.preventDefault();

            const email = emailInput ? emailInput.value.trim() : "";
            const password = passwordInput ? passwordInput.value : "";

            if (!email || !password) {
                if (window.showToast) showToast("Please provide both email and password.", "warning");
                return;
            }

            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<span class="spinner" style="width:16px;height:16px;border-width:2px;display:inline-block;margin-right:8px;"></span> Signing in...';
            }
            if (messageEl) messageEl.textContent = "";

            try {
                const textResponse = await api.post("/api/users/login", { email, password });
                const token = typeof textResponse === 'string' ? textResponse.trim().replace(/^"|"$/g, "") : textResponse.token || textResponse;

                localStorage.setItem("jwtToken", token);

                // Decode token payload
                let role = activeRole;
                let userName = email;
                try {
                    const base64Url = token.split(".")[1];
                    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
                    const payload = JSON.parse(atob(base64));
                    role = payload.role || activeRole;
                    userName = payload.name || payload.email || email;
                    localStorage.setItem("userRole", role);
                    localStorage.setItem("userName", userName);
                } catch (decodeErr) {
                    console.warn("Could not decode payload", decodeErr);
                }

                if (window.showToast) {
                    showToast(`Welcome back, ${userName}!`, "success");
                }

                let targetDashboard = "student-dashboard.html";
                if (role === "ADMIN") targetDashboard = "admin-dashboard.html";
                else if (role === "TEACHER") targetDashboard = "teacher-dashboard.html";

                setTimeout(() => {
                    window.location.href = targetDashboard;
                }, 600);

            } catch (error) {
                console.error("Login error:", error);
                const errMsg = error.message || "Invalid credentials. Please verify your email and password.";
                if (window.showToast) showToast(errMsg, "error");
                if (messageEl) {
                    messageEl.textContent = errMsg;
                    messageEl.style.color = "var(--danger)";
                }
                if (submitBtn) {
                    submitBtn.disabled = false;
                    const config = roleConfigs[activeRole];
                    submitBtn.innerHTML = `<span id="submitBtnText">${config.buttonText}</span> &rarr;`;
                }
            }
        });
    }

    // 8. Global helpers
    window.instantLogin = async function (email, pwd, target) {
        if (emailInput) emailInput.value = email;
        if (passwordInput) passwordInput.value = pwd;
        if (window.showToast) showToast(`Authenticating as ${email}...`, "info");
        try {
            const res = await api.post("/api/users/login", { email, password: pwd });
            const token = typeof res === 'string' ? res.trim().replace(/^"|"$/g, "") : (res.token || res);
            localStorage.setItem("jwtToken", token);
            const payload = JSON.parse(atob(token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")));
            localStorage.setItem("userRole", payload.role || "STUDENT");
            localStorage.setItem("userName", payload.name || email);
            if (window.showToast) showToast("Sign in successful! Redirecting...", "success");
            setTimeout(() => {
                window.location.href = target;
            }, 500);
        } catch (err) {
            if (window.showToast) showToast("Login failed: " + err.message, "error");
        }
    };

    window.forgotPasswordHelper = function () {
        alert("For demo security, please use the 1-Click Demo Persona accounts or contact the administrator (admin@university.edu).");
    };
});
