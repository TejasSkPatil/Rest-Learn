document.addEventListener("DOMContentLoaded", function () {
    const loginForm = document.getElementById("loginForm");
    if (!loginForm) return;

    // Check if redirected due to expiry
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get("reason") === "expired" && window.showToast) {
        showToast("Your session has expired. Please sign in again.", "warning");
    }

    loginForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value;
        const messageEl = document.getElementById("message");
        const submitBtn = document.getElementById("submitBtn");

        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.textContent = "Signing in...";
        }

        try {
            const textResponse = await api.post("/api/users/login", { email, password });
            const token = typeof textResponse === 'string' ? textResponse.trim().replace(/^"|"$/g, "") : textResponse.token || textResponse;

            localStorage.setItem("jwtToken", token);

            // Decode token payload
            let role = "STUDENT";
            let userName = email;
            try {
                const base64Url = token.split(".")[1];
                const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
                const payload = JSON.parse(atob(base64));
                role = payload.role || "STUDENT";
                userName = payload.name || payload.email || email;
                localStorage.setItem("userRole", role);
                localStorage.setItem("userName", userName);
            } catch (decodeErr) {
                console.warn("Could not decode payload", decodeErr);
            }

            showToast("Welcome back! Redirecting...", "success");

            let targetDashboard = "student-dashboard.html";
            if (role === "ADMIN") targetDashboard = "admin-dashboard.html";
            else if (role === "TEACHER") targetDashboard = "teacher-dashboard.html";

            setTimeout(() => {
                window.location.href = targetDashboard;
            }, 600);

        } catch (error) {
            console.error("Login error:", error);
            showToast(error.message || "Invalid credentials. Please try again.", "error");
            if (messageEl) {
                messageEl.textContent = error.message;
                messageEl.style.color = "var(--danger)";
            }
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.textContent = "Sign In";
            }
        }
    });
});
