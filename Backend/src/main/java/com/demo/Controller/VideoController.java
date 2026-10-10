package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.entity.Video;
import com.example.demo.enums.Role;
import com.example.demo.repository.UserRepo;
import com.example.demo.service.CourseAccessService;
import com.example.demo.service.VideoService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
public class VideoController {

    private final VideoService videoService;
    private final CourseAccessService courseAccessService;
    private final UserRepo userRepo;

    public VideoController(VideoService videoService, CourseAccessService courseAccessService, UserRepo userRepo) {
        this.videoService = videoService;
        this.courseAccessService = courseAccessService;
        this.userRepo = userRepo;
    }

    @PostMapping("/api/teacher/modules/{moduleId}/videos")
    public ResponseEntity<?> uploadVideo(
            @PathVariable Long moduleId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title) {

        User teacher = getAuthenticatedUser();
        if (teacher == null) return ResponseEntity.status(401).body("Not authenticated");

        try {
            Video video = videoService.uploadVideo(moduleId, title, file, teacher.getId());
            return ResponseEntity.ok(video);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/api/teacher/modules/{moduleId}/videos")
    public ResponseEntity<?> listTeacherVideos(@PathVariable Long moduleId) {
        User teacher = getAuthenticatedUser();
        if (teacher == null) return ResponseEntity.status(401).body("Not authenticated");

        List<Video> videos = videoService.listVideosByModule(moduleId);
        return ResponseEntity.ok(videos);
    }

    @DeleteMapping("/api/teacher/modules/{moduleId}/videos/{videoId}")
    public ResponseEntity<?> deleteVideo(@PathVariable Long moduleId, @PathVariable Long videoId) {
        User teacher = getAuthenticatedUser();
        if (teacher == null) return ResponseEntity.status(401).body("Not authenticated");

        try {
            videoService.deleteVideo(videoId, teacher.getId());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/api/modules/{moduleId}/videos")
    public ResponseEntity<?> listPublishedVideos(@PathVariable Long moduleId) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Not authenticated");

        List<Video> allVideos = videoService.listVideosByModule(moduleId);
        if (allVideos.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        Long courseId = allVideos.get(0).getModule().getCourse().getId();

        boolean isOwner = courseAccessService.isTeacherOwner(courseId, user.getId());
        boolean isEnrolled = courseAccessService.isEnrolledStudent(courseId, user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isOwner && !isEnrolled && !isAdmin) {
            return ResponseEntity.status(403).body("Access denied");
        }

        if (!isOwner && !isAdmin) {
            List<Video> publishedVideos = videoService.listPublishedVideosByModule(moduleId);
            return ResponseEntity.ok(publishedVideos);
        }

        return ResponseEntity.ok(allVideos);
    }

    // HTTP 206 Partial Content Range-based Video Streaming
    @GetMapping("/api/videos/{id}/stream")
    public ResponseEntity<ResourceRegion> streamVideo(
            @PathVariable Long id,
            @RequestHeader(value = "Range", required = false) String rangeHeader) {

        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).build();

        try {
            Video video = videoService.getVideoById(id);
            Long courseId = video.getModule().getCourse().getId();

            boolean isOwner = courseAccessService.isTeacherOwner(courseId, user.getId());
            boolean isEnrolled = courseAccessService.isEnrolledStudent(courseId, user.getId());
            boolean isAdmin = user.getRole() == Role.ADMIN;

            if (!isOwner && !isEnrolled && !isAdmin) {
                return ResponseEntity.status(403).build();
            }

            if (!isOwner && !isAdmin && video.getStatus() != com.example.demo.enums.VideoStatus.PUBLISHED) {
                return ResponseEntity.status(403).build();
            }

            Path filePath = Paths.get(video.getStorageRef());
            Resource resource = new FileSystemResource(filePath);

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            long contentLength = resource.contentLength();
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) contentType = "video/mp4";

            long chunkSize = 1024 * 1024; // 1 MB chunk
            ResourceRegion region;

            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String[] ranges = rangeHeader.substring(6).split("-");
                long start = Long.parseLong(ranges[0]);
                long end = ranges.length > 1 && !ranges[1].isEmpty() ? Long.parseLong(ranges[1]) : contentLength - 1;
                long rangeLength = Math.min(chunkSize, end - start + 1);
                region = new ResourceRegion(resource, start, rangeLength);

                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .contentType(MediaType.parseMediaType(contentType))
                        .header("Accept-Ranges", "bytes")
                        .body(region);
            } else {
                long rangeLength = Math.min(chunkSize, contentLength);
                region = new ResourceRegion(resource, 0, rangeLength);
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .contentType(MediaType.parseMediaType(contentType))
                        .header("Accept-Ranges", "bytes")
                        .body(region);
            }

        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    private User getAuthenticatedUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepo.findByEmail(auth.getName()).orElse(null);
    }
}
