package com.example.demo.controller;

import com.example.demo.entity.Notification;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepo;
import com.example.demo.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepo userRepo;

    public NotificationController(NotificationService notificationService, UserRepo userRepo) {
        this.notificationService = notificationService;
        this.userRepo = userRepo;
    }

    @GetMapping("/api/notifications/mine")
    public ResponseEntity<List<Notification>> getMyNotifications() {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(notificationService.getMyNotifications(user.getId()));
    }

    @PatchMapping("/api/notifications/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    private User getAuthenticatedUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepo.findByEmail(auth.getName()).orElse(null);
    }
}