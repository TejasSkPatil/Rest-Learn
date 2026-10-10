package com.example.demo.repository;

import com.example.demo.entity.Assignment;
import com.example.demo.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByModuleId(Long moduleId);

    List<Assignment> findByModuleIdAndStatus(Long moduleId, AssignmentStatus status);
}