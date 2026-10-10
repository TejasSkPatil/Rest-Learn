package com.example.demo.entity;

import com.example.demo.enums.AttendanceStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "live_class_id", nullable = false)
    private LiveClass liveClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    @Column(name = "join_time")
    private LocalDateTime joinTime;

    @Column(name = "leave_time")
    private LocalDateTime leaveTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marked_by")
    private User markedBy;

    @Column(name = "marked_at")
    private LocalDateTime markedAt;

    @Column(name = "manually_set", nullable = false, columnDefinition = "boolean default false")
    private boolean manuallySet;

    public Attendance() {}

    public Attendance(LiveClass liveClass, User student) {
        this.liveClass = liveClass;
        this.student = student;
        this.status = AttendanceStatus.ABSENT;
        this.manuallySet = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LiveClass getLiveClass() { return liveClass; }
    public void setLiveClass(LiveClass liveClass) { this.liveClass = liveClass; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }

    public LocalDateTime getJoinTime() { return joinTime; }
    public void setJoinTime(LocalDateTime joinTime) { this.joinTime = joinTime; }

    public LocalDateTime getLeaveTime() { return leaveTime; }
    public void setLeaveTime(LocalDateTime leaveTime) { this.leaveTime = leaveTime; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public User getMarkedBy() { return markedBy; }
    public void setMarkedBy(User markedBy) { this.markedBy = markedBy; }

    public LocalDateTime getMarkedAt() { return markedAt; }
    public void setMarkedAt(LocalDateTime markedAt) { this.markedAt = markedAt; }

    public boolean isManuallySet() { return manuallySet; }
    public void setManuallySet(boolean manuallySet) { this.manuallySet = manuallySet; }
}