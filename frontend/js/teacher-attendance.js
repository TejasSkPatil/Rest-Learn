// Teacher Attendance Page
let currentLiveClassId = null;

document.addEventListener('DOMContentLoaded', function () {
    const urlParams = new URLSearchParams(window.location.search);
    currentLiveClassId = urlParams.get('id');

    if (!currentLiveClassId) {
        document.getElementById('message').textContent = 'No live class ID provided';
        return;
    }

    loadLiveClassInfo();
    loadAttendance();
});

async function loadLiveClassInfo() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    const messageEl = document.getElementById('message');

    try {
        const response = await fetch(`http://localhost:8080/api/teacher/live-classes/${currentLiveClassId}`, {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) throw new Error('Failed to fetch live class info');

        const liveClass = await response.json();
        document.getElementById('liveClassTitle').textContent = liveClass.timetable.course.name;
        document.getElementById('liveClassDate').textContent = new Date(liveClass.date).toLocaleDateString();
        
        // Show status badge
        const statusBadge = document.getElementById('liveClassStatus');
        statusBadge.textContent = liveClass.status;
        statusBadge.className = 'badge ' + (liveClass.status === 'LIVE' ? 'badge-active' : liveClass.status === 'SCHEDULED' ? 'badge-draft' : 'badge-deactivate');
        
        // Show meetingRef if available
        document.getElementById('meetingRef').textContent = liveClass.meetingRef ? liveClass.meetingRef : '—';

    } catch (error) {
        console.error(error);
        document.getElementById('message').textContent = 'Could not load live class info.';
    }
}

async function loadAttendance() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    const attendanceTable = document.getElementById('attendanceTableBody');
    const messageEl = document.getElementById('message');

    try {
        const response = await fetch(`http://localhost:8080/api/teacher/live-classes/${currentLiveClassId}/attendance`, {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) throw new Error('Failed to fetch attendance');

        const attendance = await response.json();

        if (attendance.length === 0) {
            attendanceTable.innerHTML = '<tr><td colspan="5" style="text-align:center;">No attendance records yet.</td></tr>';
            return;
        }

        attendanceTable.innerHTML = attendance.map(record => {
            const studentName = record.student.name || '—';
            const statusClass = record.status === 'PRESENT' ? 'badge-active' :
                        record.status === 'LATE' ? 'badge-draft' : 'badge-deactivate';
            return `
                <tr data-id="${record.id}">
                    <td>${studentName}</td>
                    <td>${record.joinTime ? new Date(record.joinTime).toLocaleString() : '—'}</td>
                    <td>
                        <span class="badge ${statusClass}">${record.status}</span>
                    </td>
                    <td>${record.leaveTime ? new Date(record.leaveTime).toLocaleString() : '—'}</td>
                    <td>${record.durationMinutes !== null ? record.durationMinutes + ' min' : '—'}</td>
                </tr>
            `;
        }).join('');

    } catch (error) {
        console.error(error);
        messageEl.textContent = 'Could not load attendance.';
        messageEl.style.color = 'red';
    }
}

function markAllPresent() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    
    if (!currentLiveClassId) return;
    
    fetch(`http://localhost:8080/api/teacher/live-classes/${currentLiveClassId}/attendance/mark-all-present`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` }
    })
    .then(response => {
        if (!response.ok) throw new Error('Failed to mark all present');
        alert('All students marked as present!');
        loadAttendance();
    })
    .catch(error => {
        console.error(error);
        alert('Failed to mark all present: ' + error.message);
    });
}

function toggleAttendanceStatus(rowId, currentStatus) {
    const newStatus = currentStatus === 'PRESENT' ? 'ABSENT' : 'PRESENT';
    
    // Send API call to update attendance
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    
    fetch(`http://localhost:8080/api/teacher/live-classes/${currentLiveClassId}/attendance`, {
        method: 'PUT',
        headers: { 
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ studentId: rowId, status: newStatus })
    })
    .then(response => {
        if (!response.ok) throw new Error('Failed to update attendance');
        loadAttendance();
    })
    .catch(error => {
        console.error(error);
        alert('Failed to update attendance: ' + error.message);
    });
}