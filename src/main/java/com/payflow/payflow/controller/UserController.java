package com.payflow.payflow.controller;

import com.payflow.payflow.model.User;
import com.payflow.payflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000") // allow frontend access
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // =============================
    // 🔐 1. Login Endpoint
    // =============================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        Optional<User> user = userRepository.findByUsername(username);

        if (user.isPresent() && user.get().getPassword().equals(password)) {
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.get().getId());
            response.put("username", user.get().getUsername());
            response.put("role", user.get().getRole());
            response.put("firstLogin", user.get().isFirstLogin());
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }

    // =============================
    // 👤 2. Admin Creates HR/Manager
    // =============================
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        // Prevent duplicate usernames
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }

        user.setFirstLogin(true); // new user must reset password
        return ResponseEntity.ok(userRepository.save(user));
    }

    // =============================
    // 🔑 3. Reset Password (First Login)
    // =============================
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String newPassword = body.get("newPassword");

        Optional<User> user = userRepository.findByUsername(username);

        if (user.isPresent()) {
            User u = user.get();
            u.setPassword(newPassword);
            u.setFirstLogin(false);
            return ResponseEntity.ok(userRepository.save(u));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }
    @PutMapping("/{id}/status")
    public User updateUserStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(request.getStatus());
        return userRepository.save(user);
    }

    // DTO for status update
    public static class StatusUpdateRequest {
        private String status;
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    // =============================
    // 📋 4. Get All Users (Admin view)
    // =============================
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
}
