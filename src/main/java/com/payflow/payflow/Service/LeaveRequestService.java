package com.payflow.payflow.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.payflow.payflow.repository.LeaveRequestRepository;
import com.payflow.payflow.repository.EmployeeRepository;
import com.payflow.payflow.Entity.LeaveRequest;
import com.payflow.payflow.model.Employee;

import java.util.List;
import java.util.ArrayList;
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
        
        // Calculate the number of days for this leave request
        int days = calculateDuration(request.getStartDate(), request.getEndDate());
        request.setDays(days);
        
        // If employeeId is provided but employeeEmail is not, fetch the email
        if (request.getEmployeeId() != null && (request.getEmployeeEmail() == null || request.getEmployeeEmail().trim().isEmpty())) {
            Employee employee = employeeRepository.findById(request.getEmployeeId()).orElse(null);
            if (employee != null) {
                request.setEmployeeEmail(employee.getEmail());
            }
        }
        
        // If employeeEmail is provided but employeeId is not, try to find the employee
        if (request.getEmployeeEmail() != null && request.getEmployeeId() == null) {
            Employee employee = employeeRepository.findByEmail(request.getEmployeeEmail()).orElse(null);
            if (employee != null) {
                request.setEmployeeId(employee.getId());
            }
        }
        
        return repository.save(request);
    }

    public List<LeaveRequest> getLeaveRequestsByEmployee(Long employeeId) {
        return repository.findByEmployeeId(employeeId);
    }

    public List<LeaveRequest> getLeaveRequestsByManager(Long managerId) {
        System.out.println("🔍 Getting leave requests for manager ID: " + managerId);
        
        // First get all employees assigned to this manager
        List<Employee> managerEmployees = employeeRepository.findByManagerId(managerId);
        System.out.println("👥 Employees assigned to manager: " + managerEmployees.size());
        managerEmployees.forEach(emp -> System.out.println("  - " + emp.getName() + " (" + emp.getEmail() + ")"));
        
        if (managerEmployees.isEmpty()) {
            System.out.println("❌ No employees assigned to manager " + managerId);
            return List.of(); // Return empty list if no employees assigned
        }
        
        // Collect all employee emails and IDs
        List<String> employeeEmails = managerEmployees.stream()
                .map(Employee::getEmail)
                .toList();
        List<Long> employeeIds = managerEmployees.stream()
                .map(Employee::getId)
                .toList();
        
        // Get leave requests by employee emails (more efficient)
        List<LeaveRequest> leavesByEmail = repository.findByEmployeeEmailIn(employeeEmails);
        System.out.println("📧 Found " + leavesByEmail.size() + " leave requests by email");
        
        // Get leave requests by employee IDs (more efficient)
        List<LeaveRequest> leavesById = repository.findByEmployeeIdIn(employeeIds);
        System.out.println("🆔 Found " + leavesById.size() + " leave requests by ID");
        
        // Combine and remove duplicates
        List<LeaveRequest> allLeaves = new ArrayList<>(leavesByEmail);
        for (LeaveRequest leave : leavesById) {
            if (!allLeaves.stream().anyMatch(existing -> existing.getId().equals(leave.getId()))) {
                allLeaves.add(leave);
            }
        }
        
        System.out.println("📋 Found " + allLeaves.size() + " leave requests for manager's team");
        allLeaves.forEach(leave -> System.out.println("  - Leave ID: " + leave.getId() + ", Employee: " + leave.getEmployeeEmail() + ", Status: " + leave.getStatus()));
        
        return allLeaves;
    }

    public List<LeaveRequest> getAllLeaveRequests() {
        return repository.findAll();
    }

    public LeaveRequest updateLeaveStatus(Long id, String status) {
        LeaveRequest req = repository.findById(id).orElseThrow();
        req.setStatus(status);
        
        // If leave is approved, update employee's leave balance and handle salary deductions
        if ("APPROVED".equals(status)) {
            Employee employee = employeeRepository.findById(req.getEmployeeId()).orElse(null);
            if (employee != null) {
                int leaveDays = req.getDays() != null ? req.getDays() : calculateDuration(req.getStartDate(), req.getEndDate());
                int currentBalance = employee.getLeaveBalance() != null ? employee.getLeaveBalance() : 12;
                
                if (currentBalance >= leaveDays) {
                    // Sufficient leave balance
                    employee.setLeaveBalance(currentBalance - leaveDays);
                    req.setSalaryDeducted(false);
                } else {
                    // Insufficient leave balance - salary deduction
                    int excessDays = leaveDays - currentBalance;
                    employee.setLeaveBalance(0);
                    employee.setSalaryDeductionDays(employee.getSalaryDeductionDays() + excessDays);
                    req.setSalaryDeducted(true);
                }
                
                employeeRepository.save(employee);
            }
        }
        
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
        body.append("• Duration: ").append(leaveRequest.getDays() != null ? leaveRequest.getDays() : calculateDuration(leaveRequest.getStartDate(), leaveRequest.getEndDate())).append(" days\n");
        body.append("• Reason: ").append(leaveRequest.getReason()).append("\n");
        body.append("• Status: ").append(status).append("\n");
        
        if ("APPROVED".equals(status) && leaveRequest.getSalaryDeducted() != null && leaveRequest.getSalaryDeducted()) {
            body.append("• ⚠️  Salary Deduction: Yes (insufficient leave balance)\n");
        }
        body.append("\n");
        
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
