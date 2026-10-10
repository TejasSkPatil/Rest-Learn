package com.example.demo.service;

import com.example.demo.entity.Resource;
import com.example.demo.enums.ResourceType;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ResourceService {

    Resource uploadResource(Long moduleId, String title, MultipartFile file, Long teacherId);

    void deleteResource(Long resourceId, Long teacherId);

    List<Resource> listResourcesByModule(Long moduleId);

    List<Resource> listPublishedResourcesByModule(Long moduleId);

    Resource getResourceById(Long resourceId);

    String getResourceStoragePath(Long resourceId);
}