// Student Module Assignments Page
let currentModuleId = null;

document.addEventListener('DOMContentLoaded', function () {
    const urlParams = new URLSearchParams(window.location.search);
    currentModuleId = urlParams.get('moduleId');

    if (!currentModuleId) {
        document.getElementById('message').textContent = 'No module ID provided';
        return;
    }

    loadAssignments();
});

// Extract student ID from JWT token
let studentId = null;
try {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    if (token) {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const payload = JSON.parse(atob(base64));
        studentId = payload.sub || payload.id || null;
    }
} catch (e) {
    console.error('Could not decode student ID from token', e);
}

async function loadAssignments() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    const assignmentsList = document.getElementById('assignmentsList');

    try {
        const response = await fetch(`http://localhost:8080/api/modules/${currentModuleId}/assignments`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error('Failed to fetch assignments');
        }

        const assignments = await response.json();

        if (assignments.length === 0) {
            assignmentsList.innerHTML = '<p>No assignments for this module yet.</p>';
            return;
        }

        assignmentsList.innerHTML = assignments.map(assignment => 
            renderAssignmentCard(assignment, 'STUDENT', studentId)
        ).join('');

    } catch (error) {
        console.error(error);
        document.getElementById('message').textContent = 'Could not load assignments.';
        document.getElementById('message').style.color = 'red';
    }
}