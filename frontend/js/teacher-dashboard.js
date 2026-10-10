// Teacher Dashboard - Today's Classes
async function loadTodaysClasses() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    const container = document.getElementById('todaysClasses');

    try {
        const response = await fetch('http://localhost:8080/api/teacher/live-classes/today', {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) throw new Error('Failed to fetch classes');

        const classes = await response.json();

        if (classes.length === 0) {
            container.innerHTML = '<p>No classes scheduled for today.</p>';
            return;
        }

        container.innerHTML = classes.map(lc => `
            <div class="live-class-item">
                <strong>${lc.timetable?.course?.name || 'Course'}</strong>
                <span>${lc.timetable?.startTime} - ${lc.timetable?.endTime}</span>
                <span class="badge ${lc.status === 'LIVE' ? 'badge-active' : 'badge-draft'}">${lc.status}</span>
                ${lc.status === 'SCHEDULED' ? `<button onclick="startClass(${lc.id})" class="btn-action btn-activate">Start Class</button>` : ''}
                ${lc.status === 'LIVE' ? `<button onclick="endClass(${lc.id})" class="btn-action btn-deactivate">End Class</button>` : ''}
            </div>
        `).join('');

    } catch (error) {
        console.error(error);
        container.innerHTML = '<p style="color:red;">Could not load classes.</p>';
    }
}

async function startClass(classId) {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    try {
        const response = await fetch(`http://localhost:8080/api/teacher/live-classes/${classId}/start`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error('Failed to start class');
        alert('Class started!');
        loadTodaysClasses();
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

async function endClass(classId) {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    try {
        const response = await fetch(`http://localhost:8080/api/teacher/live-classes/${classId}/end`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error('Failed to end class');
        alert('Class ended!');
        loadTodaysClasses();
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

async function loadTeacherTimetable() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    const container = document.getElementById('teacherTimetable');

    try {
        const response = await fetch('http://localhost:8080/api/timetable/mine', {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) throw new Error('Failed to fetch timetable');

        const entries = await response.json();
        container.innerHTML = renderTimetableGrid(entries, 'TEACHER');

    } catch (error) {
        console.error(error);
        container.innerHTML = '<p style="color:red;">Could not load timetable.</p>';
    }
}

// Initialize
document.addEventListener('DOMContentLoaded', function () {
    loadTodaysClasses();
    loadTeacherTimetable();
});