// Student Attendance Page
document.addEventListener('DOMContentLoaded', function () {
    loadAttendance();
});

async function loadAttendance() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');

    try {
        const response = await fetch('http://localhost:8080/api/student/attendance/mine', {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) throw new Error('Failed to fetch attendance');

        const data = await response.json();

        // Overall
        document.getElementById('overallAttendance').innerHTML = `
            <div class="card mb-3">
                <div class="card-body">
                    <h2>${data.overall.toFixed(1)}%</h2>
                    <p>Overall Attendance</p>
                </div>
            </div>
        `;

        // Per course
        const perCourse = data.perCourse || {};
        const courseHtml = Object.entries(perCourse).map(([name, pct]) => `
            <div class="course-card">
                <h4>${name}</h4>
                <p><strong>${pct.toFixed(1)}%</strong></p>
            </div>
        `).join('');
        document.getElementById('courseAttendance').innerHTML = courseHtml || '<p>No courses enrolled.</p>';

    } catch (error) {
        console.error(error);
        document.getElementById('message').textContent = 'Could not load attendance.';
    }
}