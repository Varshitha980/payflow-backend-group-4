package com.payflow.payflow.Controller;

import com.payflow.payflow.Service.EmployeeService;
import com.payflow.payflow.Service.PaymentHoldService;
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
    
    @Autowired
    private PaymentHoldService paymentHoldService;

    /**
     * Endpoint to retrieve a list of all employees in the system.
     * This method also checks if each employee has a payment hold and sets the hasPaymentHold flag accordingly.
     * @return A List of Employee objects with payment hold status.
     */
    @GetMapping
    public List<Employee> getAllEmployees() {
        List<Employee> employees = employeeService.getAllEmployees();
        
        // Check payment hold status for each employee
        for (Employee employee : employees) {
            boolean hasHold = paymentHoldService.hasPaymentHold(employee.getId());
            employee.setHasPaymentHold(hasHold);
        }
        
        return employees;
    }

    /**
     * Endpoint to retrieve all employees who report to a specific manager.
     * @param managerId The ID of the manager.
     * @return A List of Employee objects managed by the given manager.
     */
    @GetMapping("/manager/{managerId}")
    public List<Employee> getEmployeesByManager(@PathVariable Long managerId) {
        return employeeService.getEmployeesByManager(managerId);
    }

    /**
     * Endpoint to retrieve all employees who have not yet been assigned a manager.
     * This is useful for HR during the onboarding process.
     * @return A List of unassigned Employee objects.
     */
    @GetMapping("/unassigned")
    public List<Employee> getUnassignedEmployees() {
        return employeeService.getUnassignedEmployees();
    }

    /**
     * Endpoint to assign an existing employee to a manager.
     * @param employeeId The ID of the employee to be assigned.
     * @param managerId The ID of the manager they will report to.
     * @return A ResponseEntity containing the updated Employee object on success, or an error message on failure.
     */
    @PutMapping("/{employeeId}/assign-manager/{managerId}")
    public ResponseEntity<?> assignEmployeeToManager(@PathVariable Long employeeId, @PathVariable Long managerId) {
        try {
            Employee updatedEmployee = employeeService.assignEmployeeToManager(employeeId, managerId);
            return ResponseEntity.ok(updatedEmployee);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint to create a new employee with an onboarding process.
     * This method handles initial employee data creation, setting a default password, and role.
     * @param payload A Map containing the employee details.
     * @return A ResponseEntity with a status and a map containing the success status and the created employee object.
     */
    @PostMapping("/create")
    public ResponseEntity<?> createEmployee(@RequestBody Map<String, Object> payload) {
        try {
            if (payload == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Request body is required"));
            }
            System.out.println("Creating employee with payload: " + payload);
            Employee createdEmployee = employeeService.createEmployeeWithOnboarding(payload);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Employee created successfully");
            response.put("employee", createdEmployee);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            System.err.println("Validation error in controller: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Unexpected error in controller: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create employee: " + e.getMessage()));
        }
    }

    /**
     * Endpoint to retrieve a single employee by their ID.
     * This method also checks if the employee has a payment hold and sets the hasPaymentHold flag accordingly.
     * @param id The ID of the employee.
     * @return A ResponseEntity containing the Employee object if found, or a 404 Not Found status.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        return employeeRepository.findById(id)
                .map(employee -> {
                    // Check if employee has a payment hold
                    boolean hasHold = paymentHoldService.hasPaymentHold(employee.getId());
                    employee.setHasPaymentHold(hasHold);
                    return ResponseEntity.ok(employee);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // The 'login' and 'reset-password' endpoints have been removed to centralize all authentication
    // logic within the AuthController.
}
