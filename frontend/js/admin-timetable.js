// Admin Timetable Page
document.addEventListener('DOMContentLoaded', function () {
    loadTimetable();
    loadFormOptions();

    document.getElementById('createForm').addEventListener('submit', async function (event) {
        event.preventDefault();
        await createEntry();
    });
});

async function loadTimetable() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    const container = document.getElementById('timetableContainer');

    try {
        const response = await fetch('http://localhost:8080/api/admin/timetable', {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) throw new Error('Failed to fetch timetable');

        const entries = await response.json();
        container.innerHTML = renderTimetableGrid(entries, 'ADMIN');

    } catch (error) {
        console.error(error);
        container.innerHTML = '<p style="color:red;">Could not load timetable.</p>';
    }
}

async function loadFormOptions() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');

    try {
        // Load courses
        const coursesRes = await fetch('http://localhost:8080/api/admin/courses', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (coursesRes.ok) {
            const courses = await coursesRes.json();
            document.getElementById('courseId').innerHTML = courses.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
        }

        // Load sessions
        const sessionsRes = await fetch('http://localhost:8080/api/admin/academic-sessions', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (sessionsRes.ok) {
            const sessions = await sessionsRes.json();
            document.getElementById('sessionId').innerHTML = sessions.map(s => `<option value="${s.id}">${s.name}</option>`).join('');
        }

        // Load teachers
        const usersRes = await fetch('http://localhost:8080/api/admin/users', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (usersRes.ok) {
            const users = await usersRes.json();
            const teachers = users.filter(u => u.role === 'TEACHER');
            document.getElementById('teacherId').innerHTML = teachers.map(t => `<option value="${t.id}">${t.name}</option>`).join('');
        }

    } catch (error) {
        console.error(error);
    }
}

async function createEntry() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    const messageEl = document.getElementById('message');

    const entry = {
        course: { id: parseInt(document.getElementById('courseId').value) },
        teacher: { id: parseInt(document.getElementById('teacherId').value) },
        academicSession: { id: parseInt(document.getElementById('sessionId').value) },
        dayOfWeek: document.getElementById('dayOfWeek').value,
        startTime: document.getElementById('startTime').value,
        endTime: document.getElementById('endTime').value
    };

    try {
        const response = await fetch('http://localhost:8080/api/admin/timetable', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(entry)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText);
        }

        alert('Timetable entry added successfully!');
        document.getElementById('createForm').reset();
        loadTimetable();

    } catch (error) {
        console.error(error);
        messageEl.textContent = error.message;
        messageEl.style.color = 'red';
    }
}