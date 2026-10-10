package com.example.demo.service;

import com.example.demo.entity.Course;
import com.example.demo.entity.User;
import java.util.List;
import java.util.Optional;

public interface CourseService {

    Course createCourse(Course course, User teacher);

    Course updateCourse(Course course);

    List<Course> listCoursesByTeacher(User teacher);

    Optional<Course> getCourseById(Long id);
}