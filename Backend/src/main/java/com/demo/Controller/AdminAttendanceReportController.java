package com.example.demo.controller;

import com.example.demo.entity.Attendance;
import com.example.demo.entity.User;
import com.example.demo.enums.AttendanceStatus;
import com.example.demo.enums.Role;
import com.example.demo.repository.AttendanceRepository;
import com.example.demo.repository.UserRepo;
import com.example.demo.service.AttendanceStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class AdminAttendanceReportController {

    private final AttendanceRepository AttendanceRepository;
    private final AttendanceStatsService statsService;
    private final UserRepo userRepo;

    public AdminAttendanceReportController(AttendanceRepository AttendanceRepository, AttendanceStatsService statsService, UserRepo userRepo) {
        this.AttendanceRepository = AttendanceRepository;
        this.statsService = statsService;
        this.userRepo = userRepo;
    }

    // Admin: Get attendance reports with filters
    @GetMapping("/api/admin/attendance/reports")
    public ResponseEntity<?> getReports(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long departmentId) {

        User admin = getAuthenticatedUser();
        if (admin == null) return ResponseEntity.status(401).body("Not authenticated");
        if (admin.getRole() != Role.ADMIN) return ResponseEntity.status(403).body("Admin access required");

        List<Attendance> allRecords = AttendanceRepository.findAll();

        // Filter by course if specified
        if (courseId != null) {
            allRecords = allRecords.stream()
                    .filter(a -> a.getLiveClass().getTimetable().getCourse().getId().equals(courseId))
                    .toList();
        }

        int present = 0, absent = 0, late = 0;
        for (Attendance a : allRecords) {
            if (a.getStatus() == AttendanceStatus.PRESENT) present++;
            else if (a.getStatus() == AttendanceStatus.ABSENT) absent++;
            else if (a.getStatus() == AttendanceStatus.LATE) late++;
        }

        double total = present + absent + late;
        double percentage = total == 0 ? 0 : (present / total) * 100;

        Map<String, Object> report = new HashMap<>();
        report.put("totalRecords", allRecords.size());
        report.put("present", present);
        report.put("absent", absent);
        report.put("late", late);
        report.put("percentage", percentage);

        return ResponseEntity.ok(report);
    }

    private User getAuthenticatedUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepo.findByEmail(auth.getName()).orElse(null);
    }
}