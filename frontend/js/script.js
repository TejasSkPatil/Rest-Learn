document.addEventListener("DOMContentLoaded", function () {
    const registerForm = document.getElementById("registerForm");
    if (!registerForm) return;

    registerForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const name = document.getElementById("name").value.trim();
        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value;
        const role = document.getElementById("role").value;
        const messageEl = document.getElementById("message");
        const registerBtn = document.getElementById("registerBtn");

        if (!role) {
            messageEl.style.color = "#dc3545";
            messageEl.textContent = "Please select a role (Student or Teacher).";
            return;
        }

        const user = { name, email, password, role };

        messageEl.style.color = "#555";
        messageEl.textContent = "Creating account...";
        if (registerBtn) registerBtn.disabled = true;

        try {
            const response = await fetch("http://localhost:8080/api/users/register", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(user)
            });

            if (response.ok) {
                messageEl.style.color = "#28a745";
                messageEl.textContent = "Account created successfully! Redirecting to login...";
                registerForm.reset();

                setTimeout(function () {
                    window.location.href = "login.html";
                }, 1200);
            } else {
                const errData = await response.text();
                messageEl.style.color = "#dc3545";
                messageEl.textContent = errData || "Registration failed. Email may already be in use.";
                if (registerBtn) registerBtn.disabled = false;
            }

        } catch (error) {
            console.error("Register error:", error);
            messageEl.style.color = "#dc3545";
            messageEl.textContent = "Could not connect to backend server. Make sure it is running.";
            if (registerBtn) registerBtn.disabled = false;
        }
    });
});