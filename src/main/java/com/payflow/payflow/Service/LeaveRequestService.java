package com.payflow.payflow.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.payflow.payflow.repository.LeaveRequestRepository;
import com.payflow.payflow.repository.EmployeeRepository;
import com.payflow.payflow.Entity.LeaveRequest;
import com.payflow.payflow.model.Employee;

import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
        return repository.save(request);
    }

    public List<LeaveRequest> getLeaveRequestsByEmployee(Long employeeId) {
        return repository.findByEmployeeId(employeeId);
    }

    public List<LeaveRequest> getAllLeaveRequests() {
        return repository.findAll();
    }

    public LeaveRequest updateLeaveStatus(Long id, String status) {
        LeaveRequest req = repository.findById(id).orElseThrow();
        req.setStatus(status);
        LeaveRequest updated = repository.save(req);

        // ✅ Send detailed email notification
        sendLeaveStatusEmail(req, status);

        return updated;
    }

    private void sendLeaveStatusEmail(LeaveRequest leaveRequest, String status) {
        try {
            System.out.println("📧 Leave status changed to: " + status);
            System.out.println("📧 Employee ID: " + leaveRequest.getEmployeeId());
            
            // Fetch employee email from database
            Employee employee = employeeRepository.findById(leaveRequest.getEmployeeId()).orElse(null);
            if (employee == null) {
                System.err.println("❌ Employee not found with ID: " + leaveRequest.getEmployeeId());
                return;
            }
            
            String employeeEmail = employee.getEmail();
            System.out.println("📧 Employee Email from DB: " + employeeEmail);
            
            if (employeeEmail == null || employeeEmail.trim().isEmpty()) {
                System.err.println("❌ Employee email is null or empty for employee ID: " + leaveRequest.getEmployeeId());
                return;
            }
            
            String subject = "Leave Request " + status + " - PayFlow HR System";
            String emailBody = buildEmailBody(leaveRequest, status);
            
            emailService.sendSimpleMessage(employeeEmail, subject, emailBody);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String buildEmailBody(LeaveRequest leaveRequest, String status) {
        StringBuilder body = new StringBuilder();
        
        body.append("Dear Employee,\n\n");
        
        if ("APPROVED".equals(status)) {
            body.append("🎉 Your leave request has been APPROVED!\n\n");
        } else if ("REJECTED".equals(status)) {
            body.append("❌ Your leave request has been REJECTED.\n\n");
        }
        
        body.append("Leave Request Details:\n");
        body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        body.append("• Employee ID: ").append(leaveRequest.getEmployeeId()).append("\n");
        body.append("• Start Date: ").append(formatDate(leaveRequest.getStartDate())).append("\n");
        body.append("• End Date: ").append(formatDate(leaveRequest.getEndDate())).append("\n");
        body.append("• Duration: ").append(calculateDuration(leaveRequest.getStartDate(), leaveRequest.getEndDate())).append(" days\n");
        body.append("• Reason: ").append(leaveRequest.getReason()).append("\n");
        body.append("• Status: ").append(status).append("\n\n");
        
        if ("APPROVED".equals(status)) {
            body.append("✅ Your leave has been approved. Please ensure to:\n");
            body.append("• Hand over your responsibilities to your team members\n");
            body.append("• Set up out-of-office notifications\n");
            body.append("• Update your calendar\n\n");
        } else if ("REJECTED".equals(status)) {
            body.append("❌ Your leave request has been rejected. Please:\n");
            body.append("• Contact your manager for more details\n");
            body.append("• Consider alternative dates if possible\n");
            body.append("• Ensure proper coverage for your responsibilities\n\n");
        }
        
        body.append("If you have any questions, please contact your manager or HR department.\n\n");
        body.append("Best regards,\n");
        body.append("PayFlow HR Team\n");
        body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        body.append("This is an automated message from the PayFlow HR System.");
        
        return body.toString();
    }

    private String formatDate(String dateString) {
        try {
            LocalDate date = LocalDate.parse(dateString);
            return date.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"));
        } catch (Exception e) {
            return dateString;
        }
    }

    private int calculateDuration(String startDate, String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            return (int) (end.toEpochDay() - start.toEpochDay()) + 1;
        } catch (Exception e) {
            return 0;
        }
    }
}
