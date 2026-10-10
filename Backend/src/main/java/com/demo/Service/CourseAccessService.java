package com.example.demo.service;

import com.example.demo.entity.Course;
import com.example.demo.entity.Enrollment;
import com.example.demo.enums.EnrollmentStatus;
import com.example.demo.enums.Role;
import com.example.demo.repository.CourseRepo;
import com.example.demo.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CourseAccessService {

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private EnrollmentRepository EnrollmentRepository;

    /**
     * Returns true if the given user is the teacher who owns the course.
     */
    public boolean isTeacherOwner(Long courseId, Long userId) {
        Optional<Course> courseOpt = courseRepo.findById(courseId);
        return courseOpt.isPresent() && courseOpt.get().getTeacher().getId().equals(userId);
    }

    /**
     * Returns true if the given user has an ACTIVE enrollment in the course.
     */
    public boolean isEnrolledStudent(Long courseId, Long userId) {
        return EnrollmentRepository.findByStudentIdAndCourseId(userId, courseId)
                .map(enrollment -> enrollment.getStatus() == EnrollmentStatus.ACTIVE)
                .orElse(false);
    }

    /**
     * Returns true if the user has any admin role.
     */
    public boolean isAdmin(Long userId) {
        // Admin check is handled via SecurityConfig role-based access
        // This method can be extended if needed
        return false;
    }

    /**
     * Checks if a user can access a course (teacher owner, enrolled student, or admin).
     */
    public boolean canAccessCourse(Long courseId, Long userId, Role role) {
        if (role == Role.ADMIN) {
            return true;
        }
        if (isTeacherOwner(courseId, userId)) {
            return true;
        }
        if (isEnrolledStudent(courseId, userId)) {
            return true;
        }
        return false;
    }

    /**
     * Fetch a course by ID.
     */
    public Optional<Course> getCourseById(Long courseId) {
        return courseRepo.findById(courseId);
    }
}