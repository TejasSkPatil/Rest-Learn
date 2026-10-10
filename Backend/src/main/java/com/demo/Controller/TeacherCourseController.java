package com.example.demo.controller;

import com.example.demo.entity.Course;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepo;
import com.example.demo.service.CourseService;
import com.example.demo.util.EnrollmentCodeGenerator;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/teacher/courses")
public class TeacherCourseController {

    private final CourseService courseService;
    private final EnrollmentCodeGenerator codeGenerator;
    private final UserRepo userRepo;

    public TeacherCourseController(CourseService courseService, EnrollmentCodeGenerator codeGenerator, UserRepo userRepo) {
        this.courseService = courseService;
        this.codeGenerator = codeGenerator;
        this.userRepo = userRepo;
    }

    @PostMapping("/create")
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        User authenticatedUser = getAuthenticatedUser();
        Course courseWithCode = new Course();
        courseWithCode.setName(course.getName());
        courseWithCode.setDepartment(course.getDepartment());
        courseWithCode.setSemester(course.getSemester());
        courseWithCode.setDescription(course.getDescription());
        courseWithCode = courseService.createCourse(courseWithCode, authenticatedUser);
        // Generate and set enrollment code
        String generatedCode = codeGenerator.generateCode();
        courseWithCode.setEnrollmentCode(generatedCode);
        courseWithCode.setCodeActive(true);
        return ResponseEntity.ok(courseService.updateCourse(courseWithCode));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<Course>> listMyCourses() {
        User authenticatedUser = getAuthenticatedUser();
        List<Course> courses = courseService.listCoursesByTeacher(authenticatedUser);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        User authenticatedUser = getAuthenticatedUser();
        Optional<Course> optionalCourse = courseService.getCourseById(id);

        if (optionalCourse.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Course course = optionalCourse.get();

        // Ownership check: teacher can only view their own courses
        if (!course.getTeacher().getId().equals(authenticatedUser.getId())) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(course);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @RequestBody Course courseDetails) {
        User authenticatedUser = getAuthenticatedUser();
        Optional<Course> optionalCourse = courseService.getCourseById(id);

        if (optionalCourse.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Course course = optionalCourse.get();

        // Ownership check: teacher can only update their own courses
        if (!course.getTeacher().getId().equals(authenticatedUser.getId())) {
            return ResponseEntity.status(403).build();
        }

        course.setName(courseDetails.getName());
        course.setDepartment(courseDetails.getDepartment());
        course.setSemester(courseDetails.getSemester());
        course.setDescription(courseDetails.getDescription());

        return ResponseEntity.ok(courseService.updateCourse(course));
    }

    @PostMapping("/{id}/regenerate-code")
    public ResponseEntity<Course> regenerateCode(@PathVariable Long id) {
        User authenticatedUser = getAuthenticatedUser();
        Optional<Course> optionalCourse = courseService.getCourseById(id);

        if (optionalCourse.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Course course = optionalCourse.get();

        // Ownership check: teacher can only regenerate code for their own course
        if (!course.getTeacher().getId().equals(authenticatedUser.getId())) {
            return ResponseEntity.status(403).build();
        }

        // Generate new unique code
        String newCode = codeGenerator.generateCode();
        course.setEnrollmentCode(newCode);
        course.setCodeActive(true);

        return ResponseEntity.ok(courseService.updateCourse(course));
    }

    @PatchMapping("/{id}/code-status")
    public ResponseEntity<Course> setCodeStatus(@PathVariable Long id, @RequestParam boolean active) {
        User authenticatedUser = getAuthenticatedUser();
        Optional<Course> optionalCourse = courseService.getCourseById(id);

        if (optionalCourse.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Course course = optionalCourse.get();

        // Ownership check: teacher can only modify code status for their own course
        if (!course.getTeacher().getId().equals(authenticatedUser.getId())) {
            return ResponseEntity.status(403).build();
        }

        course.setCodeActive(active);

        return ResponseEntity.ok(courseService.updateCourse(course));
    }

    private User getAuthenticatedUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        String email = auth.getName();
        return userRepo.findByEmail(email).orElse(null);
    }
}