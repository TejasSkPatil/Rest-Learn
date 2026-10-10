package com.example.demo.controller;

import com.example.demo.entity.Course;
import com.example.demo.entity.Enrollment;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepo;
import com.example.demo.service.EnrollmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final UserRepo userRepo;

    public EnrollmentController(EnrollmentService enrollmentService, UserRepo userRepo) {
        this.enrollmentService = enrollmentService;
        this.userRepo = userRepo;
    }

    @PostMapping("/enroll")
    public ResponseEntity<?> enrollByCode(@RequestBody Map<String, String> request) {
        User student = getAuthenticatedUser();
        String code = request.get("code");

        if (code == null || code.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Enrollment code is required");
        }

        try {
            Enrollment enrollment = enrollmentService.enrollByCode(student, code.trim().toUpperCase());
            return ResponseEntity.ok(enrollment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/courses")
    public ResponseEntity<List<Course>> getMyCourses() {
        User student = getAuthenticatedUser();
        List<Course> courses = enrollmentService.getStudentActiveCourses(student);
        return ResponseEntity.ok(courses);
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