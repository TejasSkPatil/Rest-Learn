package com.example.demo.entity;

import com.example.demo.enums.Role;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String courseCode;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String semester;

    @Column(columnDefinition = "text")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(nullable = false, unique = true)
    private String enrollmentCode;

    @Column(name = "code_active", nullable = false, columnDefinition = "boolean default true")
    private boolean codeActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    private Set<Enrollment> enrollments = new HashSet<>();

    // Default constructor
    public Course() {
        this.codeActive = true;
        this.createdAt = LocalDateTime.now();
    }

    // Full constructor
    public Course(String name, String courseCode, String department, String semester,
                  String description, User teacher, String enrollmentCode, boolean codeActive) {
        this.name = name;
        this.courseCode = courseCode;
        this.department = department;
        this.semester = semester;
        this.description = description;
        this.teacher = teacher;
        this.enrollmentCode = enrollmentCode;
        this.codeActive = codeActive;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getTeacher() {
        return teacher;
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }

    public String getEnrollmentCode() {
        return enrollmentCode;
    }

    public void setEnrollmentCode(String enrollmentCode) {
        this.enrollmentCode = enrollmentCode;
    }

    public boolean isCodeActive() {
        return codeActive;
    }

    public void setCodeActive(boolean codeActive) {
        this.codeActive = codeActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<Enrollment> getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(Set<Enrollment> enrollments) {
        this.enrollments = enrollments;
    }
}