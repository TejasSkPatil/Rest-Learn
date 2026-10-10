package com.example.demo.controller;

import com.example.demo.entity.UserProfile;
import com.example.demo.entity.User;
import com.example.demo.repository.UserProfileRepo;
import com.example.demo.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private UserProfileRepo profileRepo;

    @Autowired
    private UserRepo userRepo;

    private static final String UPLOAD_DIR = "uploads/avatars/";

    @GetMapping("/mine")
    public ResponseEntity<?> getMyProfile() {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Not authenticated");

        UserProfile profile = profileRepo.findByUserId(user.getId()).orElseGet(() -> {
            UserProfile p = new UserProfile();
            p.setUser(user);
            p.setBio("");
            return profileRepo.save(p);
        });

        Map<String, Object> resp = new HashMap<>();
        resp.put("userId", user.getId());
        resp.put("name", user.getName());
        resp.put("email", user.getEmail());
        resp.put("role", user.getRole());
        resp.put("bio", profile.getBio());
        resp.put("avatarRef", profile.getAvatarRef());

        return ResponseEntity.ok(resp);
    }

    @PutMapping("/mine")
    public ResponseEntity<?> updateBio(@RequestBody Map<String, String> body) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Not authenticated");

        UserProfile profile = profileRepo.findByUserId(user.getId()).orElseGet(() -> {
            UserProfile p = new UserProfile();
            p.setUser(user);
            return p;
        });

        profile.setBio(body.getOrDefault("bio", ""));
        profile.setUpdatedAt(LocalDateTime.now());
        profileRepo.save(profile);

        return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
    }

    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        User user = getAuthenticatedUser();
        if (user == null) return ResponseEntity.status(401).body("Not authenticated");

        if (file.isEmpty()) return ResponseEntity.badRequest().body("File is empty");
        if (file.getSize() > 5 * 1024 * 1024) return ResponseEntity.badRequest().body("File exceeds 5MB limit");

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png") && !contentType.equals("image/webp"))) {
            return ResponseEntity.badRequest().body("Only JPG, PNG, and WEBP image files are allowed");
        }

        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) dir.mkdirs();

            String ext = contentType.contains("png") ? ".png" : contentType.contains("webp") ? ".webp" : ".jpg";
            String filename = UUID.randomUUID().toString() + ext;
            Path targetPath = Paths.get(UPLOAD_DIR + filename);
            Files.copy(file.getInputStream(), targetPath);

            UserProfile profile = profileRepo.findByUserId(user.getId()).orElseGet(() -> {
                UserProfile p = new UserProfile();
                p.setUser(user);
                return p;
            });

            profile.setAvatarRef("/" + UPLOAD_DIR + filename);
            profile.setUpdatedAt(LocalDateTime.now());
            profileRepo.save(profile);

            return ResponseEntity.ok(Map.of("avatarUrl", profile.getAvatarRef()));
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error saving avatar: " + e.getMessage());
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getPublicProfile(@PathVariable Long userId) {
        Optional<User> uOpt = userRepo.findById(userId);
        if (uOpt.isEmpty()) return ResponseEntity.notFound().build();
        User u = uOpt.get();

        UserProfile profile = profileRepo.findByUserId(u.getId()).orElse(null);

        Map<String, Object> pub = new HashMap<>();
        pub.put("userId", u.getId());
        pub.put("name", u.getName());
        pub.put("role", u.getRole());
        pub.put("bio", profile != null ? profile.getBio() : "");
        pub.put("avatarRef", profile != null ? profile.getAvatarRef() : null);

        return ResponseEntity.ok(pub);
    }

    private User getAuthenticatedUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepo.findByEmail(auth.getName()).orElse(null);
    }
}
