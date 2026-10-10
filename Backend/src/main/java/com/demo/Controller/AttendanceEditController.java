package com.example.demo.controller;

import com.example.demo.entity.AttendanceEdit;
import com.example.demo.entity.User;
import com.example.demo.enums.Role;
import com.example.demo.repository.AttendanceEditRepository;
import com.example.demo.repository.UserRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AttendanceEditController {

    private final AttendanceEditRepository editRepository;
    private final UserRepo userRepo;

    public AttendanceEditController(AttendanceEditRepository editRepository, UserRepo userRepo) {
        this.editRepository = editRepository;
        this.userRepo = userRepo;
    }

    // Admin: View edit history for an attendance record
    @GetMapping("/api/admin/attendance/{id}/edits")
    public ResponseEntity<?> getEditHistory(@PathVariable Long id) {
        User admin = getAuthenticatedUser();
        if (admin == null) return ResponseEntity.status(401).body("Not authenticated");
        if (admin.getRole() != Role.ADMIN) return ResponseEntity.status(403).body("Admin access required");

        List<AttendanceEdit> edits = editRepository.findByAttendanceIdOrderByEditedAtDesc(id);
        return ResponseEntity.ok(edits);
    }

    private User getAuthenticatedUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepo.findByEmail(auth.getName()).orElse(null);
    }
}