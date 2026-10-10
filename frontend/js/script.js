/**
 * script.js — Account Registration & Form Validation Controller
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
    const registerForm = document.getElementById("registerForm");
    if (!registerForm) return;

    const roleInput = document.getElementById("role");
    const roleCardStudent = document.getElementById("roleCardStudent");
    const roleCardTeacher = document.getElementById("roleCardTeacher");
    const nameInput = document.getElementById("name");
    const emailInput = document.getElementById("email");
    const departmentInput = document.getElementById("department");
    const passwordInput = document.getElementById("password");
    const confirmPasswordInput = document.getElementById("confirmPassword");
    const registerBtn = document.getElementById("registerBtn");
    const registerBtnText = document.getElementById("registerBtnText");
    const messageEl = document.getElementById("message");
    const pwdMatchStatus = document.getElementById("pwdMatchStatus");
    const toggleRegPasswordBtn = document.getElementById("toggleRegPasswordBtn");

    // 3. Role Selection Handler
    function selectRole(role) {
        if (!roleInput) return;
        roleInput.value = role;

        if (roleCardStudent) roleCardStudent.classList.toggle("selected", role === "STUDENT");
        if (roleCardTeacher) roleCardTeacher.classList.toggle("selected", role === "TEACHER");

        if (registerBtnText) {
            registerBtnText.textContent = role === "STUDENT" ? "Create Student Account" : "Create Faculty Account";
        }
    }

    if (roleCardStudent) {
        roleCardStudent.addEventListener("click", () => selectRole("STUDENT"));
    }
    if (roleCardTeacher) {
        roleCardTeacher.addEventListener("click", () => selectRole("TEACHER"));
    }

    // 4. Password Toggle
    if (toggleRegPasswordBtn && passwordInput) {
        toggleRegPasswordBtn.addEventListener("click", function () {
            const isPassword = passwordInput.getAttribute("type") === "password";
            passwordInput.setAttribute("type", isPassword ? "text" : "password");
            if (confirmPasswordInput) {
                confirmPasswordInput.setAttribute("type", isPassword ? "text" : "password");
            }
            toggleRegPasswordBtn.textContent = isPassword ? "🙈" : "👁️";
        });
    }

    // 5. Password Match Live Validation
    function checkPasswordMatch() {
        if (!confirmPasswordInput || !passwordInput || !pwdMatchStatus) return;
        const p1 = passwordInput.value;
        const p2 = confirmPasswordInput.value;

        if (!p2) {
            pwdMatchStatus.textContent = "";
            return;
        }

        if (p1 === p2) {
            pwdMatchStatus.textContent = "✓ Passwords match";
            pwdMatchStatus.style.color = "var(--success)";
        } else {
            pwdMatchStatus.textContent = "✗ Passwords do not match";
            pwdMatchStatus.style.color = "var(--danger)";
        }
    }

    if (passwordInput && confirmPasswordInput) {
        passwordInput.addEventListener("input", checkPasswordMatch);
        confirmPasswordInput.addEventListener("input", checkPasswordMatch);
    }

    // 6. Form Submission
    registerForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const name = nameInput ? nameInput.value.trim() : "";
        const email = emailInput ? emailInput.value.trim() : "";
        const password = passwordInput ? passwordInput.value : "";
        const confirmPassword = confirmPasswordInput ? confirmPasswordInput.value : "";
        const role = roleInput ? roleInput.value : "STUDENT";
        const department = departmentInput ? departmentInput.value : "General";

        if (!name || !email || !password) {
            if (window.showToast) showToast("Please fill in all required fields.", "warning");
            return;
        }

        if (password.length < 6) {
            if (window.showToast) showToast("Password must be at least 6 characters.", "warning");
            return;
        }

        if (password !== confirmPassword) {
            if (window.showToast) showToast("Passwords do not match. Please re-enter.", "error");
            if (confirmPasswordInput) confirmPasswordInput.focus();
            return;
        }

        if (registerBtn) {
            registerBtn.disabled = true;
            registerBtn.innerHTML = '<span class="spinner" style="width:16px;height:16px;border-width:2px;display:inline-block;margin-right:8px;"></span> Creating Account...';
        }
        if (messageEl) messageEl.textContent = "";

        const payload = {
            name,
            email,
            password,
            role,
            department
        };

        try {
            const response = await fetch("/api/users/register", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(payload)
            });

            if (response.ok) {
                if (window.showToast) {
                    showToast("Account created successfully! Redirecting to sign in...", "success");
                }
                if (messageEl) {
                    messageEl.style.color = "var(--success)";
                    messageEl.textContent = "Account created! Redirecting to portal sign in...";
                }

                setTimeout(function () {
                    window.location.href = `login.html?registered=true&email=${encodeURIComponent(email)}&role=${encodeURIComponent(role)}`;
                }, 900);
            } else {
                const errData = await response.text();
                let errMsg = "Registration failed. Email may already be in use.";
                try {
                    const parsed = JSON.parse(errData);
                    errMsg = parsed.message || parsed.error || errMsg;
                } catch (e) {
                    if (errData && errData.trim()) errMsg = errData;
                }

                if (window.showToast) showToast(errMsg, "error");
                if (messageEl) {
                    messageEl.style.color = "var(--danger)";
                    messageEl.textContent = errMsg;
                }
                if (registerBtn) {
                    registerBtn.disabled = false;
                    registerBtn.innerHTML = `<span id="registerBtnText">${role === "STUDENT" ? "Create Student Account" : "Create Faculty Account"}</span> &rarr;`;
                }
            }
        } catch (error) {
            console.error("Register error:", error);
            const netErr = "Could not connect to the campus server. Please try again.";
            if (window.showToast) showToast(netErr, "error");
            if (messageEl) {
                messageEl.style.color = "var(--danger)";
                messageEl.textContent = netErr;
            }
            if (registerBtn) {
                registerBtn.disabled = false;
                registerBtn.innerHTML = `<span id="registerBtnText">${role === "STUDENT" ? "Create Student Account" : "Create Faculty Account"}</span> &rarr;`;
            }
        }
    });
});
