// Teacher Module Videos Page
let currentModuleId = null;

document.addEventListener('DOMContentLoaded', function () {
    const urlParams = new URLSearchParams(window.location.search);
    currentModuleId = urlParams.get('moduleId');

    if (!currentModuleId) {
        document.getElementById('message').textContent = 'No module ID provided';
        return;
    }

    loadVideos();

    document.getElementById('uploadForm').addEventListener('submit', async function (event) {
        event.preventDefault();
        await uploadVideo();
    });
});

async function loadVideos() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    const videosList = document.getElementById('videosList');

    try {
        const response = await fetch(`http://localhost:8080/api/teacher/modules/${currentModuleId}/videos`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error('Failed to fetch videos');
        }

        const videos = await response.json();

        if (videos.length === 0) {
            videosList.innerHTML = '<p>No videos uploaded yet.</p>';
            return;
        }

        videosList.innerHTML = videos.map(video => renderVideoCard(video, 'TEACHER')).join('');

    } catch (error) {
        console.error(error);
        document.getElementById('message').textContent = 'Could not load videos.';
        document.getElementById('message').style.color = 'red';
    }
}

async function uploadVideo() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    const title = document.getElementById('videoTitle').value;
    const fileInput = document.getElementById('videoFile');

    if (!title.trim()) {
        alert('Please enter a video title');
        return;
    }

    if (!fileInput.files || fileInput.files.length === 0) {
        alert('Please select a video file');
        return;
    }

    const formData = new FormData();
    formData.append('title', title);
    formData.append('file', fileInput.files[0]);

    try {
        const response = await fetch(`http://localhost:8080/api/teacher/modules/${currentModuleId}/videos`, {
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

        const video = await response.json();
        alert('Video uploaded successfully!');
        document.getElementById('uploadForm').reset();
        loadVideos();

    } catch (error) {
        console.error(error);
        alert('Upload failed: ' + error.message);
    }
}

async function deleteVideo(videoId) {
    if (!confirm('Are you sure you want to delete this video?')) {
        return;
    }

    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');

    try {
        const response = await fetch(`http://localhost:8080/api/teacher/modules/${currentModuleId}/videos/${videoId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error('Delete failed');
        }

        alert('Video deleted successfully!');
        loadVideos();

    } catch (error) {
        console.error(error);
        alert('Delete failed: ' + error.message);
    }
}