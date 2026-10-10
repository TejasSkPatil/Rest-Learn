package com.example.demo.service;

import com.example.demo.entity.Module;
import com.example.demo.entity.Resource;
import com.example.demo.enums.ResourceType;
import com.example.demo.enums.VideoStatus;
import com.example.demo.repository.ModuleRepository;
import com.example.demo.repository.ResourceRepository;
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
public class ResourceServiceImpl implements ResourceService {

    @Autowired
    private ResourceRepository ResourceRepository;

    @Autowired
    private ModuleRepository ModuleRepository;

    @Value("${app.upload.resources:uploads/resources}")
    private String uploadDir;

    @Value("${app.upload.max-file-size:20971520}")
    private long maxFileSize;

    @Override
    public Resource uploadResource(Long moduleId, String title, MultipartFile file, Long teacherId) {
        Module module = ModuleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        if (!module.getCourse().getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("Access denied: you do not own this course");
        }

        // Validate file size (20MB max)
        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("File size exceeds maximum allowed (20MB)");
        }

        // Validate file type
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("Invalid file");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        ResourceType fileType;
        switch (extension) {
            case "pdf":
                fileType = ResourceType.PDF;
                break;
            case "ppt":
            case "pptx":
                fileType = ResourceType.PPT;
                break;
            case "doc":
            case "docx":
                fileType = ResourceType.DOC;
                break;
            default:
                throw new RuntimeException("Invalid file type. Allowed: PDF, PPT, PPTX, DOC, DOCX");
        }

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String uniqueFilename = UUID.randomUUID().toString() + "." + extension;
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath);

            String storageRef = "uploads/resources/" + uniqueFilename;
            Resource resource = new Resource(module, title, fileType, storageRef);
            return ResourceRepository.save(resource);

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload resource: " + e.getMessage());
        }
    }

    @Override
    public void deleteResource(Long resourceId, Long teacherId) {
        Resource resource = ResourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        if (!resource.getModule().getCourse().getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("Access denied: you do not own this course");
        }

        try {
            Path filePath = Paths.get(resource.getStorageRef());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Failed to delete resource file: " + e.getMessage());
        }

        ResourceRepository.delete(resource);
    }

    @Override
    public List<Resource> listResourcesByModule(Long moduleId) {
        return ResourceRepository.findByModuleId(moduleId);
    }

    @Override
    public List<Resource> listPublishedResourcesByModule(Long moduleId) {
        return ResourceRepository.findByModuleIdAndStatus(moduleId, VideoStatus.PUBLISHED);
    }

    @Override
    public Resource getResourceById(Long resourceId) {
        return ResourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));
    }

    @Override
    public String getResourceStoragePath(Long resourceId) {
        Resource resource = getResourceById(resourceId);
        return resource.getStorageRef();
    }
}