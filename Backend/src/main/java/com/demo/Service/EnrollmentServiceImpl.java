package com.example.demo.service;

import com.example.demo.entity.Course;
import com.example.demo.entity.Enrollment;
import com.example.demo.entity.User;
import com.example.demo.enums.EnrollmentMethod;
import com.example.demo.enums.EnrollmentStatus;
import com.example.demo.repository.CourseRepo;
import com.example.demo.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    @Autowired
    private EnrollmentRepository EnrollmentRepository;

    @Autowired
    private CourseRepo courseRepo;

    @Override
    public Enrollment enrollByCode(User student, String code) {
        // Look up course by enrollment code
        Course course = courseRepo.findByEnrollmentCode(code)
                .orElseThrow(() -> new RuntimeException("Invalid enrollment code: no course found with code " + code));

        // Check if code is active
        if (!course.isCodeActive()) {
            throw new RuntimeException("Enrollment code is no longer active for course: " + course.getName());
        }

        // Check if student is already enrolled
        if (EnrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            throw new RuntimeException("You are already enrolled in course: " + course.getName());
        }

        // Create enrollment record
        Enrollment enrollment = new Enrollment(student, course, EnrollmentMethod.CODE);
        return EnrollmentRepository.save(enrollment);
    }

    @Override
    public List<Course> getStudentActiveCourses(User student) {
        return EnrollmentRepository.findByStudentIdAndStatus(student.getId(), EnrollmentStatus.ACTIVE)
                .stream()
                .map(Enrollment::getCourse)
                .collect(Collectors.toList());
    }
}