package com.example.demo.controller;

import com.example.demo.entity.Assignment;
import com.example.demo.entity.User;
import com.example.demo.enums.AssignmentStatus;
import com.example.demo.enums.Role;
import com.example.demo.repository.UserRepo;
import com.example.demo.service.AssignmentService;
import com.example.demo.service.CourseAccessService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final CourseAccessService courseAccessService;
    private final UserRepo userRepo;

    public AssignmentController(AssignmentService assignmentService, CourseAccessService courseAccessService, UserRepo userRepo) {
        this.assignmentService = assignmentService;
        this.courseAccessService = courseAccessService;
        this.userRepo = userRepo;
    }

    // Teacher-only: Create assignment
    @PostMapping("/api/teacher/modules/{moduleId}/assignments")
    public ResponseEntity<?> createAssignment(@PathVariable Long moduleId, @RequestBody Assignment assignment) {
        User teacher = getAuthenticatedUser();
        if (teacher == null) return ResponseEntity.status(401).body("Not authenticated");

        try {
            Assignment created = assignmentService.createAssignment(moduleId, assignment, teacher.getId());
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Teacher-only: List all assignments (including DRAFT)
    @GetMapping("/api/teacher/modules/{moduleId}/assignments")
    public ResponseEntity<?> listTeacherAssignments(@PathVariable Long moduleId) {
        User teacher = getAuthenticatedUser();
        if (teacher == null) return ResponseEntity.status(401).body("Not authenticated");

        List<Assignment> assignments = assignmentService.listAssignmentsByModule(moduleId);
        return ResponseEntity.ok(assignments);
    }

    // Teacher-only: Update assignment
    @PutMapping("/api/teacher/modules/{moduleId}/assignments/{assignmentId}")
    public ResponseEntity<?> updateAssignment(@PathVariable Long moduleId, @PathVariable Long assignmentId, @RequestBody Assignment assignmentDetails) {
        User teacher = getAuthenticatedUser();
        if (teacher == null) return ResponseEntity.status(401).body("Not authenticated");

        try {
            Assignment updated = assignmentService.updateAssignment(assignmentId, assignmentDetails, teacher.getId());
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Teacher-only: Delete assignment
    @DeleteMapping("/api/teacher/modules/{moduleId}/assignments/{assignmentId}")
    public ResponseEntity<?> deleteAssignment(@PathVariable Long moduleId, @PathVariable Long assignmentId) {
        User teacher = getAuthenticatedUser();
        if (teacher == null) return ResponseEntity.status(401).body("Not authenticated");

        try {
            assignmentService.deleteAssignment(assignmentId, teacher.getId());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Enrolled student/teacher/admin: Get assignments for a module
    @GetMapping("/api/modules/{moduleId}/assignments")
    public ResponseEntity<?> getModuleAssignments(@PathVariable Long moduleId) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Not authenticated");

        List<Assignment> allAssignments = assignmentService.listAssignmentsByModule(moduleId);
        if (allAssignments.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        Long courseId = allAssignments.get(0).getModule().getCourse().getId();

        boolean isOwner = courseAccessService.isTeacherOwner(courseId, user.getId());
        boolean isEnrolled = courseAccessService.isEnrolledStudent(courseId, user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isOwner && !isEnrolled && !isAdmin) {
            return ResponseEntity.status(403).body("Access denied");
        }

        if (!isOwner && !isAdmin) {
            List<Assignment> publishedAssignments = assignmentService.listPublishedAssignmentsByModule(moduleId);
            return ResponseEntity.ok(publishedAssignments);
        }

        return ResponseEntity.ok(allAssignments);
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