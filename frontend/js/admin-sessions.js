// Admin Academic Sessions Page
document.addEventListener('DOMContentLoaded', function () {
    loadSessions();

    document.getElementById('createForm').addEventListener('submit', async function (event) {
        event.preventDefault();
        await createSession();
    });
});

async function loadSessions() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    const tableBody = document.getElementById('sessionsTableBody');

    try {
        const response = await fetch('http://localhost:8080/api/admin/academic-sessions', {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) throw new Error('Failed to fetch sessions');

        const sessions = await response.json();

        if (sessions.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="5" style="text-align:center;">No sessions yet.</td></tr>';
            return;
        }

        tableBody.innerHTML = sessions.map(s => `
            <tr>
                <td>${s.id}</td>
                <td>${s.name}</td>
                <td>${s.startDate}</td>
                <td>${s.endDate}</td>
                <td>
                    <button onclick="deleteSession(${s.id})" class="btn-action btn-deactivate">Delete</button>
                </td>
            </tr>
        `).join('');

    } catch (error) {
        console.error(error);
        document.getElementById('message').textContent = 'Could not load sessions.';
        document.getElementById('message').style.color = 'red';
    }
}

async function createSession() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    const name = document.getElementById('sessionName').value.trim();
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;

    if (!name || !startDate || !endDate) {
        alert('Please fill in all fields');
        return;
    }

    try {
        const response = await fetch('http://localhost:8080/api/admin/academic-sessions', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ name, startDate, endDate })
        });

        if (!response.ok) throw new Error('Failed to create session');

        alert('Session created successfully!');
        document.getElementById('createForm').reset();
        loadSessions();

    } catch (error) {
        console.error(error);
        alert('Failed: ' + error.message);
    }
}

async function deleteSession(id) {
    if (!confirm('Are you sure you want to delete this session?')) return;

    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');

    try {
        const response = await fetch(`http://localhost:8080/api/admin/academic-sessions/${id}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) throw new Error('Delete failed');

        alert('Session deleted successfully!');
        loadSessions();

    } catch (error) {
        console.error(error);
        alert('Delete failed: ' + error.message);
    }
}