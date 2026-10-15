// student-dashboard.js — Student Dashboard Enrolled Courses & Quick Actions
document.addEventListener("DOMContentLoaded", async function() {
    const container = document.getElementById("studentCourses") || 
                      document.getElementById("enrolledCoursesContainer") || 
                      document.getElementById("courseList");
    if (!container) return;

    container.innerHTML = '<div class="loading-container" style="grid-column: 1 / -1;"><div class="spinner"></div><span>Loading enrolled courses...</span></div>';

    try {
        const courses = await api.get("/api/student/courses");
        if (!courses || courses.length === 0) {
            container.innerHTML = `
                <div class="empty-state" style="grid-column: 1 / -1;">
                    <div class="empty-state-icon">🎓</div>
                    <h3 class="empty-state-title">Not Enrolled in Any Course</h3>
                    <p class="empty-state-desc">Join a course using an 8-character enrollment code provided by your instructor to access lecture videos and assignments.</p>
                    <a href="student-join-course.html" class="btn btn-primary">Join a Course with Code</a>
                </div>
            `;
            return;
        }

        container.innerHTML = courses.map(c => renderCourseCard(c, "STUDENT")).join("");
    } catch (err) {
        console.error("Error loading courses:", err);
        showToast("Error loading enrolled courses: " + err.message, "error");
        container.innerHTML = `<div class="empty-state" style="grid-column: 1 / -1;"><p class="empty-state-title">Failed to load courses</p><p class="empty-state-desc">${err.message}</p></div>`;
    }
});
