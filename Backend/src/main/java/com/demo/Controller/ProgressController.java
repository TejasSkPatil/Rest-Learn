package com.example.demo.controller;

import com.example.demo.entity.*;
import com.example.demo.enums.EnrollmentStatus;
import com.example.demo.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ProgressController {

    private final ProgressItemRepository progressRepo;
    private final EnrollmentRepository EnrollmentRepository;
    private final ModuleRepository ModuleRepository;
    private final VideoRepository VideoRepository;
    private final ResourceRepository ResourceRepository;
    private final AssignmentRepository AssignmentRepository;
    private final UserRepo userRepo;

    public ProgressController(ProgressItemRepository progressRepo, EnrollmentRepository EnrollmentRepository,
                              ModuleRepository ModuleRepository, VideoRepository VideoRepository, ResourceRepository ResourceRepository,
                              AssignmentRepository AssignmentRepository, UserRepo userRepo) {
        this.progressRepo = progressRepo;
        this.EnrollmentRepository = EnrollmentRepository;
        this.ModuleRepository = ModuleRepository;
        this.VideoRepository = VideoRepository;
        this.ResourceRepository = ResourceRepository;
        this.AssignmentRepository = AssignmentRepository;
        this.userRepo = userRepo;
    }

    // Student: Mark an item as complete
    @PostMapping("/api/student/progress")
    public ResponseEntity<?> markComplete(@RequestBody Map<String, String> request) {
        User student = getAuthenticatedUser();
        if (student == null) return ResponseEntity.status(401).body("Not authenticated");

        String itemType = request.get("itemType");
        Long itemId = Long.parseLong(request.get("itemId"));

        if (!progressRepo.existsByStudentIdAndItemTypeAndItemId(student.getId(), itemType, itemId)) {
            ProgressItem item = new ProgressItem(student, itemType, itemId);
            progressRepo.save(item);
        }

        return ResponseEntity.ok().build();
    }

    // Student: Get progress for a course
    @GetMapping("/api/student/courses/{courseId}/progress")
    public ResponseEntity<?> getCourseProgress(@PathVariable Long courseId) {
        User student = getAuthenticatedUser();
        if (student == null) return ResponseEntity.status(401).body("Not authenticated");

        // Count total items in published modules
        List<com.example.demo.entity.Module> publishedModules = ModuleRepository.findByCourseIdAndStatusOrderByPosition(courseId, com.example.demo.enums.ModuleStatus.PUBLISHED);
        int totalItems = 0;
        for (com.example.demo.entity.Module m : publishedModules) {
            totalItems += VideoRepository.findByModuleIdAndStatus(m.getId(), com.example.demo.enums.VideoStatus.PUBLISHED).size();
            totalItems += ResourceRepository.findByModuleIdAndStatus(m.getId(), com.example.demo.enums.VideoStatus.PUBLISHED).size();
            totalItems += AssignmentRepository.findByModuleIdAndStatus(m.getId(), com.example.demo.enums.AssignmentStatus.PUBLISHED).size();
        }

        // Count completed items
        List<ProgressItem> completed = progressRepo.findByStudentIdAndItemType(student.getId(), "VIDEO");
        int completedCount = completed.size();

        int percentage = totalItems == 0 ? 0 : (completedCount * 100) / totalItems;

        Map<String, Object> result = new HashMap<>();
        result.put("totalItems", totalItems);
        result.put("completedItems", completedCount);
        result.put("percentage", percentage);

        return ResponseEntity.ok(result);
    }

    private User getAuthenticatedUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepo.findByEmail(auth.getName()).orElse(null);
    }
}