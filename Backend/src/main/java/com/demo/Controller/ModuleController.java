package com.example.demo.controller;

import com.example.demo.entity.Module;
import com.example.demo.entity.User;
import com.example.demo.enums.Role;
import com.example.demo.repository.UserRepo;
import com.example.demo.service.CourseAccessService;
import com.example.demo.service.ModuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ModuleController {

    private final ModuleService moduleService;
    private final CourseAccessService courseAccessService;
    private final UserRepo userRepo;

    public ModuleController(ModuleService moduleService, CourseAccessService courseAccessService, UserRepo userRepo) {
        this.moduleService = moduleService;
        this.courseAccessService = courseAccessService;
        this.userRepo = userRepo;
    }

    // Teacher-only: Create module in a course
    @PostMapping("/api/teacher/courses/{courseId}/modules")
    public ResponseEntity<?> createModule(@PathVariable Long courseId, @RequestBody Module module) {
        User teacher = getAuthenticatedUser();
        if (teacher == null) return ResponseEntity.status(401).body("Not authenticated");

        if (!courseAccessService.isTeacherOwner(courseId, teacher.getId())) {
            return ResponseEntity.status(403).body("Access denied");
        }

        try {
            Module created = moduleService.createModule(courseId, module, teacher.getId());
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Teacher-only: List all modules (including DRAFT)
    @GetMapping("/api/teacher/courses/{courseId}/modules")
    public ResponseEntity<?> listTeacherModules(@PathVariable Long courseId) {
        User teacher = getAuthenticatedUser();
        if (teacher == null) return ResponseEntity.status(401).body("Not authenticated");

        if (!courseAccessService.isTeacherOwner(courseId, teacher.getId())) {
            return ResponseEntity.status(403).body("Access denied");
        }

        List<Module> modules = moduleService.listModulesByCourse(courseId);
        return ResponseEntity.ok(modules);
    }

    // Teacher-only: Update module
    @PutMapping("/api/teacher/courses/{courseId}/modules/{moduleId}")
    public ResponseEntity<?> updateModule(@PathVariable Long courseId, @PathVariable Long moduleId, @RequestBody Module moduleDetails) {
        User teacher = getAuthenticatedUser();
        if (teacher == null) return ResponseEntity.status(401).body("Not authenticated");

        if (!courseAccessService.isTeacherOwner(courseId, teacher.getId())) {
            return ResponseEntity.status(403).body("Access denied");
        }

        try {
            Module updated = moduleService.updateModule(moduleId, moduleDetails, teacher.getId());
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Teacher-only: Delete module
    @DeleteMapping("/api/teacher/courses/{courseId}/modules/{moduleId}")
    public ResponseEntity<?> deleteModule(@PathVariable Long courseId, @PathVariable Long moduleId) {
        User teacher = getAuthenticatedUser();
        if (teacher == null) return ResponseEntity.status(401).body("Not authenticated");

        if (!courseAccessService.isTeacherOwner(courseId, teacher.getId())) {
            return ResponseEntity.status(403).body("Access denied");
        }

        try {
            moduleService.deleteModule(moduleId, teacher.getId());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Any logged-in user: Get modules for a course
    // Students see only PUBLISHED; teacher/admin see all
    @GetMapping("/api/courses/{courseId}/modules")
    public ResponseEntity<?> getCourseModules(@PathVariable Long courseId) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Not authenticated");

        boolean isOwner = courseAccessService.isTeacherOwner(courseId, user.getId());
        boolean isEnrolled = courseAccessService.isEnrolledStudent(courseId, user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isOwner && !isEnrolled && !isAdmin) {
            return ResponseEntity.status(403).body("Access denied");
        }

        // Students only see PUBLISHED modules
        if (!isOwner && !isAdmin) {
            List<Module> publishedModules = moduleService.listPublishedModulesByCourse(courseId);
            return ResponseEntity.ok(publishedModules);
        }

        // Teacher and admin see all modules
        List<Module> allModules = moduleService.listModulesByCourse(courseId);
        return ResponseEntity.ok(allModules);
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