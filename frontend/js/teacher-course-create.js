// teacher-course-create.js — Interactive Course Studio Creator with Live Student Preview
document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("createCourseForm");
    const nameInput = document.getElementById("courseName");
    const codeInput = document.getElementById("courseCode");
    const deptSelect = document.getElementById("department");
    const semSelect = document.getElementById("semester");
    const descTextarea = document.getElementById("description");
    const charCount = document.getElementById("charCount");

    // Preview Elements
    const previewTitle = document.getElementById("previewTitle");
    const previewCodeBadge = document.getElementById("previewCodeBadge");
    const previewSemesterBadge = document.getElementById("previewSemesterBadge");
    const previewDeptTag = document.getElementById("previewDeptTag");
    const previewDesc = document.getElementById("previewDesc");
    const previewTeacher = document.getElementById("previewTeacher");
    const previewBanner = document.getElementById("previewCardBanner");

    // Submission & Success Elements
    const submitBtn = document.getElementById("submitBtn");
    const btnText = document.getElementById("btnText");
    const btnSpinner = document.getElementById("btnSpinner");
    const successPanel = document.getElementById("successPanel");
    const studioContainer = document.getElementById("createStudioContainer");
    const generatedCodeDisplay = document.getElementById("generatedCodeDisplay");
    const copyCodeBtn = document.getElementById("copyCodeBtn");
    const goToCourseDetailBtn = document.getElementById("goToCourseDetailBtn");

    let currentEnrollmentCode = "";

    // Set instructor name from logged in user if available
    const userName = localStorage.getItem("userName") || sessionStorage.getItem("userName") || "Professor";
    if (previewTeacher) {
        previewTeacher.textContent = userName;
    }

    // Color Swatches
    window.selectCardColor = function(colorHex, name) {
        document.querySelectorAll(".theme-swatch").forEach(el => {
            el.classList.toggle("active", el.getAttribute("data-color") === name);
        });
        if (previewBanner) {
            previewBanner.style.background = `linear-gradient(135deg, ${colorHex} 0%, #1e1b4b 100%)`;
        }
    };

    // 1. Live Preview Updating
    function updatePreview() {
        const title = nameInput ? nameInput.value.trim() : "";
        const code = codeInput ? codeInput.value.trim().toUpperCase() : "";
        const dept = deptSelect ? deptSelect.value : "";
        const sem = semSelect ? semSelect.value : "";
        const desc = descTextarea ? descTextarea.value.trim() : "";

        if (previewTitle) {
            previewTitle.textContent = title || "Introduction to Computer Science";
        }
        if (previewCodeBadge) {
            previewCodeBadge.textContent = code || "CS101";
        }
        if (previewSemesterBadge) {
            previewSemesterBadge.textContent = sem || "Fall 2026";
        }
        if (previewDeptTag) {
            previewDeptTag.textContent = dept || "Computer Science";
        }
        if (previewDesc) {
            previewDesc.textContent = desc || "Comprehensive study of foundational computing concepts, program design, and algorithmic problem-solving.";
        }
        if (charCount && descTextarea) {
            charCount.textContent = `${descTextarea.value.length} chars`;
        }
    }

    if (nameInput) nameInput.addEventListener("input", updatePreview);
    if (codeInput) {
        codeInput.addEventListener("input", function() {
            this.value = this.value.toUpperCase();
            updatePreview();
        });
    }
    if (deptSelect) deptSelect.addEventListener("change", updatePreview);
    if (semSelect) semSelect.addEventListener("change", updatePreview);
    if (descTextarea) descTextarea.addEventListener("input", updatePreview);

    // Initial sync
    updatePreview();

    // 2. Preset Template Applier
    window.applyPreset = function (name, code, dept, desc) {
        if (nameInput) nameInput.value = name;
        if (codeInput) codeInput.value = code;
        if (deptSelect) deptSelect.value = dept;
        if (descTextarea && desc) descTextarea.value = desc;
        updatePreview();
        if (window.showToast) {
            showToast(`Loaded curriculum preset for ${code}`, "info");
        }
    };

    // 3. Form Submission
    if (form) {
        form.addEventListener("submit", async function (e) {
            e.preventDefault();

            const name = nameInput.value.trim();
            const courseCode = codeInput.value.trim().toUpperCase();
            const department = deptSelect.value.trim();
            const semester = semSelect.value.trim();
            const description = descTextarea ? descTextarea.value.trim() : "";

            if (!name || !courseCode) {
                if (window.showToast) showToast("Course Title and Catalog Code are required.", "warning");
                return;
            }

            // UI loading state
            if (submitBtn) submitBtn.disabled = true;
            if (btnText) btnText.textContent = "Publishing Course...";
            if (btnSpinner) btnSpinner.style.display = "inline-block";

            try {
                const payload = {
                    name,
                    courseCode,
                    department,
                    semester,
                    description
                };

                const res = await api.post("/api/teacher/courses", payload);

                if (window.showToast) {
                    showToast("Course created successfully!", "success");
                }

                currentEnrollmentCode = res.enrollmentCode || "SUCCESS";

                // Show Success Panel
                if (generatedCodeDisplay) {
                    generatedCodeDisplay.textContent = currentEnrollmentCode;
                }
                if (goToCourseDetailBtn && res.id) {
                    goToCourseDetailBtn.href = `teacher-course-detail.html?id=${res.id}`;
                }

                if (studioContainer) studioContainer.style.display = "none";
                if (successPanel) {
                    successPanel.style.display = "block";
                    successPanel.scrollIntoView({ behavior: "smooth" });
                }

            } catch (err) {
                console.error("Course creation failed:", err);
                if (window.showToast) {
                    showToast("Failed to create course: " + err.message, "error");
                }
                if (submitBtn) submitBtn.disabled = false;
                if (btnText) btnText.textContent = "Publish Course & Generate Key →";
                if (btnSpinner) btnSpinner.style.display = "none";
            }
        });
    }

    // 4. Clipboard Copy
    if (copyCodeBtn) {
        copyCodeBtn.addEventListener("click", function () {
            if (!currentEnrollmentCode) return;
            navigator.clipboard.writeText(currentEnrollmentCode).then(function () {
                copyCodeBtn.textContent = "✓ Copied!";
                if (window.showToast) showToast(`Enrollment code ${currentEnrollmentCode} copied to clipboard!`, "success");
                setTimeout(function () {
                    copyCodeBtn.textContent = "📋 Copy Code";
                }, 2000);
            }).catch(function () {
                if (window.showToast) showToast("Could not copy automatically. Code: " + currentEnrollmentCode, "info");
            });
        });
    }
});
