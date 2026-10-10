package com.example.demo.controller;

import com.example.demo.entity.Attendance;
import com.example.demo.entity.User;
import com.example.demo.enums.AttendanceStatus;
import com.example.demo.repository.UserRepo;
import com.example.demo.service.AttendanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final UserRepo userRepo;

    public AttendanceController(AttendanceService attendanceService, UserRepo userRepo) {
        this.attendanceService = attendanceService;
        this.userRepo = userRepo;
    }

    // Teacher: Get attendance list for a live class (pre-filled with enrolled students)
    @GetMapping("/api/teacher/live-classes/{id}/attendance")
    public ResponseEntity<?> getAttendance(@PathVariable Long id) {
        User teacher = getAuthenticatedUser();
        if (teacher == null) return ResponseEntity.status(401).body("Not authenticated");

        try {
            List<Attendance> attendance = attendanceService.getAttendanceForLiveClass(id, teacher.getId());
            return ResponseEntity.ok(attendance);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Teacher: Mark one student's attendance
    @PutMapping("/api/teacher/live-classes/{id}/attendance")
    public ResponseEntity<?> markAttendance(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        User teacher = getAuthenticatedUser();
        if (teacher == null) return ResponseEntity.status(401).body("Not authenticated");

        Long studentId = ((Number) request.get("studentId")).longValue();
        String statusStr = (String) request.get("status");
        AttendanceStatus status = AttendanceStatus.valueOf(statusStr);

        try {
            Attendance attendance = attendanceService.markAttendance(id, studentId, status, teacher.getId());
            return ResponseEntity.ok(attendance);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Teacher: Mark all present
    @PostMapping("/api/teacher/live-classes/{id}/attendance/mark-all-present")
    public ResponseEntity<?> markAllPresent(@PathVariable Long id) {
        User teacher = getAuthenticatedUser();
        if (teacher == null) return ResponseEntity.status(401).body("Not authenticated");

        try {
            attendanceService.markAllPresent(id, teacher.getId());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private User getAuthenticatedUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        String email = auth.getName();
        return userRepo.findByEmail(email).orElse(null);
    }
}