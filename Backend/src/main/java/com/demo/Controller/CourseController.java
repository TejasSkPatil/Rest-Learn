package com.example.demo.controller;

import com.example.demo.entity.Course;
import com.example.demo.entity.User;
import com.example.demo.enums.Role;
import com.example.demo.repository.UserRepo;
import com.example.demo.service.CourseAccessService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseAccessService courseAccessService;
    private final UserRepo userRepo;

    public CourseController(CourseAccessService courseAccessService, UserRepo userRepo) {
        this.courseAccessService = courseAccessService;
        this.userRepo = userRepo;
    }

    /**
     * GET /api/courses/{id}
     * Returns course details only if the caller is:
     * - the owning teacher
     * - an enrolled student (ACTIVE enrollment)
     * - an admin
     * Otherwise returns 403.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCourse(@PathVariable Long id) {
        User authenticatedUser = getAuthenticatedUser();

        if (authenticatedUser == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }

        // Manual access check using CourseAccessService
        boolean hasAccess = courseAccessService.canAccessCourse(
                id,
                authenticatedUser.getId(),
                authenticatedUser.getRole()
        );

        if (!hasAccess) {
            return ResponseEntity.status(403).body("Access denied: you do not have permission to view this course");
        }

        // Fetch and return course details
        Optional<Course> course = courseAccessService.getCourseById(id);
        if (course.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(course.get());
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