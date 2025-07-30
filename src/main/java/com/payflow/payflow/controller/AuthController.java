package com.payflow.payflow.controller;

import com.payflow.payflow.model.User;
import com.payflow.payflow.model.Employee;
import com.payflow.payflow.repository.UserRepository;
import com.payflow.payflow.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    // ✅ Unified Login Endpoint
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body("Username and password are required");
        }

        // First, try to find in User table (ADMIN, HR, MANAGER)
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent() && user.get().getPassword().equals(password)) {
            User foundUser = user.get();
            Map<String, Object> response = new HashMap<>();
            response.put("id", foundUser.getId());
            response.put("username", foundUser.getUsername());
            response.put("name", foundUser.getUsername()); // For compatibility
            response.put("email", foundUser.getUsername() + "@company.com"); // Default email
            response.put("role", foundUser.getRole());
            response.put("firstLogin", foundUser.isFirstLogin());
            response.put("token", "user-token-" + foundUser.getId()); // Simple token for demo
            
            return ResponseEntity.ok(response);
        }

        // If not found in User table, try Employee table
        Optional<Employee> employee = employeeRepository.findByNameAndPassword(username, password);
        if (employee.isPresent()) {
            Employee foundEmployee = employee.get();
            return ResponseEntity.ok(Map.of(
                    "id", foundEmployee.getId(),
                    "username", foundEmployee.getEmail(),
                    "name", foundEmployee.getName(),
                    "email", foundEmployee.getEmail(),
                    "role", foundEmployee.getRole(),
                    "firstLogin", foundEmployee.getFirstLogin(),
                    "token", "employee-token-" + foundEmployee.getId()
                ));
        }

        // If not found in either table, return unauthorized
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid username or password"));
    }

    // ✅ Reset Password for Users
    @PostMapping("/reset-password/user")
    public ResponseEntity<?> resetUserPassword(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String newPassword = body.get("newPassword");

        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            User u = user.get();
            u.setPassword(newPassword);
            u.setFirstLogin(false);
            userRepository.save(u);
            return ResponseEntity.ok(Map.of("message", "Password reset successful"));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
    }

    // ✅ Reset Password for Employees
    @PostMapping("/reset-password/employee")
    public ResponseEntity<?> resetEmployeePassword(@RequestBody Map<String, String> body) {
        String idStr = body.get("id");
        String newPassword = body.get("newPassword");

        if (idStr == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing id or password"));
        }

        try {
            Long id = Long.parseLong(idStr);
            Optional<Employee> empOpt = employeeRepository.findById(id);

            if (empOpt.isPresent()) {
                Employee emp = empOpt.get();
                emp.setPassword(newPassword);
                emp.setFirstLogin(false);
                employeeRepository.save(emp);
                return ResponseEntity.ok(Map.of("message", "Password reset successful"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Employee not found"));
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid ID format"));
        }
    }

    // ✅ Health Check
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "PayFlow Authentication Service is running");
        response.put("timestamp", new Date());
        return ResponseEntity.ok(response);
    }
}