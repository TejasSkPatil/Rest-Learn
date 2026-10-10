// Admin Departments Page
document.addEventListener('DOMContentLoaded', function () {
    loadDepartments();

    document.getElementById('createForm').addEventListener('submit', async function (event) {
        event.preventDefault();
        await createDepartment();
    });
});

async function loadDepartments() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    const tableBody = document.getElementById('departmentsTableBody');

    try {
        const response = await fetch('http://localhost:8080/api/admin/departments', {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) throw new Error('Failed to fetch departments');

        const departments = await response.json();

        if (departments.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="3" style="text-align:center;">No departments yet.</td></tr>';
            return;
        }

        tableBody.innerHTML = departments.map(dept => `
            <tr>
                <td>${dept.id}</td>
                <td>${dept.name}</td>
                <td>
                    <button onclick="deleteDepartment(${dept.id})" class="btn-action btn-deactivate">Delete</button>
                </td>
            </tr>
        `).join('');

    } catch (error) {
        console.error(error);
        document.getElementById('message').textContent = 'Could not load departments.';
        document.getElementById('message').style.color = 'red';
    }
}

async function createDepartment() {
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    const name = document.getElementById('deptName').value.trim();

    if (!name) {
        alert('Please enter a department name');
        return;
    }

    try {
        const response = await fetch('http://localhost:8080/api/admin/departments', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ name: name })
        });

        if (!response.ok) throw new Error('Failed to create department');

        alert('Department created successfully!');
        document.getElementById('deptName').value = '';
        loadDepartments();

    } catch (error) {
        console.error(error);
        alert('Failed: ' + error.message);
    }
}

async function deleteDepartment(id) {
    if (!confirm('Are you sure you want to delete this department?')) return;

    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');

    try {
        const response = await fetch(`http://localhost:8080/api/admin/departments/${id}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) throw new Error('Delete failed');

        alert('Department deleted successfully!');
        loadDepartments();

    } catch (error) {
        console.error(error);
        alert('Delete failed: ' + error.message);
    }
}