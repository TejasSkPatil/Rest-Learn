// Teacher Live Class Attendance Page
let currentLiveClassId = null;

document.addEventListener('DOMContentLoaded', function () {
    const urlParams = new URLSearchParams(window.location.search);
    currentLiveClassId = urlParams.get('id');
    if (currentLiveClassId) loadAttendance();
});

async function loadAttendance() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    const res = await fetch(`http://localhost:8080/api/teacher/live-classes/${currentLiveClassId}/attendance`, {
        headers: { 'Authorization': `Bearer ${token}` }
    });
    if (!res.ok) { alert('Failed to load attendance'); return; }
    const records = await res.json();
    document.getElementById('attendanceTable').innerHTML = renderAttendanceTable(records, true);
}

async function markAttendance(studentId, status) {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    const res = await fetch(`http://localhost:8080/api/teacher/live-classes/${currentLiveClassId}/attendance`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
        body: JSON.stringify({ studentId, status })
    });
    if (res.ok) loadAttendance();
    else alert('Failed to mark attendance');
}

async function markAllPresent() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    const res = await fetch(`http://localhost:8080/api/teacher/live-classes/${currentLiveClassId}/attendance/mark-all-present`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` }
    });
    if (res.ok) { alert('All marked present!'); loadAttendance(); }
    else alert('Failed');
}