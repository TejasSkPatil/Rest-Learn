package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "progress_items")
public class ProgressItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(name = "item_type", nullable = false)
    private String itemType; // VIDEO, RESOURCE, ASSIGNMENT

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    public ProgressItem() {}

    public ProgressItem(User student, String itemType, Long itemId) {
        this.student = student;
        this.itemType = itemType;
        this.itemId = itemId;
        this.completedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}