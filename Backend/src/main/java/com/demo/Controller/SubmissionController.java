package com.example.demo.controller;

import com.example.demo.entity.Submission;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepo;
import com.example.demo.service.SubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class SubmissionController {

    private final SubmissionService submissionService;
    private final UserRepo userRepo;

    public SubmissionController(SubmissionService submissionService, UserRepo userRepo) {
        this.submissionService = submissionService;
        this.userRepo = userRepo;
    }

    // Student submits an assignment (file and/or text)
    @PostMapping("/api/student/assignments/{assignmentId}/submissions")
    public ResponseEntity<?> submitAssignment(
            @PathVariable Long assignmentId,
            @RequestBody Map<String, String> request) {

        User student = getAuthenticatedUser();
        if (student == null) return ResponseEntity.status(401).body("Not authenticated");

        String textAnswer = request.get("textAnswer");
        String fileRef = request.get("fileRef");

        if ((textAnswer == null || textAnswer.trim().isEmpty()) && (fileRef == null || fileRef.trim().isEmpty())) {
            return ResponseEntity.badRequest().body("Please provide a text answer and/or file");
        }

        try {
            Submission submission = submissionService.submitAssignment(assignmentId, student.getId(), textAnswer, fileRef);
            return ResponseEntity.ok(submission);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Student views their own submission for an assignment
    @GetMapping("/api/student/assignments/{assignmentId}/submissions/mine")
    public ResponseEntity<?> getMySubmission(@PathVariable Long assignmentId) {
        User student = getAuthenticatedUser();
        if (student == null) return ResponseEntity.status(401).body("Not authenticated");

        Optional<Submission> submission = submissionService.getMySubmission(assignmentId, student.getId());
        return ResponseEntity.ok(submission.orElse(null));
    }

    // Teacher views all submissions for an assignment
    @GetMapping("/api/teacher/assignments/{assignmentId}/submissions")
    public ResponseEntity<?> getSubmissionsForAssignment(@PathVariable Long assignmentId) {
        User teacher = getAuthenticatedUser();
        if (teacher == null) return ResponseEntity.status(401).body("Not authenticated");

        try {
            List<Submission> submissions = submissionService.getSubmissionsForAssignment(assignmentId, teacher.getId());
            return ResponseEntity.ok(submissions);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Teacher grades a submission
    @PutMapping("/api/teacher/submissions/{submissionId}/grade")
    public ResponseEntity<?> gradeSubmission(
            @PathVariable Long submissionId,
            @RequestBody Map<String, Object> request) {

        User teacher = getAuthenticatedUser();
        if (teacher == null) return ResponseEntity.status(401).body("Not authenticated");

        Integer marks = request.get("marks") != null ? ((Number) request.get("marks")).intValue() : null;
        String feedback = (String) request.get("feedback");

        if (marks == null) {
            return ResponseEntity.badRequest().body("Marks are required");
        }

        try {
            Submission graded = submissionService.gradeSubmission(submissionId, marks, feedback, teacher.getId());
            return ResponseEntity.ok(graded);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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