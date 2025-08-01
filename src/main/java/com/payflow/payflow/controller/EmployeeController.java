package com.payflow.payflow.controller;

import com.payflow.payflow.Service.EmployeeService;
import com.payflow.payflow.model.Employee;
import com.payflow.payflow.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    // ✅ 1. Get all employees
    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    // ✅ Get employees by manager ID
    @GetMapping("/manager/{managerId}")
    public List<Employee> getEmployeesByManager(@PathVariable Long managerId) {
        return employeeService.getEmployeesByManager(managerId);
    }

    // ✅ Get employees without manager (for HR assignment)
    @GetMapping("/unassigned")
    public List<Employee> getUnassignedEmployees() {
        return employeeService.getUnassignedEmployees();
    }

    // ✅ Assign employee to manager
    @PutMapping("/{employeeId}/assign-manager/{managerId}")
    public ResponseEntity<?> assignEmployeeToManager(@PathVariable Long employeeId, @PathVariable Long managerId) {
        try {
            Employee updatedEmployee = employeeService.assignEmployeeToManager(employeeId, managerId);
            return ResponseEntity.ok(updatedEmployee);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ 2. Create employee with default password and role
    @PostMapping("/create")
    public ResponseEntity<?> createEmployee(@RequestBody Map<String, Object> payload) {
        try {
            // Validate payload
            if (payload == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Request body is required"));
            }

            // Log the incoming request
            System.out.println("Creating employee with payload: " + payload);

            Employee createdEmployee = employeeService.createEmployeeWithOnboarding(payload);
            
            // Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Employee created successfully");
            response.put("employee", createdEmployee);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            // Handle validation errors
            System.err.println("Validation error in controller: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            
        } catch (Exception e) {
            // Handle unexpected errors
            System.err.println("Unexpected error in controller: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create employee: " + e.getMessage()));
        }
    }

    // ✅ 3. Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        String password = payload.get("password");

        Optional<Employee> employee = employeeRepository.findByNameAndPassword(name, password);

        if (employee.isPresent()) {
            Employee emp = employee.get();
            Map<String, Object> response = new HashMap<>();
            response.put("id", emp.getId());
            response.put("name", emp.getName());
            response.put("email", emp.getEmail());
            response.put("firstLogin", emp.getFirstLogin());
            response.put("role", emp.getRole());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        return employeeRepository.findById(id)

                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ 4. Reset Password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload) {
        String idStr = payload.get("id");
        String newPassword = payload.get("newPassword");

        if (idStr == null || newPassword == null) {
            return ResponseEntity.badRequest().body("Missing id or password");
        }

        try {
            Long id = Long.parseLong(idStr);
            Optional<Employee> empOpt = employeeRepository.findById(id);

            if (empOpt.isPresent()) {
                Employee emp = empOpt.get();
                emp.setPassword(newPassword);
                emp.setFirstLogin(false);
                employeeRepository.save(emp);
                return ResponseEntity.ok("Password reset successful");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found");
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid ID format");
        }
    }
}
