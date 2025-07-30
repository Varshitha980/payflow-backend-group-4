package com.payflow.payflow.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.payflow.payflow.Service.LeaveRequestService;
import com.payflow.payflow.Entity.LeaveRequest;
import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class LeaveRequestController {

    @Autowired
    private LeaveRequestService service;

    // ✅ Fixed: POST /api/leaves
    @PostMapping
    public LeaveRequest submitLeave(@RequestBody LeaveRequest request) {
        return service.submitLeaveRequest(request);
    }

    // GET /api/leaves/employee/{employeeId}
    @GetMapping("/employee/{employeeId}")
    public List<LeaveRequest> getEmployeeLeaves(@PathVariable Long employeeId) {
        return service.getLeaveRequestsByEmployee(employeeId);
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

    // Optional: fallback GET /api/leaves?employeeId=123
    @GetMapping
    public List<LeaveRequest> getLeavesByEmployeeId(@RequestParam(required = false) Long employeeId) {
        if (employeeId != null) {
            return service.getLeaveRequestsByEmployee(employeeId);
        } else {
            return service.getAllLeaveRequests();
        }
    }
}
