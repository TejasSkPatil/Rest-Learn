package com.example.demo.repository;

import com.example.demo.entity.AttendanceRule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AttendanceRuleRepository extends JpaRepository<AttendanceRule, Long> {

    Optional<AttendanceRule> findByCourseId(Long courseId);

    Optional<AttendanceRule> findByCourseIsNull();
}