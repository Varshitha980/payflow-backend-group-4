package com.payflow.payflow.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.payflow.payflow.Service.LeaveRequestService;
import com.payflow.payflow.Entity.LeaveRequest;
import com.payflow.payflow.model.Employee;
import com.payflow.payflow.repository.EmployeeRepository;
import com.payflow.payflow.repository.LeaveRequestRepository;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/leaves")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class LeaveRequestController {

    @Autowired
    private LeaveRequestService service;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeaveRequestRepository repository;

    // ✅ Fixed: POST /api/leaves
    @PostMapping
    public LeaveRequest submitLeave(@RequestBody LeaveRequest request) {
        try {
            System.out.println("📝 Received leave request: " + request);
            System.out.println("📝 Employee ID: " + request.getEmployeeId());
            System.out.println("📝 Start Date: " + request.getStartDate());
            System.out.println("📝 End Date: " + request.getEndDate());
            System.out.println("📝 Reason: " + request.getReason());
            
            LeaveRequest result = service.submitLeaveRequest(request);
            System.out.println("✅ Leave request submitted successfully: " + result.getId());
            return result;
        } catch (Exception e) {
            System.err.println("❌ Error submitting leave request: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // ✅ NEW: POST /api/leaves/create (alternative endpoint)
    @PostMapping("/create")
    public LeaveRequest createLeave(@RequestBody LeaveRequest request) {
        return service.submitLeaveRequest(request);
    }

    // GET /api/leaves/employee/{employeeId}
    @GetMapping("/employee/{employeeId}")
    public List<LeaveRequest> getEmployeeLeaves(@PathVariable Long employeeId) {
        return service.getLeaveRequestsByEmployee(employeeId);
    }

    // ✅ NEW: Get leave requests for employees assigned to a manager
    @GetMapping("/manager/{managerId}")
    public List<LeaveRequest> getLeavesByManager(@PathVariable Long managerId) {
        System.out.println("🎯 Controller: Getting leave requests for manager " + managerId);
        List<LeaveRequest> leaves = service.getLeaveRequestsByManager(managerId);
        System.out.println("🎯 Controller: Returning " + leaves.size() + " leave requests");
        return leaves;
    }

    // GET /api/leaves/all
    @GetMapping("/all")
    public List<LeaveRequest> getAllLeaves() {
        return service.getAllLeaveRequests();
    }

    // PUT /api/leaves/update/{id}?status=Approved
    @PutMapping("/update/{id}")
    public LeaveRequest updateLeaveStatus(@PathVariable Long id, @RequestParam String status) {
        return service.updateLeaveStatus(id, status);
    }

    // ✅ NEW: Specific approve endpoint
    @PutMapping("/{id}/approve")
    public LeaveRequest approveLeave(@PathVariable Long id) {
        return service.updateLeaveStatus(id, "APPROVED");
    }

    // ✅ NEW: Specific reject endpoint
    @PutMapping("/{id}/reject")
    public LeaveRequest rejectLeave(@PathVariable Long id) {
        return service.updateLeaveStatus(id, "REJECTED");
    }

    // General fallback GET /api/leaves?employeeId=123 (must be last)
    @GetMapping
    public List<LeaveRequest> getLeavesByEmployeeId(@RequestParam(required = false) Long employeeId) {
        if (employeeId != null) {
            return service.getLeaveRequestsByEmployee(employeeId);
        } else {
            return service.getAllLeaveRequests();
        }
    }

    // ✅ NEW: Debug endpoint to check all leave requests and employees
    @GetMapping("/debug")
    public Map<String, Object> debugLeaveRequests() {
        Map<String, Object> debug = new HashMap<>();
        
        // Get all leave requests
        List<LeaveRequest> allLeaves = service.getAllLeaveRequests();
        debug.put("totalLeaveRequests", allLeaves.size());
        debug.put("leaveRequests", allLeaves.stream().map(leave -> Map.of(
            "id", leave.getId(),
            "employeeId", leave.getEmployeeId(),
            "employeeEmail", leave.getEmployeeEmail(),
            "status", leave.getStatus(),
            "startDate", leave.getStartDate(),
            "endDate", leave.getEndDate()
        )).toList());
        
        // Get all employees with their manager assignments
        List<Employee> allEmployees = employeeRepository.findAll();
        debug.put("totalEmployees", allEmployees.size());
        debug.put("employees", allEmployees.stream().map(emp -> Map.of(
            "id", emp.getId(),
            "name", emp.getName(),
            "email", emp.getEmail(),
            "managerId", emp.getManagerId(),
            "role", emp.getRole()
        )).toList());
        
        return debug;
    }

    // ✅ NEW: Create test data for debugging
    @PostMapping("/create-test-data")
    public Map<String, Object> createTestData() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Find a manager
            List<Employee> managers = employeeRepository.findAll().stream()
                    .filter(emp -> "MANAGER".equals(emp.getRole()))
                    .toList();
            
            if (managers.isEmpty()) {
                result.put("error", "No managers found. Please create a manager first.");
                return result;
            }
            
            Employee manager = managers.get(0);
            
            // Find employees assigned to this manager
            List<Employee> assignedEmployees = employeeRepository.findByManagerId(manager.getId());
            
            if (assignedEmployees.isEmpty()) {
                result.put("error", "No employees assigned to manager " + manager.getName() + ". Please assign employees first.");
                return result;
            }
            
            Employee employee = assignedEmployees.get(0);
            
            // Create a test leave request
            LeaveRequest testLeave = new LeaveRequest();
            testLeave.setEmployeeEmail(employee.getEmail());
            testLeave.setEmployeeId(employee.getId());
            testLeave.setStartDate(java.time.LocalDate.now().plusDays(7).toString());
            testLeave.setEndDate(java.time.LocalDate.now().plusDays(10).toString());
            testLeave.setReason("Test leave request created by debug endpoint");
            testLeave.setStatus("PENDING");
            
            LeaveRequest savedLeave = repository.save(testLeave);
            
            result.put("success", true);
            result.put("message", "Test leave request created successfully");
            result.put("manager", Map.of("id", manager.getId(), "name", manager.getName()));
            result.put("employee", Map.of("id", employee.getId(), "name", employee.getName(), "email", employee.getEmail()));
            result.put("leaveRequest", Map.of(
                "id", savedLeave.getId(),
                "employeeEmail", savedLeave.getEmployeeEmail(),
                "employeeId", savedLeave.getEmployeeId(),
                "status", savedLeave.getStatus()
            ));
            
        } catch (Exception e) {
            result.put("error", "Failed to create test data: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }

    // ✅ NEW: Simple test endpoint
    @GetMapping("/test")
    public Map<String, Object> testEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Leave request endpoint is working");
        response.put("timestamp", java.time.LocalDateTime.now());
        return response;
    }

    // ✅ NEW: Database schema check endpoint
    @GetMapping("/schema-check")
    public Map<String, Object> checkSchema() {
        Map<String, Object> response = new HashMap<>();
        try {
            // Try to create a simple leave request to test the schema
            LeaveRequest testRequest = new LeaveRequest();
            testRequest.setEmployeeId(1L);
            testRequest.setStartDate("2025-08-05");
            testRequest.setEndDate("2025-08-07");
            testRequest.setReason("Schema test");
            testRequest.setStatus("PENDING");
            testRequest.setDays(3);
            testRequest.setSalaryDeducted(false);
            
            // Try to save it
            LeaveRequest saved = repository.save(testRequest);
            
            response.put("success", true);
            response.put("message", "Schema is working correctly");
            response.put("testLeaveId", saved.getId());
            
            // Clean up - delete the test record
            repository.deleteById(saved.getId());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());
            e.printStackTrace();
        }
        return response;
    }
}
