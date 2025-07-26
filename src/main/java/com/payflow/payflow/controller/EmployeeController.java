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

    // ✅ 2. Create employee with default password and role
    @PostMapping("/create")
    public Employee createEmployee(@RequestBody Map<String, Object> payload) throws Exception {
        return employeeService.createEmployeeWithOnboarding(payload);
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
            response.put("firstLogin", emp.isFirstLogin());
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
