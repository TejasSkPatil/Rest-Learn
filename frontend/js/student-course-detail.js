// student-course-detail.js — Student Course Learning Portal with Direct Video Streaming & Assignment Submission
document.addEventListener("DOMContentLoaded", async function () {
    const urlParams = new URLSearchParams(window.location.search);
    const courseId = parseInt(urlParams.get("id")) || 1;

    let isEnrolled = false;
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

    // 1. Check Enrollment & Course Info
    async function checkStatus() {
        try {
            const statusRes = await api.get(`/api/student/courses/${courseId}/status`);
            isEnrolled = statusRes.enrolled;
            currentCourse = statusRes.course;

            if (!currentCourse) {
                currentCourse = await api.get(`/api/courses/${courseId}`);
            }

            // Populate Course Header
            document.getElementById("courseNameTitle").textContent = currentCourse.name;
            document.getElementById("breadcrumbTitle").textContent = currentCourse.courseCode || currentCourse.name;
            document.getElementById("courseCodeBadge").textContent = currentCourse.courseCode || "CS";
            document.getElementById("courseSemesterBadge").textContent = currentCourse.semester || "Current";
            document.getElementById("teacherName").textContent = currentCourse.teacher?.name || "Professor";
            document.getElementById("deptName").textContent = currentCourse.department || "General";
            document.getElementById("studentCourseDesc").textContent = currentCourse.description || "No description provided.";

            const overviewTeacher = document.getElementById("overviewTeacherName");
            if (overviewTeacher) overviewTeacher.textContent = currentCourse.teacher?.name || "Professor";
            const overviewDept = document.getElementById("overviewDeptName");
            if (overviewDept) overviewDept.textContent = currentCourse.department || "General";

            const lockedScreen = document.getElementById("lockedScreen");
            const enrolledContent = document.getElementById("enrolledCourseContent");
            const statusBadge = document.getElementById("enrollmentStatusBadge");

            if (!isEnrolled) {
                lockedScreen.style.display = "block";
                enrolledContent.style.display = "none";
                statusBadge.className = "badge badge-warning";
                statusBadge.textContent = "🔒 Not Enrolled";
            } else {
                lockedScreen.style.display = "none";
                enrolledContent.style.display = "block";
                statusBadge.className = "badge badge-success";
                statusBadge.textContent = "✓ Enrolled";

                // Load Videos & Assignments
                await loadVideos();
                await loadAssignments();
            }

        } catch (err) {
            console.error("Check status error:", err);
            showToast("Failed to load course information: " + err.message, "error");
        }
    }

    // 2. Enroll form
    const enrollForm = document.getElementById("enrollCourseForm");
    if (enrollForm) {
        enrollForm.addEventListener("submit", async function (e) {
            e.preventDefault();
            const code = document.getElementById("joinEnrollmentCodeInput").value.trim().toUpperCase();
            if (!code) return;

            try {
                await api.post("/api/student/enroll", { enrollmentCode: code });
                showToast("Enrolled successfully! Unlocking course materials...", "success");
                await checkStatus();
            } catch (err) {
                showToast("Enrollment failed: " + err.message, "error");
            }
        });
    }

    // Render Video Card HTML helper
    function createVideoCardHtml(v) {
        return `
            <div class="card card-interactive" style="display:flex; flex-direction:column; justify-content:space-between; cursor:pointer;" onclick="playCourseVideo(${v.id})">
                <div>
                    <div style="position:relative; background:#111; height:140px; border-radius:var(--radius-md); display:flex; align-items:center; justify-content:center; margin-bottom:12px; overflow:hidden;">
                        <span style="font-size:2.8rem; color:white; opacity:0.9; transition:transform 0.2s;" onmouseover="this.style.transform='scale(1.1)'" onmouseout="this.style.transform='scale(1)'">▶</span>
                        <span style="position:absolute; bottom:8px; right:8px; background:rgba(0,0,0,0.75); color:white; font-size:0.75rem; padding:2px 8px; border-radius:4px; font-weight:700;">
                            ${v.duration || '20:00'}
                        </span>
                        <span style="position:absolute; top:8px; left:8px; background:var(--primary); color:white; font-size:0.68rem; padding:2px 6px; border-radius:4px; font-weight:700; text-transform:uppercase;">
                            ${v.moduleTitle || 'Lecture'}
                        </span>
                    </div>
                    <h4 style="font-size:1.05rem; font-weight:700; color:var(--text-main); margin-bottom:4px; line-height:1.35;">
                        ${v.title}
                    </h4>
                    <small style="color:var(--text-muted); font-size:0.75rem;">
                        Section: <strong>${v.moduleTitle || 'General Lecture'}</strong>
                    </small>
                </div>
                <div style="margin-top:14px; border-top:1px solid var(--border-color); padding-top:10px;">
                    <button type="button" class="btn btn-primary btn-sm" style="width:100%; display:flex; align-items:center; justify-content:center; gap:6px;">
                        <span>▶</span> <span>Watch Lecture</span>
                    </button>
                </div>
            </div>
        `;
    }

    // 3. Load & Stream Course Videos
    async function loadVideos() {
        const allGrid = document.getElementById("allVideosGrid");
        const singleGrid = document.getElementById("studentVideosGrid");
        try {
            courseVideos = await api.get(`/api/courses/${courseId}/videos`);
            const count = courseVideos ? courseVideos.length : 0;

            const countBadge = document.getElementById("videosTabCount");
            if (countBadge) countBadge.textContent = count;
            const allCountBadge = document.getElementById("allVideosCountBadge");
            if (allCountBadge) allCountBadge.textContent = `${count} Videos`;

            if (!courseVideos || courseVideos.length === 0) {
                const emptyHtml = `
                    <div class="empty-state" style="grid-column: 1 / -1; padding: 30px 20px;">
                        <div class="empty-state-icon" style="font-size:2.5rem;">📹</div>
                        <h3 class="empty-state-title" style="font-size:1.1rem;">No Lecture Videos Uploaded Yet</h3>
                        <p class="empty-state-desc">Your instructor will publish lecture videos and recordings for this course soon.</p>
                    </div>
                `;
                if (allGrid) allGrid.innerHTML = emptyHtml;
                if (singleGrid) singleGrid.innerHTML = emptyHtml;
                return;
            }

            const html = courseVideos.map(createVideoCardHtml).join("");
            if (allGrid) allGrid.innerHTML = html;
            if (singleGrid) singleGrid.innerHTML = html;

            // If player not yet loaded, pre-load first video
            const playerBox = document.getElementById("activePlayerBox");
            if (courseVideos.length > 0 && (!playerBox || playerBox.style.display === "none")) {
                playCourseVideo(courseVideos[0].id, false);
            }

        } catch (err) {
            console.error("Load videos error:", err);
            const errHtml = `<div class="empty-state" style="grid-column: 1 / -1;"><p>Failed to load videos: ${err.message}</p></div>`;
            if (allGrid) allGrid.innerHTML = errHtml;
            if (singleGrid) singleGrid.innerHTML = errHtml;
        }
    }

    // Play video helper
    window.playCourseVideo = function (videoId, autoPlay = true) {
        const v = courseVideos.find(item => item.id === videoId);
        if (!v) return;

        const playerBox = document.getElementById("activePlayerBox");
        const player = document.getElementById("studentVideoPlayer");
        const titleEl = document.getElementById("nowPlayingTitle");
        const modEl = document.getElementById("nowPlayingModule");
        const durEl = document.getElementById("nowPlayingDuration");

        if (titleEl) titleEl.textContent = v.title;
        if (modEl) modEl.textContent = v.moduleTitle || "Lecture Video";
        if (durEl) durEl.textContent = `Duration: ${v.duration || '20:00'}`;

        const videoSrc = v.videoUrl || "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
        player.src = videoSrc;
        playerBox.style.display = "block";

        if (autoPlay) {
            playerBox.scrollIntoView({ behavior: "smooth" });
            player.play().catch(e => console.log("Autoplay note:", e));
        }
    };

    // Render Assignment Card HTML helper
    function createAssignmentCardHtml(a) {
        const sub = a.mySubmission;
        let statusBadge = '<span class="badge badge-warning">Pending Submission</span>';
        let actionBtnText = 'Submit Work &rarr;';
        let feedbackHtml = '';

        if (sub) {
            if (sub.status === 'GRADED') {
                statusBadge = `<span class="badge badge-success">✓ Graded: ${sub.marks !== null ? sub.marks : '-'}/${a.maxPoints || 100}</span>`;
                actionBtnText = 'View Graded Work';
                if (sub.feedback) {
                    feedbackHtml = `
                        <div style="margin-top:10px; padding:10px 14px; background:var(--success-light); border-left:4px solid var(--success); border-radius:4px; font-size:0.825rem; color:var(--text-main);">
                            <strong>Instructor Feedback:</strong> ${sub.feedback}
                        </div>
                    `;
                }
            } else {
                statusBadge = '<span class="badge badge-primary">✓ Submitted</span>';
                actionBtnText = 'Edit / Review Submission';
            }
        }

        return `
            <div class="card" style="padding:var(--space-5); border:1px solid var(--border-color); transition:box-shadow var(--transition-fast);">
                <div style="display:flex; justify-content:space-between; align-items:flex-start; flex-wrap:wrap; gap:12px; margin-bottom:8px;">
                    <div>
                        <div style="display:flex; align-items:center; gap:8px; margin-bottom:6px; flex-wrap:wrap;">
                            <span class="badge badge-primary">${a.moduleTitle || 'General'}</span>
                            <span class="badge badge-neutral">Due: ${a.dueDate || 'No Due Date'}</span>
                            <span class="badge badge-neutral">${a.maxPoints || 100} pts</span>
                            ${statusBadge}
                        </div>
                        <h3 style="font-size:1.2rem; font-weight:800; color:var(--text-main); margin:0;">
                            ${a.title}
                        </h3>
                    </div>

                    <button type="button" class="btn btn-primary btn-sm" onclick="openSubmissionModal(${a.id})">
                        ${actionBtnText}
                    </button>
                </div>

                <p style="color:var(--text-muted); font-size:0.9rem; margin:8px 0 0; line-height:1.5;">
                    ${a.description || 'Complete the deliverables and task requirements as specified by your instructor.'}
                </p>
                ${feedbackHtml}
            </div>
        `;
    }

    // 4. Load Course Assignments
    async function loadAssignments() {
        const allList = document.getElementById("allAssignmentsList");
        const singleList = document.getElementById("studentAssignmentsList");
        try {
            courseAssignments = await api.get(`/api/courses/${courseId}/assignments`);
            const count = courseAssignments ? courseAssignments.length : 0;

            const countBadge = document.getElementById("assignmentsTabCount");
            if (countBadge) countBadge.textContent = count;
            const allCountBadge = document.getElementById("allAssignmentsCountBadge");
            if (allCountBadge) allCountBadge.textContent = `${count} Assignments`;

            if (!courseAssignments || courseAssignments.length === 0) {
                const emptyHtml = `
                    <div class="empty-state" style="padding: 30px 20px;">
                        <div class="empty-state-icon" style="font-size:2.5rem;">📝</div>
                        <h3 class="empty-state-title" style="font-size:1.1rem;">No Assignments Published</h3>
                        <p class="empty-state-desc">There are no assignments due for this course at this time.</p>
                    </div>
                `;
                if (allList) allList.innerHTML = emptyHtml;
                if (singleList) singleList.innerHTML = emptyHtml;
                return;
            }

            const html = courseAssignments.map(createAssignmentCardHtml).join("");
            if (allList) allList.innerHTML = html;
            if (singleList) singleList.innerHTML = html;

        } catch (err) {
            console.error("Load assignments error:", err);
            const errHtml = `<div class="empty-state"><p>Failed to load assignments: ${err.message}</p></div>`;
            if (allList) allList.innerHTML = errHtml;
            if (singleList) singleList.innerHTML = errHtml;
        }
    }

    // 5. Open Submission Modal
    window.openSubmissionModal = function (assignmentId) {
        const a = courseAssignments.find(item => item.id === assignmentId);
        if (!a) return;

        const modal = document.getElementById("studentSubmitModal");
        const titleEl = document.getElementById("modalAssignmentTitle");
        const metaEl = document.getElementById("modalAssignmentMeta");
        const hiddenId = document.getElementById("submitAssignmentId");
        const textAnswer = document.getElementById("submissionTextAnswer");
        const noticeBox = document.getElementById("existingSubmissionNotice");
        const dateEl = document.getElementById("existingSubDate");
        const gradeEl = document.getElementById("existingGradeDisplay");

        hiddenId.value = a.id;
        titleEl.textContent = `Submit: ${a.title}`;
        metaEl.textContent = `Due Date: ${a.dueDate || 'N/A'} • Maximum Marks: ${a.maxPoints || 100} pts`;

        const sub = a.mySubmission;
        if (sub && (sub.textAnswer || sub.submittedAt)) {
            noticeBox.style.display = "block";
            dateEl.textContent = new Date(sub.submittedAt).toLocaleString();
            textAnswer.value = sub.textAnswer || "";
            if (sub.status === 'GRADED') {
                gradeEl.textContent = `Grade Assigned: ${sub.marks}/${a.maxPoints || 100}`;
                if (sub.feedback) gradeEl.textContent += ` ("${sub.feedback}")`;
            } else {
                gradeEl.textContent = "Status: Submitted (Pending Instructor Review)";
            }
        } else {
            noticeBox.style.display = "none";
            textAnswer.value = "";
        }

        modal.style.display = "flex";
    };

    // Close Modal
    const closeModal = () => document.getElementById("studentSubmitModal").style.display = "none";
    const closeBtn = document.getElementById("closeSubmitModalBtn");
    const cancelBtn = document.getElementById("cancelSubmitModalBtn");
    if (closeBtn) closeBtn.addEventListener("click", closeModal);
    if (cancelBtn) cancelBtn.addEventListener("click", closeModal);

    // 6. Submit Solution Handler
    const submitForm = document.getElementById("assignmentSubmissionForm");
    if (submitForm) {
        submitForm.addEventListener("submit", async function (e) {
            e.preventDefault();
            const aId = document.getElementById("submitAssignmentId").value;
            const textAnswer = document.getElementById("submissionTextAnswer").value.trim();
            const fileInput = document.getElementById("submissionAttachmentFile");
            const submitBtn = document.getElementById("submitSolutionBtn");

            if (!textAnswer) {
                showToast("Please provide a text response or summary.", "warning");
                return;
            }

            if (submitBtn) { submitBtn.disabled = true; submitBtn.textContent = "Submitting Solution..."; }

            try {
                const formData = new FormData();
                formData.append("textAnswer", textAnswer);
                if (fileInput && fileInput.files[0]) {
                    formData.append("file", fileInput.files[0]);
                }

                await api.post(`/api/student/assignments/${aId}/submissions`, formData);
                showToast("Assignment submitted successfully!", "success");
                closeModal();
                await loadAssignments();

            } catch (err) {
                showToast("Submission failed: " + err.message, "error");
            } finally {
                if (submitBtn) { submitBtn.disabled = false; submitBtn.textContent = "Submit Solution →"; }
            }
        });
    }

    // Initial Execution
    await checkStatus();
});
