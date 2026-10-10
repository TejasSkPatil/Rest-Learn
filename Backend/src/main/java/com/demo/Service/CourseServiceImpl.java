package com.example.demo.service;

import com.example.demo.entity.Course;
import com.example.demo.entity.User;
import com.example.demo.repository.CourseRepo;
import com.example.demo.util.EnrollmentCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private EnrollmentCodeGenerator codeGenerator;

    @Override
    public Course createCourse(Course course, User teacher) {
        course.setTeacher(teacher);
        // Generate unique enrollment code
        String generatedCode = generateUniqueCode();
        course.setEnrollmentCode(generatedCode);
        course.setCodeActive(true);
        course.setCreatedAt(LocalDateTime.now());
        return courseRepo.save(course);
    }

    @Override
    public Course updateCourse(Course course) {
        return courseRepo.save(course);
    }

    @Override
    public List<Course> listCoursesByTeacher(User teacher) {
        return courseRepo.findByTeacherId(teacher.getId());
    }

    @Override
    public Optional<Course> getCourseById(Long id) {
        return courseRepo.findById(id);
    }

    private String generateUniqueCode() {
        String generatedCode;
        do {
            generatedCode = codeGenerator.generateCode();
            // Check if code already exists in database
        } while (courseRepo.findByEnrollmentCode(generatedCode).isPresent());
        return generatedCode;
    }
}