// Admin Attendance Rules Page
document.addEventListener('DOMContentLoaded', function () {
    loadRule();
    document.getElementById('ruleForm').addEventListener('submit', saveRule);
});

async function loadRule() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    const res = await fetch('http://localhost:8080/api/admin/attendance-rules/global', {
        headers: { 'Authorization': `Bearer ${token}` }
    });
    if (res.ok) {
        const rule = await res.json();
        document.getElementById('windowMinutes').value = rule.windowMinutes;
        document.getElementById('minDuration').value = rule.minDurationMinutes || '';
        document.getElementById('lateThreshold').value = rule.lateThresholdMinutes || '';
    }
}

async function saveRule(e) {
    e.preventDefault();
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    const rule = {
        windowMinutes: parseInt(document.getElementById('windowMinutes').value),
        minDurationMinutes: document.getElementById('minDuration').value ? parseInt(document.getElementById('minDuration').value) : null,
        lateThresholdMinutes: document.getElementById('lateThreshold').value ? parseInt(document.getElementById('lateThreshold').value) : null
    };
    const res = await fetch('http://localhost:8080/api/admin/attendance-rules/global', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
        body: JSON.stringify(rule)
    });
    if (res.ok) alert('Rule saved!');
    else alert('Failed to save rule');
}