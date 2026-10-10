package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.example.demo.enums.SubmissionStatus;

@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(name = "file_ref")
    private String fileRef;

    @Column(name = "text_answer", columnDefinition = "text")
    private String textAnswer;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column
    private Integer marks;

    @Column(columnDefinition = "text")
    private String feedback;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status;

    public Submission() {
        this.submittedAt = LocalDateTime.now();
        this.status = SubmissionStatus.SUBMITTED;
    }

    public Submission(Assignment assignment, User student) {
        this.assignment = assignment;
        this.student = student;
        this.submittedAt = LocalDateTime.now();
        this.status = SubmissionStatus.SUBMITTED;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Assignment getAssignment() { return assignment; }
    public void setAssignment(Assignment assignment) { this.assignment = assignment; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public String getFileRef() { return fileRef; }
    public void setFileRef(String fileRef) { this.fileRef = fileRef; }

    public String getTextAnswer() { return textAnswer; }
    public void setTextAnswer(String textAnswer) { this.textAnswer = textAnswer; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public Integer getMarks() { return marks; }
    public void setMarks(Integer marks) { this.marks = marks; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public SubmissionStatus getStatus() { return status; }
    public void setStatus(SubmissionStatus status) { this.status = status; }
}