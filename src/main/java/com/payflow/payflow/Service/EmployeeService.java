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
            // Log the full payload for debugging
            System.out.println("Received payload: " + payload);
            
            // Validate required fields
            String name = (String) payload.get("name");
            String email = (String) payload.get("email");
            
            System.out.println("Name: " + name);
            System.out.println("Email: " + email);
            
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

            // Handle additional fields from payload
            if (payload.containsKey("phone")) {
                employee.setPhone((String) payload.get("phone"));
            }
            if (payload.containsKey("address")) {
                employee.setAddress((String) payload.get("address"));
            }
            if (payload.containsKey("position")) {
                employee.setPosition((String) payload.get("position"));
            }
            if (payload.containsKey("startDate") && payload.get("startDate") != null) {
                employee.setStartDate(java.time.LocalDate.parse((String) payload.get("startDate")));
            }
            if (payload.containsKey("experiences")) {
                Object experiences = payload.get("experiences");
                if (experiences != null) {
                    employee.setExperiences(experiences.toString());
                }
            }
            if (payload.containsKey("education")) {
                Object education = payload.get("education");
                if (education != null) {
                    employee.setEducation(education.toString());
                }
            }

            // Set manager ID if provided in payload
            if (payload.containsKey("managerId") && payload.get("managerId") != null) {
                Long managerId = Long.valueOf(payload.get("managerId").toString());
                employee.setManagerId(managerId);
            }

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

    public List<Employee> getEmployeesByManager(Long managerId) {
        return employeeRepository.findByManagerId(managerId);
    }

    public List<Employee> getUnassignedEmployees() {
        return employeeRepository.findByManagerIdIsNull();
    }

    public Employee assignEmployeeToManager(Long employeeId, Long managerId) throws Exception {
        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
        if (!employeeOpt.isPresent()) {
            throw new Exception("Employee not found");
        }
        
        Employee employee = employeeOpt.get();
        employee.setManagerId(managerId);
        return employeeRepository.save(employee);
    }
}
