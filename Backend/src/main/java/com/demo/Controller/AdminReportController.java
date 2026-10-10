package com.example.demo.controller;

import com.example.demo.entity.*;
import com.example.demo.enums.Role;
import com.example.demo.repository.*;
import com.example.demo.service.AttendanceStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class AdminReportController {

    private final UserRepo userRepo;
    private final CourseRepo courseRepo;
    private final EnrollmentRepository EnrollmentRepository;
    private final AttendanceRepository AttendanceRepository;
    private final AuditLogRepository auditLogRepository;
    private final AttendanceStatsService attendanceStatsService;

    public AdminReportController(UserRepo userRepo, CourseRepo courseRepo, EnrollmentRepository EnrollmentRepository,
                                 AttendanceRepository AttendanceRepository, AuditLogRepository auditLogRepository,
                                 AttendanceStatsService attendanceStatsService) {
        this.userRepo = userRepo;
        this.courseRepo = courseRepo;
        this.EnrollmentRepository = EnrollmentRepository;
        this.AttendanceRepository = AttendanceRepository;
        this.auditLogRepository = auditLogRepository;
        this.attendanceStatsService = attendanceStatsService;
    }

    // Admin: System overview report
    @GetMapping("/api/admin/reports/overview")
    public ResponseEntity<?> getOverview() {
        User admin = getAuthenticatedUser();
        if (admin == null) return ResponseEntity.status(401).body("Not authenticated");
        if (admin.getRole() != Role.ADMIN) return ResponseEntity.status(403).body("Admin access required");

        Map<String, Object> report = new HashMap<>();

        // Users by role
        List<User> allUsers = userRepo.findAll();
        Map<String, Long> usersByRole = new HashMap<>();
        for (User u : allUsers) {
            usersByRole.put(u.getRole().name(), usersByRole.getOrDefault(u.getRole().name(), 0L) + 1);
        }
        report.put("usersByRole", usersByRole);
        report.put("totalUsers", allUsers.size());

        // Courses
        report.put("totalCourses", courseRepo.count());

        // Enrollments
        report.put("totalEnrollments", EnrollmentRepository.count());

        // Average attendance
        List<Attendance> allAttendance = AttendanceRepository.findAll();
        long present = allAttendance.stream().filter(a -> a.getStatus() == com.example.demo.enums.AttendanceStatus.PRESENT).count();
        long total = allAttendance.size();
        report.put("averageAttendance", total == 0 ? 0.0 : ((double) present / total) * 100);

        return ResponseEntity.ok(report);
    }

    // Admin: Audit log (paginated)
    @GetMapping("/api/admin/audit-log")
    public ResponseEntity<?> getAuditLog(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        User admin = getAuthenticatedUser();
        if (admin == null) return ResponseEntity.status(401).body("Not authenticated");
        if (admin.getRole() != Role.ADMIN) return ResponseEntity.status(403).body("Admin access required");

        List<AuditLog> logs;
        if (userId != null) {
            logs = auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
        } else if (action != null) {
            logs = auditLogRepository.findByActionOrderByTimestampDesc(action);
        } else {
            logs = auditLogRepository.findAll();
        }

        // Simple pagination
        int start = page * size;
        int end = Math.min(start + size, logs.size());
        List<AuditLog> paginated = start < logs.size() ? logs.subList(start, end) : List.of();

        Map<String, Object> result = new HashMap<>();
        result.put("logs", paginated);
        result.put("total", logs.size());
        result.put("page", page);
        result.put("size", size);

        return ResponseEntity.ok(result);
    }

    private User getAuthenticatedUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepo.findByEmail(auth.getName()).orElse(null);
    }
}