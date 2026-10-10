package com.example.demo.service;

import com.example.demo.entity.Course;
import com.example.demo.entity.Enrollment;
import com.example.demo.entity.User;
import com.example.demo.enums.EnrollmentMethod;
import com.example.demo.enums.EnrollmentStatus;
import java.util.List;

public interface EnrollmentService {

    Enrollment enrollByCode(User student, String code);

    List<Course> getStudentActiveCourses(User student);
}