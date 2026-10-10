// Student Assignment Submit Page
let currentAssignmentId = null;

document.addEventListener('DOMContentLoaded', function () {
    const urlParams = new URLSearchParams(window.location.search);
    currentAssignmentId = urlParams.get('id');

    if (!currentAssignmentId) {
        document.getElementById('message').textContent = 'No assignment ID provided';
        return;
    }

    loadAssignmentDetails();
    loadMySubmission();

    document.getElementById('submitForm').addEventListener('submit', async function (event) {
        event.preventDefault();
        await submitAssignment();
    });
});

async function loadAssignmentDetails() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');

    try {
        // Get module assignments to find this one
        const response = await fetch(`http://localhost:8080/api/modules/assignments/${currentAssignmentId}`, {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        // Fallback: fetch from module assignments list
        // For now, we'll get it from the URL or show a message
        document.getElementById('assignmentTitle').textContent = 'Assignment #' + currentAssignmentId;

    } catch (error) {
        console.error(error);
    }
}

async function loadMySubmission() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');

    try {
        const response = await fetch(`http://localhost:8080/api/student/assignments/${currentAssignmentId}/submissions/mine`, {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) throw new Error('Failed to load submission');

        const submission = await response.json();

        if (submission && submission.id) {
            const statusDiv = document.getElementById('currentSubmission');
            const statusInfo = document.getElementById('submissionStatus');
            statusDiv.style.display = 'block';

            let statusColor = submission.status === 'GRADED' ? 'green' : (submission.status === 'LATE' ? 'orange' : 'blue');
            let html = `<p><strong>Status:</strong> <span style="color:${statusColor}">${submission.status}</span></p>`;
            html += `<p><strong>Submitted:</strong> ${new Date(submission.submittedAt).toLocaleString()}</p>`;

            if (submission.status === 'GRADED') {
                html += `<p><strong>Marks:</strong> ${submission.marks}</p>`;
                html += `<p><strong>Feedback:</strong> ${submission.feedback || 'No feedback yet'}</p>`;
            }

            statusInfo.innerHTML = html;
            document.getElementById('formTitle').textContent = 'Resubmit Your Work';
        }

    } catch (error) {
        console.error(error);
    }
}

async function submitAssignment() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    const textAnswer = document.getElementById('textAnswer').value;
    const fileInput = document.getElementById('submissionFile');

    if (!textAnswer.trim() && (!fileInput.files || fileInput.files.length === 0)) {
        alert('Please provide a text answer or attach a file');
        return;
    }

    const formData = new FormData();
    formData.append('textAnswer', textAnswer);
    if (fileInput.files && fileInput.files.length > 0) {
        formData.append('file', fileInput.files[0]);
    }

    try {
        const response = await fetch(`http://localhost:8080/api/student/assignments/${currentAssignmentId}/submissions`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}` },
            body: formData
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Submission failed');
        }

        const submission = await response.json();
        alert('Assignment submitted successfully! Status: ' + submission.status);
        loadMySubmission();

    } catch (error) {
        console.error(error);
        alert('Submission failed: ' + error.message);
    }
}