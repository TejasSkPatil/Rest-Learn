package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "live_class_join_events")
public class LiveClassJoinEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "live_class_id", nullable = false)
    private LiveClass liveClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(name = "join_time", nullable = false)
    private LocalDateTime joinTime;

    @Column(name = "leave_time")
    private LocalDateTime leaveTime;

    public LiveClassJoinEvent() {}

    public LiveClassJoinEvent(LiveClass liveClass, User student) {
        this.liveClass = liveClass;
        this.student = student;
        this.joinTime = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LiveClass getLiveClass() { return liveClass; }
    public void setLiveClass(LiveClass liveClass) { this.liveClass = liveClass; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public LocalDateTime getJoinTime() { return joinTime; }
    public void setJoinTime(LocalDateTime joinTime) { this.joinTime = joinTime; }

    public LocalDateTime getLeaveTime() { return leaveTime; }
    public void setLeaveTime(LocalDateTime leaveTime) { this.leaveTime = leaveTime; }
}