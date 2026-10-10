package com.example.demo.controller;

import com.example.demo.entity.Resource;
import com.example.demo.entity.User;
import com.example.demo.enums.Role;
import com.example.demo.enums.VideoStatus;
import com.example.demo.repository.UserRepo;
import com.example.demo.service.CourseAccessService;
import com.example.demo.service.ResourceService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
public class ResourceController {

    private final ResourceService resourceService;
    private final CourseAccessService courseAccessService;
    private final UserRepo userRepo;

    public ResourceController(ResourceService resourceService, CourseAccessService courseAccessService, UserRepo userRepo) {
        this.resourceService = resourceService;
        this.courseAccessService = courseAccessService;
        this.userRepo = userRepo;
    }

    // Teacher-only: Upload resource to a module
    @PostMapping("/api/teacher/modules/{moduleId}/resources")
    public ResponseEntity<?> uploadResource(
            @PathVariable Long moduleId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title) {

        User teacher = getAuthenticatedUser();
        if (teacher == null) return ResponseEntity.status(401).body("Not authenticated");

        try {
            Resource resource = resourceService.uploadResource(moduleId, title, file, teacher.getId());
            return ResponseEntity.ok(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Teacher-only: List all resources in a module
    @GetMapping("/api/teacher/modules/{moduleId}/resources")
    public ResponseEntity<?> listTeacherResources(@PathVariable Long moduleId) {
        User teacher = getAuthenticatedUser();
        if (teacher == null) return ResponseEntity.status(401).body("Not authenticated");

        List<Resource> resources = resourceService.listResourcesByModule(moduleId);
        return ResponseEntity.ok(resources);
    }

    // Teacher-only: Delete resource
    @DeleteMapping("/api/teacher/modules/{moduleId}/resources/{resourceId}")
    public ResponseEntity<?> deleteResource(@PathVariable Long moduleId, @PathVariable Long resourceId) {
        User teacher = getAuthenticatedUser();
        if (teacher == null) return ResponseEntity.status(401).body("Not authenticated");

        try {
            resourceService.deleteResource(resourceId, teacher.getId());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Enrolled student/teacher/admin: List published resources in a module
    @GetMapping("/api/modules/{moduleId}/resources")
    public ResponseEntity<?> listPublishedResources(@PathVariable Long moduleId) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Not authenticated");

        List<Resource> allResources = resourceService.listResourcesByModule(moduleId);
        if (allResources.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        Long courseId = allResources.get(0).getModule().getCourse().getId();

        boolean isOwner = courseAccessService.isTeacherOwner(courseId, user.getId());
        boolean isEnrolled = courseAccessService.isEnrolledStudent(courseId, user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isOwner && !isEnrolled && !isAdmin) {
            return ResponseEntity.status(403).body("Access denied");
        }

        if (!isOwner && !isAdmin) {
            List<Resource> publishedResources = resourceService.listPublishedResourcesByModule(moduleId);
            return ResponseEntity.ok(publishedResources);
        }

        return ResponseEntity.ok(allResources);
    }

    // Download resource file with access control
    @GetMapping("/api/resources/{id}/download")
    public ResponseEntity<FileSystemResource> downloadResource(@PathVariable Long id) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).build();

        try {
            com.example.demo.entity.Resource resource = resourceService.getResourceById(id);
            Long courseId = resource.getModule().getCourse().getId();

            boolean isOwner = courseAccessService.isTeacherOwner(courseId, user.getId());
            boolean isEnrolled = courseAccessService.isEnrolledStudent(courseId, user.getId());
            boolean isAdmin = user.getRole() == Role.ADMIN;

            if (!isOwner && !isEnrolled && !isAdmin) {
                return ResponseEntity.status(403).build();
            }

            if (!isOwner && !isAdmin && resource.getStatus() != VideoStatus.PUBLISHED) {
                return ResponseEntity.status(403).build();
            }

            Path filePath = Paths.get(resource.getStorageRef());
            FileSystemResource fileResource = new FileSystemResource(filePath);

            if (!fileResource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getTitle() + "\"")
                    .body(fileResource);

        } catch (IOException e) {
            return ResponseEntity.status(500).build();
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