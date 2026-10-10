package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.enums.Role;
import com.example.demo.repository.UserRepo;
import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> listAllUsers();

    List<User> listUsersByRole(Role role);

    Optional<User> getUserById(Long id);

    User updateUserStatus(Long id, boolean active);

    User resetUserPassword(Long id, String newPassword);

    void deleteUser(Long id);
}