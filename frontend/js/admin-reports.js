// Admin Reports Page
document.addEventListener('DOMContentLoaded', function () {
    loadOverview();
});

async function loadOverview() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');

    try {
        const response = await fetch('http://localhost:8080/api/admin/reports/overview', {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) throw new Error('Failed to fetch overview');

        const data = await response.json();

        document.getElementById('totalUsers').textContent = data.totalUsers;
        document.getElementById('totalCourses').textContent = data.totalCourses;
        document.getElementById('totalEnrollments').textContent = data.totalEnrollments;
        document.getElementById('avgAttendance').textContent = data.averageAttendance.toFixed(1) + '%';

        const roleHtml = Object.entries(data.usersByRole || {}).map(([role, count]) => `
            <div class="course-card">
                <h4>${role}</h4>
                <p><strong>${count}</strong> users</p>
            </div>
        `).join('');
        document.getElementById('usersByRole').innerHTML = roleHtml || '<p>No data</p>';

    } catch (error) {
        console.error(error);
        document.getElementById('message').textContent = 'Could not load overview.';
    }
}