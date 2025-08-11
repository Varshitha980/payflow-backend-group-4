package com.payflow.payflow.repository;

import com.payflow.payflow.Entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeId(Long employeeId);
    List<LeaveRequest> findByEmployeeIdIn(List<Long> employeeIds);
    List<LeaveRequest> findByEmployeeEmail(String employeeEmail);
    List<LeaveRequest> findByEmployeeEmailIn(List<String> employeeEmails);

    List<LeaveRequest> findByEmployeeIdAndStatusAndStartDateBetween(
            Long employeeId, String status, LocalDate startDate, LocalDate endDate);
}
