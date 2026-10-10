package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "attendance_rules")
public class AttendanceRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course; // null = global default

    @Column(name = "window_minutes", nullable = false)
    private int windowMinutes; // join within X min of start = eligible for Present

    @Column(name = "min_duration_minutes")
    private Integer minDurationMinutes; // nullable

    @Column(name = "late_threshold_minutes")
    private Integer lateThresholdMinutes; // nullable

    public AttendanceRule() {}

    public AttendanceRule(int windowMinutes) {
        this.windowMinutes = windowMinutes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public int getWindowMinutes() { return windowMinutes; }
    public void setWindowMinutes(int windowMinutes) { this.windowMinutes = windowMinutes; }

    public Integer getMinDurationMinutes() { return minDurationMinutes; }
    public void setMinDurationMinutes(Integer minDurationMinutes) { this.minDurationMinutes = minDurationMinutes; }

    public Integer getLateThresholdMinutes() { return lateThresholdMinutes; }
    public void setLateThresholdMinutes(Integer lateThresholdMinutes) { this.lateThresholdMinutes = lateThresholdMinutes; }
}