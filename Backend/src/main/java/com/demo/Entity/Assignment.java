package com.example.demo.entity;

import com.example.demo.enums.AssignmentStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "assignments")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "attachment_ref")
    private String attachmentRef;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "max_marks")
    private Integer maxMarks;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Assignment() {
        this.createdAt = LocalDateTime.now();
        this.status = AssignmentStatus.DRAFT;
    }

    public Assignment(Module module, String title, String description, LocalDateTime dueDate, Integer maxMarks) {
        this.module = module;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.maxMarks = maxMarks;
        this.status = AssignmentStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Module getModule() { return module; }
    public void setModule(Module module) { this.module = module; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAttachmentRef() { return attachmentRef; }
    public void setAttachmentRef(String attachmentRef) { this.attachmentRef = attachmentRef; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public Integer getMaxMarks() { return maxMarks; }
    public void setMaxMarks(Integer maxMarks) { this.maxMarks = maxMarks; }

    public AssignmentStatus getStatus() { return status; }
    public void setStatus(AssignmentStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}