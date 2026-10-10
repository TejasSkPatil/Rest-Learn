// Student Module Videos Page
// Uses fetch+blob approach for JWT-authenticated video streaming
// Note: <video src> cannot send Authorization headers, so we fetch the video
// as a blob with the JWT header, then create an object URL for the video element.
// Improvement point: short-lived signed URLs would avoid loading entire video into memory.

let currentModuleId = null;

document.addEventListener('DOMContentLoaded', function () {
    const urlParams = new URLSearchParams(window.location.search);
    currentModuleId = urlParams.get('moduleId');

    if (!currentModuleId) {
        document.getElementById('message').textContent = 'No module ID provided';
        return;
    }

    loadVideos();
});

async function loadVideos() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    const videosList = document.getElementById('videosList');

    try {
        const response = await fetch(`http://localhost:8080/api/modules/${currentModuleId}/videos`, {
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
            videosList.innerHTML = '<p>No videos available for this module yet.</p>';
            return;
        }

        videosList.innerHTML = videos.map(video => renderVideoCard(video, 'STUDENT')).join('');

    } catch (error) {
        console.error(error);
        document.getElementById('message').textContent = 'Could not load videos.';
        document.getElementById('message').style.color = 'red';
    }
}

async function playVideo(videoId, title) {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    const playerContainer = document.getElementById('videoPlayerContainer');
    const videoPlayer = document.getElementById('videoPlayer');
    const playerTitle = document.getElementById('playerTitle');

    try {
        // Fetch video as blob with JWT auth header
        const response = await fetch(`http://localhost:8080/api/videos/${videoId}/stream`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error('Failed to load video');
        }

        const blob = await response.blob();
        const videoUrl = URL.createObjectURL(blob);

        // Revoke previous object URL to free memory
        if (videoPlayer.src) {
            URL.revokeObjectURL(videoPlayer.src);
        }

        videoPlayer.src = videoUrl;
        playerTitle.textContent = title;
        playerContainer.style.display = 'block';
        videoPlayer.play();

        // Scroll to player
        playerContainer.scrollIntoView({ behavior: 'smooth' });

    } catch (error) {
        console.error(error);
        alert('Could not load video: ' + error.message);
    }
}