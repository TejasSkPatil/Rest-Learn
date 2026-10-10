package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepo;
import com.example.demo.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepo userRepo;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepo userRepo, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        User savedUser = userRepo.save(user);
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User loginUser) {

        User user = userRepo.findByEmail(loginUser.getEmail()).orElse(null);

        if (user == null) {
            return ResponseEntity.status(401).body("User not found");
        }

        boolean passwordMatches = false;
        String rawPassword = loginUser.getPassword();
        String storedPassword = user.getPassword();

        // 1. Check BCrypt hash match
        if (storedPassword != null && storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            passwordMatches = passwordEncoder.matches(rawPassword, storedPassword);
        } else {
            // 2. Fallback for legacy plain-text password & upgrade to BCrypt
            if (storedPassword != null && storedPassword.equals(rawPassword)) {
                passwordMatches = true;
                user.setPassword(passwordEncoder.encode(rawPassword));
                userRepo.save(user);
            }
        }

        if (!passwordMatches) {
            return ResponseEntity.status(401).body("Invalid password");
        }

        if (!user.isActive()) {
            return ResponseEntity.status(401).body("Account is inactive");
        }

        String token = jwtUtil.generateToken(user);
        return ResponseEntity.ok(token);
    }
}
