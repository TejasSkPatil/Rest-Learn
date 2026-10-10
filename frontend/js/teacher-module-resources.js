// Teacher Module Resources Page
let currentModuleId = null;

document.addEventListener('DOMContentLoaded', function () {
    const urlParams = new URLSearchParams(window.location.search);
    currentModuleId = urlParams.get('moduleId');

    if (!currentModuleId) {
        document.getElementById('message').textContent = 'No module ID provided';
        return;
    }

    loadResources();

    document.getElementById('uploadForm').addEventListener('submit', async function (event) {
        event.preventDefault();
        await uploadResource();
    });
});

async function loadResources() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    const resourcesList = document.getElementById('resourcesList');

    try {
        const response = await fetch(`http://localhost:8080/api/teacher/modules/${currentModuleId}/resources`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error('Failed to fetch resources');
        }

        const resources = await response.json();

        if (resources.length === 0) {
            resourcesList.innerHTML = '<p>No resources uploaded yet.</p>';
            return;
        }

        resourcesList.innerHTML = resources.map(resource => renderResourceCard(resource, 'TEACHER')).join('');

    } catch (error) {
        console.error(error);
        document.getElementById('message').textContent = 'Could not load resources.';
        document.getElementById('message').style.color = 'red';
    }
}

async function uploadResource() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    const title = document.getElementById('resourceTitle').value;
    const fileInput = document.getElementById('resourceFile');

    if (!title.trim()) {
        alert('Please enter a resource title');
        return;
    }

    if (!fileInput.files || fileInput.files.length === 0) {
        alert('Please select a file');
        return;
    }

    const formData = new FormData();
    formData.append('title', title);
    formData.append('file', fileInput.files[0]);

    try {
        const response = await fetch(`http://localhost:8080/api/teacher/modules/${currentModuleId}/resources`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            },
            body: formData
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Upload failed');
        }

        const resource = await response.json();
        alert('Resource uploaded successfully!');
        document.getElementById('uploadForm').reset();
        loadResources();

    } catch (error) {
        console.error(error);
        alert('Upload failed: ' + error.message);
    }
}

async function deleteResource(resourceId) {
    if (!confirm('Are you sure you want to delete this resource?')) {
        return;
    }

    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');

    try {
        const response = await fetch(`http://localhost:8080/api/teacher/modules/${currentModuleId}/resources/${resourceId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error('Delete failed');
        }

        alert('Resource deleted successfully!');
        loadResources();

    } catch (error) {
        console.error(error);
        alert('Delete failed: ' + error.message);
    }
}