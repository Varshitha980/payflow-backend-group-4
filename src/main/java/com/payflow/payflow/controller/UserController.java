package com.payflow.payflow.Controller;

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

    /**
     * Endpoint for user login.
     * @param body A Map containing the "username" and "password".
     * @return A ResponseEntity with user details on success or an unauthorized message on failure.
     */
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

    /**
     * Endpoint for an admin to create new users (HR or Manager).
     * @param user The User object to be created.
     * @return A ResponseEntity with the created user object or a conflict message if the username already exists.
     */
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        // Prevent duplicate usernames
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }

        user.setFirstLogin(true); // new user must reset password
        return ResponseEntity.ok(userRepository.save(user));
    }

    /**
     * Endpoint to reset a user's password, typically on their first login.
     * @param body A Map containing the "username" and the "newPassword".
     * @return A ResponseEntity with the updated user object or a not found message if the user does not exist.
     */
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

    /**
     * Endpoint to update a user's status.
     * @param id The ID of the user to update.
     * @param request A DTO containing the new status.
     * @return The updated User object.
     * @throws RuntimeException if the user is not found.
     */
    @PutMapping("/{id}/status")
    public User updateUserStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(request.getStatus());
        return userRepository.save(user);
    }

    /**
     * DTO for status update requests.
     */
    public static class StatusUpdateRequest {
        private String status;
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    /**
     * Endpoint to retrieve a list of all users.
     * @return A ResponseEntity with a list of all User objects.
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    /**
     * Endpoint to retrieve a list of all users with the "MANAGER" role.
     * @return A ResponseEntity with a list of all manager User objects.
     */
    @GetMapping("/managers")
    public ResponseEntity<List<User>> getAllManagers() {
        List<User> managers = userRepository.findByRole("MANAGER");
        return ResponseEntity.ok(managers);
    }
}
