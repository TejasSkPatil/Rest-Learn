package com.example.demo.controller;

import com.example.demo.entity.Announcement;
import com.example.demo.entity.Course;
import com.example.demo.entity.Enrollment;
import com.example.demo.entity.User;
import com.example.demo.enums.EnrollmentStatus;
import com.example.demo.enums.Role;
import com.example.demo.repository.AnnouncementRepository;
import com.example.demo.repository.CourseRepo;
import com.example.demo.repository.EnrollmentRepository;
import com.example.demo.repository.UserRepo;
import com.example.demo.service.CourseAccessService;
import com.example.demo.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AnnouncementController {

    private final AnnouncementRepository announcementRepository;
    private final EnrollmentRepository EnrollmentRepository;
    private final CourseRepo courseRepo;
    private final CourseAccessService courseAccessService;
    private final NotificationService notificationService;
    private final UserRepo userRepo;

    public AnnouncementController(AnnouncementRepository announcementRepository, EnrollmentRepository EnrollmentRepository,
                                  CourseRepo courseRepo, CourseAccessService courseAccessService,
                                  NotificationService notificationService, UserRepo userRepo) {
        this.announcementRepository = announcementRepository;
        this.EnrollmentRepository = EnrollmentRepository;
        this.courseRepo = courseRepo;
        this.courseAccessService = courseAccessService;
        this.notificationService = notificationService;
        this.userRepo = userRepo;
    }

    // Teacher: Create announcement (notifies all enrolled students)
    @PostMapping("/api/teacher/courses/{courseId}/announcements")
    public ResponseEntity<?> createAnnouncement(@PathVariable Long courseId, @RequestBody Announcement announcement) {
        User teacher = getAuthenticatedUser();
        if (teacher == null) return ResponseEntity.status(401).body("Not authenticated");

        if (!courseAccessService.isTeacherOwner(courseId, teacher.getId())) {
            return ResponseEntity.status(403).body("Access denied");
        }

        Course course = courseRepo.findById(courseId).orElseThrow();
        announcement.setCourse(course);
        announcement.setTeacher(teacher);
        Announcement saved = announcementRepository.save(announcement);

        // Notify all enrolled students
        List<Enrollment> enrollments = EnrollmentRepository.findByStudentIdAndStatus(courseId, EnrollmentStatus.ACTIVE);
        for (Enrollment e : enrollments) {
            notificationService.create(e.getStudent().getId(), "ANNOUNCEMENT",
                    "New announcement in " + course.getName() + ": " + announcement.getTitle());
        }

        return ResponseEntity.ok(saved);
    }

    // Teacher: List announcements for a course
    @GetMapping("/api/teacher/courses/{courseId}/announcements")
    public ResponseEntity<?> listTeacherAnnouncements(@PathVariable Long courseId) {
        User teacher = getAuthenticatedUser();
        if (teacher == null) return ResponseEntity.status(401).body("Not authenticated");

        if (!courseAccessService.isTeacherOwner(courseId, teacher.getId())) {
            return ResponseEntity.status(403).body("Access denied");
        }

        return ResponseEntity.ok(announcementRepository.findByCourseIdOrderByCreatedAtDesc(courseId));
    }

    // Teacher: Delete announcement
    @DeleteMapping("/api/teacher/courses/{courseId}/announcements/{id}")
    public ResponseEntity<?> deleteAnnouncement(@PathVariable Long courseId, @PathVariable Long id) {
        User teacher = getAuthenticatedUser();
        if (teacher == null) return ResponseEntity.status(401).body("Not authenticated");

        if (!courseAccessService.isTeacherOwner(courseId, teacher.getId())) {
            return ResponseEntity.status(403).body("Access denied");
        }

        announcementRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // Student/Admin: Get announcements for a course
    @GetMapping("/api/courses/{courseId}/announcements")
    public ResponseEntity<?> getCourseAnnouncements(@PathVariable Long courseId) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Not authenticated");

        boolean isOwner = courseAccessService.isTeacherOwner(courseId, user.getId());
        boolean isEnrolled = courseAccessService.isEnrolledStudent(courseId, user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isOwner && !isEnrolled && !isAdmin) {
            return ResponseEntity.status(403).body("Access denied");
        }

        return ResponseEntity.ok(announcementRepository.findByCourseIdOrderByCreatedAtDesc(courseId));
    }

    private User getAuthenticatedUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepo.findByEmail(auth.getName()).orElse(null);
    }
}