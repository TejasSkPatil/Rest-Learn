// Teacher Course Detail Page
let currentCourseId = null;
let isCodeActive = true;

document.addEventListener('DOMContentLoaded', function () {
    const urlParams = new URLSearchParams(window.location.search);
    currentCourseId = urlParams.get('id');

    if (!currentCourseId) {
        document.getElementById('message').textContent = 'No course ID provided';
        return;
    }

    loadCourseDetails();
});

async function loadCourseDetails() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');

    try {
        const response = await fetch(`http://localhost:8080/api/teacher/courses/${currentCourseId}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error('Failed to load course details');
        }

        const course = await response.json();

        document.getElementById('courseName').textContent = course.name;
        document.getElementById('courseCode').textContent = course.courseCode;
        document.getElementById('courseDepartment').textContent = course.department;
        document.getElementById('courseSemester').textContent = course.semester;
        document.getElementById('courseDescription').textContent = course.description || 'No description';
        document.getElementById('courseCreated').textContent = new Date(course.createdAt).toLocaleDateString();
        document.getElementById('enrollmentCode').textContent = course.enrollmentCode;

        isCodeActive = course.codeActive;
        updateCodeStatusUI();

    } catch (error) {
        console.error(error);
        document.getElementById('message').textContent = 'Could not load course details.';
        document.getElementById('message').style.color = 'red';
    }
}

function updateCodeStatusUI() {
    const statusEl = document.getElementById('codeStatus');
    const toggleBtn = document.getElementById('toggleCodeBtn');

    if (isCodeActive) {
        statusEl.textContent = 'ACTIVE';
        statusEl.className = 'status-badge status-active';
        toggleBtn.textContent = 'Deactivate Code';
        toggleBtn.className = 'btn-action btn-deactivate';
    } else {
        statusEl.textContent = 'INACTIVE';
        statusEl.className = 'status-badge status-inactive';
        toggleBtn.textContent = 'Activate Code';
        toggleBtn.className = 'btn-action btn-activate';
    }
}

async function regenerateCode() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');

    try {
        const response = await fetch(`http://localhost:8080/api/teacher/courses/${currentCourseId}/regenerate-code`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error('Failed to regenerate code');
        }

        const course = await response.json();
        document.getElementById('enrollmentCode').textContent = course.enrollmentCode;
        isCodeActive = course.codeActive;
        updateCodeStatusUI();

        alert('New enrollment code generated: ' + course.enrollmentCode);

    } catch (error) {
        console.error(error);
        alert('Failed to regenerate code: ' + error.message);
    }
}

async function toggleCodeStatus() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');

    try {
        const response = await fetch(`http://localhost:8080/api/teacher/courses/${currentCourseId}/code-status?active=${!isCodeActive}`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error('Failed to update code status');
        }

        const course = await response.json();
        isCodeActive = course.codeActive;
        updateCodeStatusUI();

    } catch (error) {
        console.error(error);
        alert('Failed to update code status: ' + error.message);
    }
}