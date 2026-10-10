package com.example.demo.controller;

import com.example.demo.dto.PageResponse;
import com.example.demo.entity.User;
import com.example.demo.enums.Role;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<PageResponse<User>> listUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        List<User> users;
        if (role != null) {
            users = userService.listUsersByRole(role);
        } else {
            users = userService.listAllUsers();
        }

        PageResponse<User> paged = PageResponse.of(users, page, size);
        return ResponseEntity.ok(paged);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<User> updateUserStatus(@PathVariable Long id, @RequestParam boolean active) {
        User updated = userService.updateUserStatus(id, active);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<User> resetUserPassword(@PathVariable Long id, @RequestBody String newPassword) {
        User updated = userService.resetUserPassword(id, newPassword);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
