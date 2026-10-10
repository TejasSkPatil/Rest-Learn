// Teacher Assignment Submissions Page
let currentAssignmentId = null;

document.addEventListener('DOMContentLoaded', function () {
    const urlParams = new URLSearchParams(window.location.search);
    currentAssignmentId = urlParams.get('assignmentId');

    if (!currentAssignmentId) {
        document.getElementById('message').textContent = 'No assignment ID provided';
        return;
    }

    loadAssignmentInfo();
    loadSubmissions();
});

async function loadAssignmentInfo() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    const assignmentTitle = document.getElementById('assignmentTitle');
    const assignmentId = document.getElementById('assignmentId');

    try {
        const response = await fetch(`http://localhost:8080/api/teacher/assignments/${currentAssignmentId}`, {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) {
            throw new Error('Failed to fetch assignment');
        }

        const assignment = await response.json();
        assignmentTitle.textContent = assignment.title;
        assignmentId.textContent = assignment.id;

    } catch (error) {
        console.error(error);
        document.getElementById('message').textContent = 'Could not load assignment info.';
    }
}

async function loadSubmissions() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    const submissionsList = document.getElementById('submissionsList');

    try {
        const response = await fetch(`http://localhost:8080/api/teacher/assignments/${currentAssignmentId}/submissions`, {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) {
            throw new Error('Failed to fetch submissions');
        }

        const submissions = await response.json();

        if (submissions.length === 0) {
            submissionsList.innerHTML = '<p>No submissions yet.</p>';
            return;
        }

        submissionsList.innerHTML = submissions.map(submission => renderSubmissionCard(submission)).join('');
    } catch (error) {
        console.error(error);
        document.getElementById('message').textContent = 'Could not load submissions.';
        document.getElementById('message').style.color = 'red';
    }
}

function renderSubmissionCard(submission) {
    const statusClass = submission.status === 'GRADED' ? 'status-graded' :
                        submission.status === 'LATE' ? 'status-late' : 'status-submitted';
    const statusText = submission.status;

    return `
        <div class="submission-card" data-submission-id="${submission.id}">
            <div class="submission-header">
                <strong>Student:</strong> ${submission.studentName || submission.studentId || '—'}
            </div>
            <div class="submission-body">
                <p><strong>Submitted:</strong> ${submission.submittedAt ? new Date(submission.submittedAt).toLocaleString() : '—'}</p>
                <p><strong>Status:</strong> <span class="${statusClass}">${statusText}</span></p>
                ${submission.textAnswer ? `<p><strong>Text Answer:</strong> ${submission.textAnswer}</p>` : ''}
                ${submission.fileRef ? `<p><strong>File:</strong> ${submission.fileRef}</p>` : ''}
                ${submission.marks !== undefined && submission.marks !== null ? `<p><strong>Marks:</strong> ${submission.marks}</p>` : ''}
                ${submission.feedback ? `<p><strong>Feedback:</strong> ${submission.feedback}</p>` : ''}
            </div>
        </div>
    `;
}