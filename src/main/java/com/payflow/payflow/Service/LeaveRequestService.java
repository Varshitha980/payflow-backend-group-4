package com.payflow.payflow.Service;

import com.payflow.payflow.Entity.LeaveRequest;
import com.payflow.payflow.model.Employee;
import com.payflow.payflow.repository.EmployeeRepository;
import com.payflow.payflow.repository.LeaveRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LeaveRequestService {

    @Autowired
    private LeaveRequestRepository repository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmailService emailService;

    public LeaveRequest submitLeaveRequest(LeaveRequest request) {
        request.setStatus("PENDING");

        // Calculate days between start and end date
        int days = calculateDuration(request.getStartDate(), request.getEndDate());
        request.setDays(days);

        // If email missing, fetch from employee table
        if (request.getEmployeeId() != null &&
                (request.getEmployeeEmail() == null || request.getEmployeeEmail().trim().isEmpty())) {
            employeeRepository.findById(request.getEmployeeId())
                    .ifPresent(emp -> request.setEmployeeEmail(emp.getEmail()));
        }

        LeaveRequest submittedRequest = repository.save(request);

        // Notify manager
        employeeRepository.findById(submittedRequest.getEmployeeId()).ifPresent(employee -> {
            if (employee.getManagerId() != null) {
                employeeRepository.findById(employee.getManagerId()).ifPresent(manager -> {
                    String subject = "New Leave Request from " + employee.getName();
                    String body = String.format(
                            "Dear %s,\n\nA new leave request has been submitted by %s from %s to %s.\n\nPlease review and take action.\n\nRegards,\nPayFlow HR System",
                            manager.getName(),
                            employee.getName(),
                            formatDate(submittedRequest.getStartDate()),
                            formatDate(submittedRequest.getEndDate())
                    );
                    emailService.sendSimpleMessage(manager.getEmail(), subject, body);
                });
            }
        });

        return submittedRequest;
    }

    public List<LeaveRequest> getLeaveRequestsByEmployee(Long employeeId) {
        return repository.findByEmployeeId(employeeId);
    }

    public List<LeaveRequest> getAllLeaveRequests() {
        return repository.findAll();
    }

    public List<LeaveRequest> getLeaveRequestsByManager(Long managerId) {
        List<Employee> managedEmployees = employeeRepository.findByManagerId(managerId);
        List<Long> employeeIds = new ArrayList<>();
        managedEmployees.forEach(emp -> employeeIds.add(emp.getId()));

        if (employeeIds.isEmpty()) return new ArrayList<>();
        return repository.findByEmployeeIdIn(employeeIds);
    }

    public LeaveRequest updateLeaveStatus(Long id, String status) {
        LeaveRequest request = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found: " + id));

        request.setStatus(status);
        LeaveRequest updatedRequest = repository.save(request);

        employeeRepository.findById(updatedRequest.getEmployeeId()).ifPresent(employee -> {
            String subject = "Your Leave Request Status Has Been Updated";
            String body = buildStatusEmailBody(updatedRequest, employee, status);
            emailService.sendSimpleMessage(employee.getEmail(), subject, body);
        });

        return updatedRequest;
    }

    // --- Helper methods ---
    private String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"));
    }

    private int calculateDuration(LocalDate startDate, LocalDate endDate) {
        return (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
    }

    private String buildStatusEmailBody(LeaveRequest request, Employee employee, String status) {
        StringBuilder body = new StringBuilder();
        body.append("Hello ").append(employee.getName()).append(",\n\n")
                .append("Your leave request has been updated.\n\n")
                .append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                .append("🗓️  **Leave Details**\n")
                .append("• Request ID: ").append(request.getId()).append("\n")
                .append("• Dates: ").append(formatDate(request.getStartDate())).append(" to ").append(formatDate(request.getEndDate())).append("\n")
                .append("• Duration: ").append(request.getDays()).append(" day(s)\n")
                .append("• Reason: ").append(request.getReason()).append("\n")
                .append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

        if ("APPROVED".equals(status)) {
            body.append("✅ Your leave has been approved.\n");
        } else if ("REJECTED".equals(status)) {
            body.append("❌ Your leave has been rejected.\n");
        }

        body.append("\nBest regards,\nPayFlow HR Team");
        return body.toString();
    }
}
