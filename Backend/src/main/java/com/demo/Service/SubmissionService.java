package com.example.demo.service;

import com.example.demo.entity.Submission;
import java.util.List;
import java.util.Optional;

public interface SubmissionService {

    Submission submitAssignment(Long assignmentId, Long studentId, String textAnswer, String fileRef);

    Optional<Submission> getMySubmission(Long assignmentId, Long studentId);

    List<Submission> getSubmissionsForAssignment(Long assignmentId, Long teacherId);

    Submission gradeSubmission(Long submissionId, Integer marks, String feedback, Long teacherId);
}