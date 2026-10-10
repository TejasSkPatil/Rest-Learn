package com.example.demo.service;

import com.example.demo.entity.Notification;
import com.example.demo.entity.User;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepo userRepo;

    public Notification create(Long userId, String type, String message) {
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Notification notification = new Notification(user, type, message);
        return notificationRepository.save(notification);
    }

    public List<Notification> getMyNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }
}