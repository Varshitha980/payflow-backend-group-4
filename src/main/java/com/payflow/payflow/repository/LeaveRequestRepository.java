package com.payflow.payflow.repository;

import com.payflow.payflow.Entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeId(Long employeeId);  // required by service
    List<LeaveRequest> findByEmployeeIdIn(List<Long> employeeIds);  // NEW: find by multiple IDs
    List<LeaveRequest> findByEmployeeEmail(String employeeEmail);  // NEW: find by email
    List<LeaveRequest> findByEmployeeEmailIn(List<String> employeeEmails);  // NEW: find by multiple emails
}
