package com.example.demo.controller;

import com.example.demo.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class PingController {

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/api/admin/ping")
    public ResponseEntity<Map<String, String>> adminPing(Authentication authentication) {
        String email = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        Map<String, String> response = new HashMap<>();
        response.put("email", email);
        response.put("role", role);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/teacher/ping")
    public ResponseEntity<Map<String, String>> teacherPing(Authentication authentication) {
        String email = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        Map<String, String> response = new HashMap<>();
        response.put("email", email);
        response.put("role", role);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/student/ping")
    public ResponseEntity<Map<String, String>> studentPing(Authentication authentication) {
        String email = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        Map<String, String> response = new HashMap<>();
        response.put("email", email);
        response.put("role", role);
        return ResponseEntity.ok(response);
    }
}