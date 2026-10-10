package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_edits")
public class AttendanceEdit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id", nullable = false)
    private Attendance attendance;

    @Column(name = "old_status", nullable = false)
    private String oldStatus;

    @Column(name = "new_status", nullable = false)
    private String newStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "edited_by")
    private User editedBy;

    @Column(name = "edited_at", nullable = false)
    private LocalDateTime editedAt;

    public AttendanceEdit() {}

    public AttendanceEdit(Attendance attendance, String oldStatus, String newStatus, User editedBy) {
        this.attendance = attendance;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.editedBy = editedBy;
        this.editedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Attendance getAttendance() { return attendance; }
    public void setAttendance(Attendance attendance) { this.attendance = attendance; }

    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public User getEditedBy() { return editedBy; }
    public void setEditedBy(User editedBy) { this.editedBy = editedBy; }

    public LocalDateTime getEditedAt() { return editedAt; }
    public void setEditedAt(LocalDateTime editedAt) { this.editedAt = editedAt; }
}