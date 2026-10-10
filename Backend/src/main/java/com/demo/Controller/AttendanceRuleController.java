package com.example.demo.controller;

import com.example.demo.entity.AttendanceRule;
import com.example.demo.entity.User;
import com.example.demo.enums.Role;
import com.example.demo.repository.AttendanceRuleRepository;
import com.example.demo.repository.UserRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AttendanceRuleController {

    private final AttendanceRuleRepository ruleRepository;
    private final UserRepo userRepo;

    public AttendanceRuleController(AttendanceRuleRepository ruleRepository, UserRepo userRepo) {
        this.ruleRepository = ruleRepository;
        this.userRepo = userRepo;
    }

    // Get global default rule
    @GetMapping("/api/admin/attendance-rules/global")
    public ResponseEntity<?> getGlobalRule() {
        AttendanceRule rule = ruleRepository.findByCourseIsNull()
                .orElse(new AttendanceRule(15)); // default 15 min window
        return ResponseEntity.ok(rule);
    }

    // Update global default rule
    @PutMapping("/api/admin/attendance-rules/global")
    public ResponseEntity<?> updateGlobalRule(@RequestBody AttendanceRule rule) {
        User admin = getAuthenticatedUser();
        if (admin == null) return ResponseEntity.status(401).body("Not authenticated");
        if (admin.getRole() != Role.ADMIN) return ResponseEntity.status(403).body("Admin access required");

        AttendanceRule existing = ruleRepository.findByCourseIsNull()
                .orElse(new AttendanceRule(15));
        existing.setWindowMinutes(rule.getWindowMinutes());
        existing.setMinDurationMinutes(rule.getMinDurationMinutes());
        existing.setLateThresholdMinutes(rule.getLateThresholdMinutes());
        return ResponseEntity.ok(ruleRepository.save(existing));
    }

    // Get rule for a specific course
    @GetMapping("/api/admin/attendance-rules/course/{courseId}")
    public ResponseEntity<?> getCourseRule(@PathVariable Long courseId) {
        AttendanceRule rule = ruleRepository.findByCourseId(courseId)
                .orElse(ruleRepository.findByCourseIsNull().orElse(new AttendanceRule(15)));
        return ResponseEntity.ok(rule);
    }

    // Update rule for a specific course
    @PutMapping("/api/admin/attendance-rules/course/{courseId}")
    public ResponseEntity<?> updateCourseRule(@PathVariable Long courseId, @RequestBody AttendanceRule ruleDetails) {
        User admin = getAuthenticatedUser();
        if (admin == null) return ResponseEntity.status(401).body("Not authenticated");
        if (admin.getRole() != Role.ADMIN) return ResponseEntity.status(403).body("Admin access required");

        AttendanceRule rule = ruleRepository.findByCourseId(courseId)
                .orElse(new AttendanceRule(15));
        rule.setCourse(new com.example.demo.entity.Course());
        rule.getCourse().setId(courseId);
        rule.setWindowMinutes(ruleDetails.getWindowMinutes());
        rule.setMinDurationMinutes(ruleDetails.getMinDurationMinutes());
        rule.setLateThresholdMinutes(ruleDetails.getLateThresholdMinutes());
        return ResponseEntity.ok(ruleRepository.save(rule));
    }

    private User getAuthenticatedUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepo.findByEmail(auth.getName()).orElse(null);
    }
}