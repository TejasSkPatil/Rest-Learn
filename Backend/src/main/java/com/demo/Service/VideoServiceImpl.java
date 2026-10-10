package com.example.demo.service;

import com.example.demo.entity.Module;
import com.example.demo.entity.Video;
import com.example.demo.enums.VideoStatus;
import com.example.demo.repository.ModuleRepository;
import com.example.demo.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class VideoServiceImpl implements VideoService {

    @Autowired
    private VideoRepository VideoRepository;

    @Autowired
    private ModuleRepository ModuleRepository;

    @Value("${app.upload.videos:uploads/videos}")
    private String uploadDir;

    @Override
    public Video uploadVideo(Long moduleId, String title, MultipartFile file, Long teacherId) {
        Module module = ModuleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        // Verify teacher owns the course
        if (!module.getCourse().getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("Access denied: you do not own this course");
        }

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            // Save file to disk
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath);

            // Create video record with relative path
            String storageRef = "uploads/videos/" + uniqueFilename;
            Video video = new Video(module, title, storageRef);
            return VideoRepository.save(video);

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload video: " + e.getMessage());
        }
    }

    @Override
    public void deleteVideo(Long videoId, Long teacherId) {
        Video video = VideoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        // Verify teacher owns the course
        if (!video.getModule().getCourse().getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("Access denied: you do not own this course");
        }

        // Delete file from disk
        try {
            Path filePath = Paths.get(video.getStorageRef());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log but don't fail if file deletion fails
            System.err.println("Failed to delete video file: " + e.getMessage());
        }

        VideoRepository.delete(video);
    }

    @Override
    public List<Video> listVideosByModule(Long moduleId) {
        return VideoRepository.findByModuleId(moduleId);
    }

    @Override
    public List<Video> listPublishedVideosByModule(Long moduleId) {
        return VideoRepository.findByModuleIdAndStatus(moduleId, VideoStatus.PUBLISHED);
    }

    @Override
    public Video getVideoById(Long videoId) {
        return VideoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));
    }

    @Override
    public String getVideoStoragePath(Long videoId) {
        Video video = getVideoById(videoId);
        return video.getStorageRef();
    }
}