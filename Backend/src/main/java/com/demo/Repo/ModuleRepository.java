package com.example.demo.repository;

import com.example.demo.entity.Module;
import com.example.demo.enums.ModuleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ModuleRepository extends JpaRepository<Module, Long> {

    List<Module> findByCourseIdOrderByPosition(Long courseId);

    List<Module> findByCourseIdAndStatusOrderByPosition(Long courseId, ModuleStatus status);
}