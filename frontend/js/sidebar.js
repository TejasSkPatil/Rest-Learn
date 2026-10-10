// Sidebar functionality with role-based menu
document.addEventListener('DOMContentLoaded', function() {
    // Check for JWT token to determine role
    const token = localStorage.getItem('jwtToken') || sessionStorage.getItem('jwtToken');
    
    if (!token) {
        // No token, hide sidebar
        const sidebar = document.querySelector('.sidebar');
        if (sidebar) {
            sidebar.style.display = 'none';
        }
        return;
    }
    
    // Parse JWT to get role
    let role = '';
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const payload = JSON.parse(atob(base64));
        role = payload.role || '';
    } catch (e) {
        console.error('Could not decode token', e);
    }
    
    // Render role-specific navigation
    const navLinks = document.querySelector('.sidebar-nav');
    if (!navLinks) return;
    
    // Define navigation items per role (no icons to avoid Font Awesome dependency)
    const navItems = {
        ADMIN: [
            { label: 'Users', href: 'admin-users.html' },
            { label: 'Timetable', href: 'timetable.html' },
            { label: 'Reports', href: 'reports.html' }
        ],
        TEACHER: [
            { label: 'My Courses', href: 'teacher-dashboard.html' },
            { label: 'Attendance', href: 'teacher-attendance.html' },
            { label: 'Timetable', href: 'timetable.html' }
        ],
        STUDENT: [
            { label: 'My Courses', href: 'student-dashboard.html' },
            { label: 'Join Course', href: 'student-join-course.html' },
            { label: 'Attendance', href: 'student-attendance.html' },
            { label: 'Timetable', href: 'timetable.html' }
        ]
    };
    
    // Generate navigation HTML based on role
    const items = navItems[role] || navItems.STUDENT; // Default to STUDENT if role not recognized
    
    let html = '';
    items.forEach(item => {
        html += `<li><a href="${item.href}">${item.label}</a></li>`;
    });
    
    navLinks.innerHTML = html;
});