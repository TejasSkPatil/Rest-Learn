package com.example.demo.entity;

import com.example.demo.enums.ResourceType;
import com.example.demo.enums.VideoStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "resources")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    private ResourceType fileType;

    @Column(name = "storage_ref", nullable = false)
    private String storageRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VideoStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Resource() {
        this.createdAt = LocalDateTime.now();
        this.status = VideoStatus.DRAFT;
    }

    public Resource(Module module, String title, ResourceType fileType, String storageRef) {
        this.module = module;
        this.title = title;
        this.fileType = fileType;
        this.storageRef = storageRef;
        this.status = VideoStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Module getModule() { return module; }
    public void setModule(Module module) { this.module = module; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public ResourceType getFileType() { return fileType; }
    public void setFileType(ResourceType fileType) { this.fileType = fileType; }

    public String getStorageRef() { return storageRef; }
    public void setStorageRef(String storageRef) { this.storageRef = storageRef; }

    public VideoStatus getStatus() { return status; }
    public void setStatus(VideoStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}