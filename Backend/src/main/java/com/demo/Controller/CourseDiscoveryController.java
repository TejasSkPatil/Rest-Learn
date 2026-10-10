package com.example.demo.controller;

import com.example.demo.dto.PageResponse;
import com.example.demo.entity.Category;
import com.example.demo.entity.Course;
import com.example.demo.entity.CourseReview;
import com.example.demo.entity.User;
import com.example.demo.repository.*;
import com.example.demo.service.CourseAccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class CourseDiscoveryController {

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private CategoryRepo categoryRepo;

    @Autowired
    private CourseReviewRepo reviewRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CourseAccessService courseAccessService;

    // Open Course Search (Paginated)
    @GetMapping("/courses/search")
    public ResponseEntity<PageResponse<Map<String, Object>>> searchCourses(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String semester,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<Course> allCourses = courseRepo.findAll();
        User currentUser = getAuthenticatedUser();

        // Apply filters
        List<Course> filtered = allCourses.stream().filter(c -> {
            boolean matchQ = q == null || q.isBlank() ||
                    (c.getName() != null && c.getName().toLowerCase().contains(q.toLowerCase())) ||
                    (c.getCourseCode() != null && c.getCourseCode().toLowerCase().contains(q.toLowerCase()));
            boolean matchDept = department == null || department.isBlank() ||
                    (c.getDepartment() != null && c.getDepartment().equalsIgnoreCase(department));
            boolean matchSem = semester == null || semester.isBlank() ||
                    (c.getSemester() != null && c.getSemester().equalsIgnoreCase(semester));
            return matchQ && matchDept && matchSem;
        }).collect(Collectors.toList());

        // Transform to response objects with review statistics and enrollment status
        List<Map<String, Object>> mapped = filtered.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("name", c.getName());
            map.put("courseCode", c.getCourseCode());
            map.put("department", c.getDepartment());
            map.put("semester", c.getSemester());
            map.put("description", c.getDescription());
            map.put("enrollmentCode", c.getEnrollmentCode());
            map.put("teacherName", c.getTeacher() != null ? c.getTeacher().getName() : "Instructor");

            Double avgRating = reviewRepo.getAverageRating(c.getId());
            Long reviewCount = reviewRepo.countReviews(c.getId());
            map.put("avgRating", avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : null);
            map.put("reviewCount", reviewCount != null ? reviewCount : 0);

            boolean isEnrolled = false;
            if (currentUser != null) {
                isEnrolled = courseAccessService.isEnrolledStudent(c.getId(), currentUser.getId());
            }
            map.put("isEnrolled", isEnrolled);
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(PageResponse.of(mapped, page, size));
    }

    // Category Endpoints
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> listCategories() {
        return ResponseEntity.ok(categoryRepo.findAll());
    }

    @PostMapping("/admin/categories")
    public ResponseEntity<Category> createCategory(@RequestBody Category cat) {
        return ResponseEntity.ok(categoryRepo.save(cat));
    }

    // Course Review Endpoints
    @GetMapping("/courses/{courseId}/reviews")
    public ResponseEntity<?> getCourseReviews(@PathVariable Long courseId) {
        List<CourseReview> reviews = reviewRepo.findByCourseId(courseId);
        Double avg = reviewRepo.getAverageRating(courseId);
        Map<String, Object> res = new HashMap<>();
        res.put("reviews", reviews);
        res.put("avgRating", avg != null ? Math.round(avg * 10.0) / 10.0 : null);
        res.put("totalReviews", reviews.size());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/student/courses/{courseId}/reviews")
    public ResponseEntity<?> addOrUpdateReview(
            @PathVariable Long courseId,
            @RequestBody CourseReview reviewReq) {

        User student = getAuthenticatedUser();
        if (student == null) return ResponseEntity.status(401).body("Not authenticated");

        if (!courseAccessService.isEnrolledStudent(courseId, student.getId())) {
            return ResponseEntity.status(403).body("Only enrolled students can review this course.");
        }

        Optional<Course> courseOpt = courseRepo.findById(courseId);
        if (courseOpt.isEmpty()) return ResponseEntity.notFound().build();

        CourseReview review = reviewRepo.findByCourseIdAndStudentId(courseId, student.getId())
                .orElse(new CourseReview());

        review.setCourse(courseOpt.get());
        review.setStudent(student);
        review.setRating(Math.max(1, Math.min(5, reviewReq.getRating())));
        review.setComment(reviewReq.getComment());
        review.setCreatedAt(LocalDateTime.now());

        return ResponseEntity.ok(reviewRepo.save(review));
    }

    private User getAuthenticatedUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepo.findByEmail(auth.getName()).orElse(null);
    }
}
