// Student Module Resources Page
let currentModuleId = null;

document.addEventListener('DOMContentLoaded', function () {
    const urlParams = new URLSearchParams(window.location.search);
    currentModuleId = urlParams.get('moduleId');

    if (!currentModuleId) {
        document.getElementById('message').textContent = 'No module ID provided';
        return;
    }

    loadResources();
});

async function loadResources() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    const resourcesList = document.getElementById('resourcesList');

    try {
        const response = await fetch(`http://localhost:8080/api/modules/${currentModuleId}/resources`, {
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
            resourcesList.innerHTML = '<p>No resources available for this module yet.</p>';
            return;
        }

        resourcesList.innerHTML = resources.map(resource => renderResourceCard(resource, 'STUDENT')).join('');

    } catch (error) {
        console.error(error);
        document.getElementById('message').textContent = 'Could not load resources.';
        document.getElementById('message').style.color = 'red';
    }
}

async function downloadResource(resourceId, title) {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');

    try {
        const response = await fetch(`http://localhost:8080/api/resources/${resourceId}/download`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error('Download failed');
        }

        const blob = await response.blob();
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = title || 'resource';
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);

    } catch (error) {
        console.error(error);
        alert('Download failed: ' + error.message);
    }
}