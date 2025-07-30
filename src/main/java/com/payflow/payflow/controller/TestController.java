package com.payflow.payflow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import com.payflow.payflow.Service.EmployeeService;
import com.payflow.payflow.model.Employee;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class TestController {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/email")
    public String testEmail(@RequestParam String to, @RequestParam String subject, @RequestParam String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            return "Email sent successfully to: " + to;
        } catch (Exception e) {
            return "Failed to send email: " + e.getMessage();
        }
    }

    @GetMapping("/health")
    public String health() {
        return "PayFlow Backend is running! Email service is configured.";
    }

    // ✅ NEW: Test employee creation endpoint
    @PostMapping("/create-employee")
    public Map<String, Object> testCreateEmployee(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== EMPLOYEE CREATION DEBUG ===");
            System.out.println("Received payload: " + payload);
            System.out.println("Payload keys: " + payload.keySet());
            
            // Check each field
            for (Map.Entry<String, Object> entry : payload.entrySet()) {
                System.out.println("Field: " + entry.getKey() + " = " + entry.getValue() + " (Type: " + (entry.getValue() != null ? entry.getValue().getClass().getSimpleName() : "null") + ")");
            }
            
            Employee createdEmployee = employeeService.createEmployeeWithOnboarding(payload);
            
            System.out.println("Employee created successfully: " + createdEmployee.getName());
            System.out.println("Employee ID: " + createdEmployee.getId());
            System.out.println("=== END DEBUG ===");
            
            response.put("success", true);
            response.put("message", "Employee created successfully");
            response.put("employee", createdEmployee);
            
        } catch (Exception e) {
            System.err.println("=== ERROR DEBUG ===");
            System.err.println("Error type: " + e.getClass().getSimpleName());
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("=== END ERROR DEBUG ===");
            
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());
        }
        
        return response;
    }
}