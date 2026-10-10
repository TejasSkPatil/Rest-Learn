package com.example.demo.controller;

import com.example.demo.entity.LiveClass;
import com.example.demo.entity.LiveClassJoinEvent;
import com.example.demo.entity.User;
import com.example.demo.enums.LiveClassStatus;
import com.example.demo.enums.Role;
import com.example.demo.repository.LiveClassRepository;
import com.example.demo.repository.LiveClassJoinEventRepo;
import com.example.demo.repository.UserRepo;
import com.example.demo.service.CourseAccessService;
import com.example.demo.service.LiveClassSchedulerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
public class LiveClassController {

    private final LiveClassRepository LiveClassRepository;
    private final LiveClassJoinEventRepo joinEventRepo;
    private final LiveClassSchedulerService schedulerService;
    private final CourseAccessService courseAccessService;
    private final UserRepo userRepo;

    public LiveClassController(LiveClassRepository LiveClassRepository, LiveClassJoinEventRepo joinEventRepo,
                               LiveClassSchedulerService schedulerService, CourseAccessService courseAccessService,
                               UserRepo userRepo) {
        this.LiveClassRepository = LiveClassRepository;
        this.joinEventRepo = joinEventRepo;
        this.schedulerService = schedulerService;
        this.courseAccessService = courseAccessService;
        this.userRepo = userRepo;
    }

    // Admin-only: Generate upcoming live class sessions
    @PostMapping("/api/admin/live-classes/generate")
    public ResponseEntity<?> generateSessions() {
        User admin = getAuthenticatedUser();
        if (admin == null) return ResponseEntity.status(401).body("Not authenticated");
        if (admin.getRole() != Role.ADMIN) return ResponseEntity.status(403).body("Admin access required");

        int created = schedulerService.generateUpcomingSessions();
        return ResponseEntity.ok("Created " + created + " new live class sessions");
    }

    // Teacher: Start a live class
    @PostMapping("/api/teacher/live-classes/{id}/start")
    public ResponseEntity<?> startClass(@PathVariable Long id) {
        User teacher = getAuthenticatedUser();
        if (teacher == null) return ResponseEntity.status(401).body("Not authenticated");

        LiveClass liveClass = LiveClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Live class not found"));

        Long courseId = liveClass.getTimetable().getCourse().getId();
        if (!courseAccessService.isTeacherOwner(courseId, teacher.getId())) {
            return ResponseEntity.status(403).body("Access denied");
        }

        liveClass.setStatus(LiveClassStatus.LIVE);
        liveClass.setStartedAt(LocalDateTime.now());
        // Placeholder meetingRef — real provider integration point
        liveClass.setMeetingRef("meeting-" + UUID.randomUUID().toString().substring(0, 8));

        LiveClass updated = LiveClassRepository.save(liveClass);
        return ResponseEntity.ok(updated);
    }

    // Teacher: End a live class
    @PostMapping("/api/teacher/live-classes/{id}/end")
    public ResponseEntity<?> endClass(@PathVariable Long id) {
        User teacher = getAuthenticatedUser();
        if (teacher == null) return ResponseEntity.status(401).body("Not authenticated");

        LiveClass liveClass = LiveClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Live class not found"));

        Long courseId = liveClass.getTimetable().getCourse().getId();
        if (!courseAccessService.isTeacherOwner(courseId, teacher.getId())) {
            return ResponseEntity.status(403).body("Access denied");
        }

        liveClass.setStatus(LiveClassStatus.ENDED);
        liveClass.setEndedAt(LocalDateTime.now());

        LiveClass updated = LiveClassRepository.save(liveClass);
        return ResponseEntity.ok(updated);
    }

    // Student: Join a live class
    @PostMapping("/api/student/live-classes/{id}/join")
    public ResponseEntity<?> joinClass(@PathVariable Long id) {
        User student = getAuthenticatedUser();
        if (student == null) return ResponseEntity.status(401).body("Not authenticated");

        LiveClass liveClass = LiveClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Live class not found"));

        if (liveClass.getStatus() != LiveClassStatus.LIVE) {
            return ResponseEntity.badRequest().body("Class is not live yet");
        }

        Long courseId = liveClass.getTimetable().getCourse().getId();
        if (!courseAccessService.isEnrolledStudent(courseId, student.getId())) {
            return ResponseEntity.status(403).body("You are not enrolled in this course");
        }

        // Record join event
        LiveClassJoinEvent joinEvent = new LiveClassJoinEvent(liveClass, student);
        joinEventRepo.save(joinEvent);

        return ResponseEntity.ok(liveClass.getMeetingRef());
    }

    // Student: Leave a live class
    @PostMapping("/api/student/live-classes/{id}/leave")
    public ResponseEntity<?> leaveClass(@PathVariable Long id) {
        User student = getAuthenticatedUser();
        if (student == null) return ResponseEntity.status(401).body("Not authenticated");

        LiveClass liveClass = LiveClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Live class not found"));

        // Find the student's join event and set leaveTime
        List<LiveClassJoinEvent> events = joinEventRepo.findByLiveClassIdAndStudentId(id, student.getId());
        if (!events.isEmpty()) {
            LiveClassJoinEvent lastEvent = events.get(events.size() - 1);
            if (lastEvent.getLeaveTime() == null) {
                lastEvent.setLeaveTime(LocalDateTime.now());
                joinEventRepo.save(lastEvent);
            }
        }

        return ResponseEntity.ok().build();
    }

    // Teacher: Get today's classes
    @GetMapping("/api/teacher/live-classes/today")
    public ResponseEntity<?> getTodaysClassesForTeacher() {
        User teacher = getAuthenticatedUser();
        if (teacher == null) return ResponseEntity.status(401).body("Not authenticated");

        List<LiveClass> classes = LiveClassRepository.findTodaysSessionsForTeacher(teacher.getId(), LocalDate.now());
        return ResponseEntity.ok(classes);
    }

    // Student: Get today's classes
    @GetMapping("/api/student/live-classes/today")
    public ResponseEntity<?> getTodaysClassesForStudent() {
        User student = getAuthenticatedUser();
        if (student == null) return ResponseEntity.status(401).body("Not authenticated");

        List<LiveClass> classes = LiveClassRepository.findTodaysSessionsForStudent(student.getId(), LocalDate.now());
        return ResponseEntity.ok(classes);
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