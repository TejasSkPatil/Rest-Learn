// Teacher Module Assignments Page
let currentModuleId = null;

document.addEventListener('DOMContentLoaded', function () {
    const urlParams = new URLSearchParams(window.location.search);
    currentModuleId = urlParams.get('moduleId');

    if (!currentModuleId) {
        document.getElementById('message').textContent = 'No module ID provided';
        return;
    }

    loadAssignments();

    document.getElementById('createForm').addEventListener('submit', async function (event) {
        event.preventDefault();
        await createAssignment();
    });
});

async function loadAssignments() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    const assignmentsList = document.getElementById('assignmentsList');

    try {
        const response = await fetch(`http://localhost:8080/api/teacher/modules/${currentModuleId}/assignments`, {
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
            assignmentsList.innerHTML = '<p>No assignments created yet.</p>';
            return;
        }

        assignmentsList.innerHTML = assignments.map(assignment => renderAssignmentCard(assignment, 'TEACHER')).join('');

    } catch (error) {
        console.error(error);
        document.getElementById('message').textContent = 'Could not load assignments.';
        document.getElementById('message').style.color = 'red';
    }
}

async function createAssignment() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    const title = document.getElementById('assignmentTitle').value;
    const description = document.getElementById('assignmentDescription').value;
    const dueDate = document.getElementById('dueDate').value;
    const maxMarks = document.getElementById('maxMarks').value;

    if (!title.trim()) {
        alert('Please enter an assignment title');
        return;
    }

    const assignment = {
        title: title,
        description: description,
        dueDate: dueDate,
        maxMarks: parseInt(maxMarks)
    };

    try {
        const response = await fetch(`http://localhost:8080/api/teacher/modules/${currentModuleId}/assignments`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(assignment)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Create failed');
        }

        const created = await response.json();
        alert('Assignment created successfully!');
        document.getElementById('createForm').reset();
        loadAssignments();

    } catch (error) {
        console.error(error);
        alert('Create failed: ' + error.message);
    }
}

async function deleteAssignment(assignmentId) {
    if (!confirm('Are you sure you want to delete this assignment?')) {
        return;
    }

    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');

    try {
        const response = await fetch(`http://localhost:8080/api/teacher/modules/${currentModuleId}/assignments/${assignmentId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error('Delete failed');
        }

        alert('Assignment deleted successfully!');
        loadAssignments();

    } catch (error) {
        console.error(error);
        alert('Delete failed: ' + error.message);
    }
}