package com.example.demo.repository;

import com.example.demo.entity.Resource;
import com.example.demo.enums.VideoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    List<Resource> findByModuleId(Long moduleId);

    List<Resource> findByModuleIdAndStatus(Long moduleId, VideoStatus status);
}