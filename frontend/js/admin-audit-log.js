// Admin Audit Log Page
document.addEventListener('DOMContentLoaded', function () {
    loadAuditLog();
});

async function loadAuditLog() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    const tableBody = document.getElementById('auditLogBody');

    try {
        const response = await fetch('http://localhost:8080/api/admin/audit-log', {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) throw new Error('Failed to fetch audit log');

        const data = await response.json();
        const logs = data.logs || [];

        if (logs.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="5" style="text-align:center;">No audit log entries.</td></tr>';
            return;
        }

        tableBody.innerHTML = logs.map(log => `
            <tr>
                <td>${new Date(log.timestamp).toLocaleString()}</td>
                <td>${log.user?.name || 'System'}</td>
                <td>${log.action}</td>
                <td>${log.targetType || '—'}</td>
                <td>${log.targetId || '—'}</td>
            </tr>
        `).join('');

    } catch (error) {
        console.error(error);
        document.getElementById('message').textContent = 'Could not load audit log.';
    }
}