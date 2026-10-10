package com.example.demo.entity;

import com.example.demo.enums.LiveClassStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "live_classes")
public class LiveClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timetable_id", nullable = false)
    private Timetable timetable;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LiveClassStatus status;

    @Column(name = "meeting_ref")
    private String meetingRef;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    public LiveClass() {}

    public LiveClass(Timetable timetable, LocalDate date) {
        this.timetable = timetable;
        this.date = date;
        this.status = LiveClassStatus.SCHEDULED;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Timetable getTimetable() { return timetable; }
    public void setTimetable(Timetable timetable) { this.timetable = timetable; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LiveClassStatus getStatus() { return status; }
    public void setStatus(LiveClassStatus status) { this.status = status; }

    public String getMeetingRef() { return meetingRef; }
    public void setMeetingRef(String meetingRef) { this.meetingRef = meetingRef; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }
}