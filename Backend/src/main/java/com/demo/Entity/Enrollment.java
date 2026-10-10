package com.example.demo.entity;

import com.example.demo.enums.EnrollmentMethod;
import com.example.demo.enums.EnrollmentStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "enrollments")
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "enrolled_at", nullable = false, updatable = false)
    private LocalDateTime enrolledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentMethod method;

    public Enrollment() {
        this.enrolledAt = LocalDateTime.now();
        this.status = EnrollmentStatus.ACTIVE;
    }

    public Enrollment(User student, Course course, EnrollmentMethod method) {
        this.student = student;
        this.course = course;
        this.method = method;
        this.enrolledAt = LocalDateTime.now();
        this.status = EnrollmentStatus.ACTIVE;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(LocalDateTime enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public EnrollmentMethod getMethod() {
        return method;
    }

    public void setMethod(EnrollmentMethod method) {
        this.method = method;
    }
}