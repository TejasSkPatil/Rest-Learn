package com.example.demo.repository;

import com.example.demo.entity.Video;
import com.example.demo.enums.VideoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {

    List<Video> findByModuleId(Long moduleId);

    List<Video> findByModuleIdAndStatus(Long moduleId, VideoStatus status);
}