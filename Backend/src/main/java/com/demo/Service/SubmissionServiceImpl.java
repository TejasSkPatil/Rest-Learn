package com.example.demo.service;

import com.example.demo.entity.Assignment;
import com.example.demo.entity.Submission;
import com.example.demo.entity.User;
import com.example.demo.enums.SubmissionStatus;
import com.example.demo.repository.AssignmentRepository;
import com.example.demo.repository.SubmissionRepository;
import com.example.demo.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    @Autowired
    private SubmissionRepository SubmissionRepository;

    @Autowired
    private AssignmentRepository AssignmentRepository;

    @Autowired
    private UserRepo userRepo;

    @Override
    public Submission submitAssignment(Long assignmentId, Long studentId, String textAnswer, String fileRef) {
        Assignment assignment = AssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        User student = userRepo.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Check for existing submission — allow resubmission by overwriting
        Optional<Submission> existingOpt = SubmissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId);

        Submission submission;
        if (existingOpt.isPresent()) {
            // Overwrite existing submission
            submission = existingOpt.get();
            submission.setTextAnswer(textAnswer);
            submission.setFileRef(fileRef);
            submission.setSubmittedAt(LocalDateTime.now());
            submission.setStatus(determineSubmissionStatus(assignment));
            submission.setMarks(null);
            submission.setFeedback(null);
        } else {
            // Create new submission
            submission = new Submission(assignment, student);
            submission.setTextAnswer(textAnswer);
            submission.setFileRef(fileRef);
            submission.setStatus(determineSubmissionStatus(assignment));
        }

        return SubmissionRepository.save(submission);
    }

    private SubmissionStatus determineSubmissionStatus(Assignment assignment) {
        if (assignment.getDueDate() != null && LocalDateTime.now().isAfter(assignment.getDueDate())) {
            return SubmissionStatus.LATE;
        }
        return SubmissionStatus.SUBMITTED;
    }

    @Override
    public Optional<Submission> getMySubmission(Long assignmentId, Long studentId) {
        return SubmissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId);
    }

    @Override
    public List<Submission> getSubmissionsForAssignment(Long assignmentId, Long teacherId) {
        Assignment assignment = AssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        // Verify teacher owns the course
        if (!assignment.getModule().getCourse().getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("Access denied: you do not own this course");
        }

        return SubmissionRepository.findByAssignmentId(assignmentId);
    }

    @Override
    public Submission gradeSubmission(Long submissionId, Integer marks, String feedback, Long teacherId) {
        Submission submission = SubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        // Verify teacher owns the course
        if (!submission.getAssignment().getModule().getCourse().getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("Access denied: you do not own this course");
        }

        submission.setMarks(marks);
        submission.setFeedback(feedback);
        submission.setStatus(SubmissionStatus.GRADED);

        return SubmissionRepository.save(submission);
    }
}