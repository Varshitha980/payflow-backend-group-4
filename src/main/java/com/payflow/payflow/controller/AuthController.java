package com.payflow.payflow.Controller;

import com.payflow.payflow.model.User;
import com.payflow.payflow.model.Employee;
import com.payflow.payflow.repository.UserRepository;
import com.payflow.payflow.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST Controller for all authentication-related endpoints, including
 * login and password reset for both administrative users and regular employees.
 * This controller demonstrates a unified approach to authentication across different
 * user types within the PayFlow system.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * Unified Login Endpoint for both administrative Users and regular Employees.
     * Checks the User table first (for roles like ADMIN, HR, MANAGER), then the Employee table.
     * NOTE: For a production application, passwords should be securely hashed (e.g., using BCrypt)
     * and not stored or compared in plain text. This implementation is for demonstration purposes.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password are required"));
        }

        System.out.println("Login attempt - Username: " + username + ", Password: " + password);

        // First, try to find in User table (for ADMIN, HR, MANAGER roles)
        // Only check User table if the input doesn't look like an email
        if (!username.contains("@")) {
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent() && user.get().getPassword().equals(password)) {
                User foundUser = user.get();
                Map<String, Object> response = new HashMap<>();
                response.put("id", foundUser.getId());
                response.put("username", foundUser.getUsername());
                response.put("name", foundUser.getUsername());
                // A placeholder email is used here. Consider adding an email field to the User table for consistency.
                response.put("email", foundUser.getUsername() + "@company.com");
                response.put("role", foundUser.getRole());
                response.put("firstLogin", foundUser.isFirstLogin());
                response.put("token", "user-token-" + foundUser.getId()); // Simple token for demo

                return ResponseEntity.ok(response);
            }
        }

        // Now try Employee table
        System.out.println("Trying Employee table with input: " + username);
        Optional<Employee> employee = employeeRepository.findByEmailAndPassword(username, password);
        if (!employee.isPresent()) {
            // If email not found, try username and password
            System.out.println("Email not found, trying username: " + username);
            employee = employeeRepository.findByUsernameAndPassword(username, password);
        }
        if (employee.isPresent()) {
            Employee foundEmployee = employee.get();
            System.out.println("Employee found: " + foundEmployee.getName() + " with role: " + foundEmployee.getRole());
            return ResponseEntity.ok(Map.of(
                    "id", foundEmployee.getId(),
                    "username", foundEmployee.getUsername(),
                    "name", foundEmployee.getName(),
                    "email", foundEmployee.getEmail(),
                    "role", foundEmployee.getRole(),
                    "firstLogin", foundEmployee.getFirstLogin(),
                    "token", "employee-token-" + foundEmployee.getId()
            ));
        }

        // If not found in either table, return unauthorized
        System.out.println("No user or employee found with credentials: " + username);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid username or password"));
    }

    /**
     * Reset Password for a User (ADMIN, HR, MANAGER).
     */
    @PostMapping("/reset-password/user")
    public ResponseEntity<?> resetUserPassword(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String newPassword = body.get("newPassword");

        if (username == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and newPassword are required"));
        }

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

    /**
     * Reset Password for an Employee.
     */
    @PostMapping("/reset-password/employee")
    public ResponseEntity<?> resetEmployeePassword(@RequestBody Map<String, String> body) {
        String idStr = body.get("id");
        String newPassword = body.get("newPassword");
        String pfNumber = body.get("pfNumber");

        if (idStr == null || newPassword == null || pfNumber == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing id, password, or PF Number"));
        }

        try {
            Long id = Long.parseLong(idStr);
            Optional<Employee> empOpt = employeeRepository.findById(id);

            if (empOpt.isPresent()) {
                Employee emp = empOpt.get();
                
                // Verify PF Number
                if (emp.getPfNumber() == null || !emp.getPfNumber().equals(pfNumber)) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", "Invalid PF Number. Please check your PF Number and try again."));
                }
                
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

    /**
     * Alternative endpoint for employee password reset that matches frontend calls.
     */
    @PostMapping("/employees/reset-password")
    public ResponseEntity<?> resetEmployeePasswordAlternative(@RequestBody Map<String, String> body) {
        return resetEmployeePassword(body);
    }

    /**
     * Alternative endpoint for user password reset that matches frontend calls.
     */
    @PostMapping("/users/reset-password")
    public ResponseEntity<?> resetUserPasswordAlternative(@RequestBody Map<String, String> body) {
        return resetUserPassword(body);
    }

    /**
     * Health Check Endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "PayFlow Authentication Service is running");
        response.put("timestamp", new Date());
        return ResponseEntity.ok(response);
    }
}
