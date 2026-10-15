// teacher-course-detail.js — Teacher Course Management with Video & Assignment Uploading
document.addEventListener("DOMContentLoaded", async function () {
    const urlParams = new URLSearchParams(window.location.search);
    const courseId = parseInt(urlParams.get("id")) || 1;

    let currentCourse = null;
    let courseVideos = [];
    let courseAssignments = [];

    // Tab Switching
    const tabBtns = document.querySelectorAll(".tab-btn");
    const tabPanels = document.querySelectorAll(".tab-panel");

    tabBtns.forEach(btn => {
        btn.addEventListener("click", () => {
            tabBtns.forEach(b => b.classList.remove("active"));
            tabPanels.forEach(p => p.classList.remove("active"));

            btn.classList.add("active");
            const targetId = btn.getAttribute("data-tab");
            const panel = document.getElementById(targetId);
            if (panel) panel.classList.add("active");
        });
    });

    // 1. Fetch Course Info
    async function loadCourseInfo() {
        try {
            currentCourse = await api.get(`/api/courses/${courseId}`);
            
            // Header elements
            document.getElementById("courseNameHeader").textContent = currentCourse.name;
            document.getElementById("breadcrumbCourseTitle").textContent = currentCourse.name;
            document.getElementById("courseCodeBadge").textContent = currentCourse.courseCode || "CS";
            document.getElementById("courseSemesterBadge").textContent = currentCourse.semester || "Current";
            document.getElementById("courseDeptName").textContent = currentCourse.department || "General";
            document.getElementById("enrollmentCode").textContent = currentCourse.enrollmentCode || "NO-CODE";
            document.getElementById("courseFullDesc").textContent = currentCourse.description || "No description provided.";
            document.getElementById("courseTeacherName").textContent = currentCourse.teacher?.name || "Instructor";
            document.getElementById("courseCreatedDate").textContent = currentCourse.createdAt ? new Date(currentCourse.createdAt).toLocaleDateString() : "-";

            const statusBadge = document.getElementById("codeStatusBadge");
            if (currentCourse.codeActive !== false) {
                statusBadge.className = "badge badge-success";
                statusBadge.textContent = "Enrollment Active";
            } else {
                statusBadge.className = "badge badge-neutral";
                statusBadge.textContent = "Enrollment Paused";
            }

        } catch (err) {
            console.error("Load course error:", err);
            showToast("Failed to load course details: " + err.message, "error");
        }
    }

    // 2. Fetch Course Videos
    async function loadVideos() {
        const grid = document.getElementById("courseVideosGrid");
        try {
            courseVideos = await api.get(`/api/courses/${courseId}/videos`);
            document.getElementById("statsVideosCount").textContent = courseVideos.length;
            document.getElementById("videosTabBadge").textContent = courseVideos.length;

            if (!courseVideos || courseVideos.length === 0) {
                grid.innerHTML = `
                    <div class="empty-state" style="grid-column: 1 / -1;">
                        <div class="empty-state-icon">📹</div>
                        <h3 class="empty-state-title">No Lecture Videos Yet</h3>
                        <p class="empty-state-desc">Upload video lectures for this course so enrolled students can start learning.</p>
                        <button type="button" class="btn btn-primary btn-sm" onclick="document.getElementById('uploadVideoFormCard').style.display='block'">+ Upload First Video</button>
                    </div>
                `;
                return;
            }

            grid.innerHTML = courseVideos.map(v => `
                <div class="card" style="display:flex; flex-direction:column; justify-content:space-between;">
                    <div>
                        <div style="position:relative; background:#000; height:130px; border-radius:var(--radius-md); display:flex; align-items:center; justify-content:center; margin-bottom:12px; overflow:hidden; cursor:pointer;" onclick="playVideo('${encodeURIComponent(v.title)}', '${v.videoUrl}')">
                            <span style="font-size:2.4rem; color:white; opacity:0.85;">▶</span>
                            <span style="position:absolute; bottom:6px; right:8px; background:rgba(0,0,0,0.7); color:white; font-size:0.7rem; padding:2px 6px; border-radius:4px; font-weight:600;">
                                ${v.duration || '15:00'}
                            </span>
                        </div>
                        <h4 style="font-size:1rem; font-weight:700; color:var(--text-main); margin-bottom:4px; line-height:1.3;">
                            ${v.title}
                        </h4>
                        <small style="color:var(--text-muted); font-size:0.75rem;">
                            Section: <strong>${v.moduleTitle || 'General'}</strong>
                        </small>
                    </div>
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-top:14px; border-top:1px solid var(--border-color); padding-top:10px;">
                        <button class="btn btn-outline btn-sm" style="font-size:0.75rem; padding:3px 8px;" onclick="playVideo('${encodeURIComponent(v.title)}', '${v.videoUrl}')">
                            ▶ Play Video
                        </button>
                        <button class="btn btn-outline btn-sm" style="color:var(--danger); border-color:var(--danger); font-size:0.75rem; padding:3px 8px;" onclick="deleteVideo(${v.id})">
                            🗑️ Delete
                        </button>
                    </div>
                </div>
            `).join("");

        } catch (err) {
            console.error("Load videos error:", err);
            grid.innerHTML = `<div class="empty-state" style="grid-column: 1 / -1;"><p>Failed to load videos: ${err.message}</p></div>`;
        }
    }

    // 3. Fetch Course Assignments
    async function loadAssignments() {
        const list = document.getElementById("courseAssignmentsList");
        try {
            courseAssignments = await api.get(`/api/courses/${courseId}/assignments`);
            document.getElementById("statsAssignmentsCount").textContent = courseAssignments.length;
            document.getElementById("assignmentsTabBadge").textContent = courseAssignments.length;

            if (!courseAssignments || courseAssignments.length === 0) {
                list.innerHTML = `
                    <div class="empty-state">
                        <div class="empty-state-icon">📝</div>
                        <h3 class="empty-state-title">No Assignments Published</h3>
                        <p class="empty-state-desc">Create homework assignments and coursework tasks for your students.</p>
                        <button type="button" class="btn btn-primary btn-sm" onclick="document.getElementById('createAssignmentFormCard').style.display='block'">+ Create First Assignment</button>
                    </div>
                `;
                return;
            }

            list.innerHTML = courseAssignments.map(a => `
                <div class="card" style="padding:var(--space-5);">
                    <div style="display:flex; justify-content:space-between; align-items:flex-start; flex-wrap:wrap; gap:10px; margin-bottom:8px;">
                        <div>
                            <div style="display:flex; align-items:center; gap:8px; margin-bottom:4px;">
                                <span class="badge badge-primary">${a.moduleTitle || 'General'}</span>
                                <span class="badge badge-warning">Due: ${a.dueDate || 'No Due Date'}</span>
                                <span class="badge badge-neutral">Max: ${a.maxPoints || 100} pts</span>
                            </div>
                            <h3 style="font-size:1.15rem; font-weight:700; color:var(--text-main); margin:0;">
                                ${a.title}
                            </h3>
                        </div>

                        <div style="display:flex; align-items:center; gap:8px;">
                            <button class="btn btn-outline btn-sm" onclick="viewSubmissions(${a.id}, '${encodeURIComponent(a.title)}')">
                                👥 View Submissions (${a.submissionCount || 0})
                            </button>
                            <button class="btn btn-outline btn-sm" style="color:var(--danger); border-color:var(--danger);" onclick="deleteAssignment(${a.id})">
                                🗑️
                            </button>
                        </div>
                    </div>

                    <p style="color:var(--text-muted); font-size:0.875rem; margin:8px 0 0; line-height:1.5;">
                        ${a.description || 'No detailed instructions provided.'}
                    </p>
                </div>
            `).join("");

        } catch (err) {
            console.error("Load assignments error:", err);
            list.innerHTML = `<div class="empty-state"><p>Failed to load assignments: ${err.message}</p></div>`;
        }
    }

    // Toggle forms
    const toggleVideoBtn = document.getElementById("toggleUploadVideoBtn");
    const videoFormCard = document.getElementById("uploadVideoFormCard");
    const cancelVideoBtn = document.getElementById("cancelUploadVideoBtn");
    if (toggleVideoBtn) toggleVideoBtn.addEventListener("click", () => videoFormCard.style.display = "block");
    if (cancelVideoBtn) cancelVideoBtn.addEventListener("click", () => videoFormCard.style.display = "none");

    const toggleAssignBtn = document.getElementById("toggleCreateAssignmentBtn");
    const assignFormCard = document.getElementById("createAssignmentFormCard");
    const cancelAssignBtn = document.getElementById("cancelCreateAssignmentBtn");
    if (toggleAssignBtn) toggleAssignBtn.addEventListener("click", () => assignFormCard.style.display = "block");
    if (cancelAssignBtn) cancelAssignBtn.addEventListener("click", () => assignFormCard.style.display = "none");

    // Video Upload Submission
    const uploadVideoForm = document.getElementById("uploadVideoForm");
    if (uploadVideoForm) {
        uploadVideoForm.addEventListener("submit", async function (e) {
            e.preventDefault();
            const title = document.getElementById("videoTitle").value.trim();
            const duration = document.getElementById("videoDuration").value.trim() || "20:00";
            const moduleTitle = document.getElementById("videoModuleName") ? document.getElementById("videoModuleName").value.trim() : "";
            const videoUrl = document.getElementById("videoUrlInput").value.trim();
            const fileInput = document.getElementById("videoFileInput");

            const submitBtn = document.getElementById("submitVideoBtn");
            if (submitBtn) { submitBtn.disabled = true; submitBtn.textContent = "Uploading..."; }

            try {
                const formData = new FormData();
                formData.append("title", title);
                formData.append("duration", duration);
                if (moduleTitle) formData.append("moduleTitle", moduleTitle);
                if (videoUrl) formData.append("videoUrl", videoUrl);
                if (fileInput && fileInput.files[0]) {
                    formData.append("file", fileInput.files[0]);
                }

                await api.post(`/api/teacher/courses/${courseId}/videos`, formData);
                showToast("Video uploaded successfully!", "success");
                uploadVideoForm.reset();
                videoFormCard.style.display = "none";
                await loadVideos();

            } catch (err) {
                showToast("Failed to upload video: " + err.message, "error");
            } finally {
                if (submitBtn) { submitBtn.disabled = false; submitBtn.textContent = "Upload Video →"; }
            }
        });
    }

    // Assignment Creation Submission
    const createAssignmentForm = document.getElementById("createAssignmentForm");
    if (createAssignmentForm) {
        createAssignmentForm.addEventListener("submit", async function (e) {
            e.preventDefault();
            const title = document.getElementById("assignmentTitle").value.trim();
            const moduleTitle = document.getElementById("assignmentModuleName") ? document.getElementById("assignmentModuleName").value.trim() : "";
            const description = document.getElementById("assignmentDesc").value.trim();
            const dueDate = document.getElementById("assignmentDueDate").value;
            const maxPoints = document.getElementById("assignmentMaxPoints").value || 100;

            const submitBtn = document.getElementById("submitAssignmentBtn");
            if (submitBtn) { submitBtn.disabled = true; submitBtn.textContent = "Publishing..."; }

            try {
                await api.post(`/api/teacher/courses/${courseId}/assignments`, {
                    title,
                    moduleTitle,
                    description,
                    dueDate,
                    maxPoints
                });

                showToast("Assignment published successfully!", "success");
                createAssignmentForm.reset();
                assignFormCard.style.display = "none";
                await loadAssignments();

            } catch (err) {
                showToast("Failed to create assignment: " + err.message, "error");
            } finally {
                if (submitBtn) { submitBtn.disabled = false; submitBtn.textContent = "Publish Assignment →"; }
            }
        });
    }

    // Delete Video
    window.deleteVideo = async function (id) {
        if (!confirm("Are you sure you want to delete this lecture video? Enrolled students will no longer be able to watch it.")) return;
        try {
            await api.del(`/api/teacher/videos/${id}`);
            showToast("Video deleted successfully.", "info");
            const modal = document.getElementById("videoPlayerModal");
            if (modal && modal.style.display !== "none") {
                const videoEl = document.getElementById("modalVideoElement");
                if (videoEl) videoEl.pause();
                modal.style.display = "none";
            }
            await loadVideos();
        } catch (err) {
            showToast("Failed to delete video: " + err.message, "error");
        }
    };

    // Delete Assignment
    window.deleteAssignment = async function (id) {
        if (!confirm("Are you sure you want to delete this assignment?")) return;
        try {
            await api.del(`/api/teacher/assignments/${id}`);
            showToast("Assignment deleted.", "info");
            await loadAssignments();
        } catch (err) {
            showToast("Failed to delete assignment: " + err.message, "error");
        }
    };

    // Video Player Modal
    window.playVideo = function (rawTitle, videoUrl) {
        const title = decodeURIComponent(rawTitle);
        const modal = document.getElementById("videoPlayerModal");
        const modalTitle = document.getElementById("modalVideoTitle");
        const videoEl = document.getElementById("modalVideoElement");

        modalTitle.textContent = title;
        videoEl.src = videoUrl || "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
        modal.style.display = "flex";
        videoEl.play().catch(e => console.log("Autoplay note:", e));
    };

    const closeVideoBtn = document.getElementById("closeVideoModalBtn");
    if (closeVideoBtn) {
        closeVideoBtn.addEventListener("click", () => {
            const modal = document.getElementById("videoPlayerModal");
            const videoEl = document.getElementById("modalVideoElement");
            if (videoEl) videoEl.pause();
            modal.style.display = "none";
        });
    }

    // View Submissions Modal
    window.viewSubmissions = async function (assignmentId, rawTitle) {
        const title = decodeURIComponent(rawTitle);
        const modal = document.getElementById("submissionsModal");
        const modalTitle = document.getElementById("submissionsModalTitle");
        const content = document.getElementById("submissionsModalContent");

        modalTitle.textContent = `Submissions for: ${title}`;
        modal.style.display = "flex";
        content.innerHTML = '<div class="loading-container"><div class="spinner"></div><span>Loading student submissions...</span></div>';

        try {
            const subs = await api.get(`/api/teacher/assignments/${assignmentId}/submissions`);

            if (!subs || subs.length === 0) {
                content.innerHTML = `
                    <div class="empty-state">
                        <div class="empty-state-icon">📥</div>
                        <h4 class="empty-state-title">No Submissions Yet</h4>
                        <p class="empty-state-desc">Students enrolled in this course have not submitted answers yet.</p>
                    </div>
                `;
                return;
            }

            content.innerHTML = subs.map(s => `
                <div class="card" style="margin-bottom:12px; padding:16px; border:1px solid var(--border-color);">
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
                        <div>
                            <strong style="font-size:1rem; color:var(--text-main);">${s.studentName || 'Student #' + s.studentId}</strong>
                            <small style="color:var(--text-muted); display:block;">Submitted: ${new Date(s.submittedAt).toLocaleString()}</small>
                        </div>
                        <span class="badge ${s.status === 'GRADED' ? 'badge-success' : 'badge-warning'}">
                            ${s.status === 'GRADED' ? 'Score: ' + s.marks + '/100' : 'Pending Review'}
                        </span>
                    </div>

                    <div style="background:var(--bg-surface-alt); padding:10px; border-radius:var(--radius-md); font-size:0.85rem; margin-bottom:12px; color:var(--text-main);">
                        <strong>Answer Text / Solution:</strong>
                        <p style="margin:4px 0 0; white-space:pre-wrap;">${s.textAnswer || s.content || '(File attachment only)'}</p>
                        ${s.fileRef ? `<div style="margin-top:6px;"><a href="/uploads/resources/${s.fileRef}" download class="btn btn-outline btn-sm" style="font-size:0.75rem;">📎 Download File: ${s.fileRef}</a></div>` : ''}
                    </div>

                    <!-- Grading Form -->
                    <form onsubmit="gradeSubmission(event, ${s.id})" style="display:flex; gap:10px; align-items:center; flex-wrap:wrap;">
                        <input type="number" name="marks" class="input" style="width:110px; padding:6px;" placeholder="Marks / 100" value="${s.marks !== null ? s.marks : ''}" required min="0" max="100">
                        <input type="text" name="feedback" class="input" style="flex:1; min-width:180px; padding:6px;" placeholder="Teacher Feedback / Notes" value="${s.feedback || ''}">
                        <button type="submit" class="btn btn-primary btn-sm">Save Grade</button>
                    </form>
                </div>
            `).join("");

        } catch (err) {
            content.innerHTML = `<p style="color:var(--danger);">Failed to load submissions: ${err.message}</p>`;
        }
    };

    window.gradeSubmission = async function (e, subId) {
        e.preventDefault();
        const form = e.target;
        const marks = parseInt(form.marks.value);
        const feedback = form.feedback.value.trim();

        try {
            await api.put(`/api/teacher/submissions/${subId}/grade`, { marks, feedback });
            showToast("Grade saved successfully!", "success");
            const badge = form.closest(".card").querySelector(".badge");
            if (badge) {
                badge.className = "badge badge-success";
                badge.textContent = `Score: ${marks}/100`;
            }
        } catch (err) {
            showToast("Failed to save grade: " + err.message, "error");
        }
    };

    const closeSubModalBtn = document.getElementById("closeSubmissionsModalBtn");
    if (closeSubModalBtn) {
        closeSubModalBtn.addEventListener("click", () => {
            document.getElementById("submissionsModal").style.display = "none";
        });
    }

    // Copy & Regenerate Code
    const copyCodeBtn = document.getElementById("copyCodeBtn");
    if (copyCodeBtn) {
        copyCodeBtn.addEventListener("click", () => {
            const code = document.getElementById("enrollmentCode").textContent;
            navigator.clipboard.writeText(code).then(() => {
                showToast(`Code ${code} copied!`, "success");
            });
        });
    }

    const regenBtn = document.getElementById("regenerateCodeBtn");
    if (regenBtn) {
        regenBtn.addEventListener("click", async () => {
            if (!confirm("Regenerate a new enrollment code? The old code will stop working for new students.")) return;
            try {
                const res = await api.post(`/api/teacher/courses/${courseId}/regenerate-code`);
                document.getElementById("enrollmentCode").textContent = res.enrollmentCode;
                showToast("New code generated: " + res.enrollmentCode, "success");
            } catch (err) {
                showToast("Failed to regenerate code: " + err.message, "error");
            }
        });
    }

    // Run Initial Loads
    await loadCourseInfo();
    await loadVideos();
    await loadAssignments();
});
