package com.example.demo.service;

import com.example.demo.entity.Video;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface VideoService {

    Video uploadVideo(Long moduleId, String title, MultipartFile file, Long teacherId);

    void deleteVideo(Long videoId, Long teacherId);

    List<Video> listVideosByModule(Long moduleId);

    List<Video> listPublishedVideosByModule(Long moduleId);

    Video getVideoById(Long videoId);

    String getVideoStoragePath(Long videoId);
}