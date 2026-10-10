package com.example.demo.controller;

import com.example.demo.entity.Timetable;
import com.example.demo.entity.User;
import com.example.demo.enums.Role;
import com.example.demo.repository.UserRepo;
import com.example.demo.service.TimetableService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TimetableController {

    private final TimetableService timetableService;
    private final UserRepo userRepo;

    public TimetableController(TimetableService timetableService, UserRepo userRepo) {
        this.timetableService = timetableService;
        this.userRepo = userRepo;
    }

    // Admin-only: Create timetable entry
    @PostMapping("/api/admin/timetable")
    public ResponseEntity<?> createEntry(@RequestBody Timetable timetable) {
        User admin = getAuthenticatedUser();
        if (admin == null) return ResponseEntity.status(401).body("Not authenticated");
        if (admin.getRole() != Role.ADMIN) return ResponseEntity.status(403).body("Admin access required");

        try {
            Timetable created = timetableService.createEntry(timetable);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    // Admin-only: List timetable entries (filterable by course/teacher/session)
    @GetMapping("/api/admin/timetable")
    public ResponseEntity<?> listTimetable(
            @RequestParam(required = false) String filterBy,
            @RequestParam(required = false) Long filterId) {
        User admin = getAuthenticatedUser();
        if (admin == null) return ResponseEntity.status(401).body("Not authenticated");
        if (admin.getRole() != Role.ADMIN) return ResponseEntity.status(403).body("Admin access required");

        List<Timetable> entries = timetableService.getTimetable(filterBy, filterId);
        return ResponseEntity.ok(entries);
    }

    // Admin-only: Update timetable entry
    @PutMapping("/api/admin/timetable/{id}")
    public ResponseEntity<?> updateEntry(@PathVariable Long id, @RequestBody Timetable timetableDetails) {
        User admin = getAuthenticatedUser();
        if (admin == null) return ResponseEntity.status(401).body("Not authenticated");
        if (admin.getRole() != Role.ADMIN) return ResponseEntity.status(403).body("Admin access required");

        try {
            Timetable updated = timetableService.updateEntry(id, timetableDetails);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    // Admin-only: Delete timetable entry
    @DeleteMapping("/api/admin/timetable/{id}")
    public ResponseEntity<?> deleteEntry(@PathVariable Long id) {
        User admin = getAuthenticatedUser();
        if (admin == null) return ResponseEntity.status(401).body("Not authenticated");
        if (admin.getRole() != Role.ADMIN) return ResponseEntity.status(403).body("Admin access required");

        try {
            timetableService.deleteEntry(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Teacher or Student: Get my timetable
    @GetMapping("/api/timetable/mine")
    public ResponseEntity<?> getMyTimetable() {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Not authenticated");

        List<Timetable> entries;
        if (user.getRole() == Role.TEACHER) {
            entries = timetableService.getMyTimetableAsTeacher(user.getId());
        } else if (user.getRole() == Role.STUDENT) {
            entries = timetableService.getMyTimetableAsStudent(user.getId());
        } else {
            return ResponseEntity.status(403).body("Only teachers and students can view timetables");
        }

        return ResponseEntity.ok(entries);
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