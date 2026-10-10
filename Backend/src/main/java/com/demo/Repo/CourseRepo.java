package com.example.demo.repository;

import com.example.demo.entity.Course;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CourseRepo extends JpaRepository<Course, Long> {

    List<Course> findByTeacherId(Long teacherId);

    Optional<Course> findByEnrollmentCode(String enrollmentCode);
}