package com.payflow.payflow.Service;

import com.payflow.payflow.model.Employee;
import com.payflow.payflow.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee createEmployeeWithOnboarding(Map<String, Object> payload) throws Exception {
        try {
            // Validate required fields
            String name = (String) payload.get("name");
            String email = (String) payload.get("email");
            
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Employee name is required");
            }
            
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("Employee email is required");
            }

            // Check if employee with same email already exists
            Optional<Employee> existingEmployee = employeeRepository.findByEmail(email);
            if (existingEmployee.isPresent()) {
                throw new IllegalArgumentException("Employee with this email already exists");
            }

            // Default password and role
            String password = "1234";
            String role = "EMPLOYEE";

            // Create new employee
            Employee employee = new Employee();
            employee.setName(name.trim());
            employee.setEmail(email.trim());
            employee.setPassword(password);
            employee.setRole(role);
            employee.setFirstLogin(true);
            employee.setStatus("ACTIVE");
            employee.setLeaves(12);
            employee.setLeaveBalance(12);
            employee.setAge(25); // Default age
            employee.setTotalExperience(0); // Default experience
            employee.setUsername(email.trim()); // Use email as username

            // Save to database
            Employee savedEmployee = employeeRepository.save(employee);
            
            // Log success
            System.out.println("Employee created successfully: " + savedEmployee.getName() + " (" + savedEmployee.getEmail() + ")");
            
            return savedEmployee;
            
        } catch (IllegalArgumentException e) {
            // Log validation errors
            System.err.println("Validation error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            // Log unexpected errors
            System.err.println("Error creating employee: " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Failed to create employee: " + e.getMessage());
        }
    }

    public Employee findByEmailAndPassword(String email, String password) {
        return employeeRepository.findByEmailAndPassword(email, password).orElse(null);
    }
}
