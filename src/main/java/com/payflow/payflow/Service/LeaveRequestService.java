package com.payflow.payflow.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.payflow.payflow.repository.LeaveRequestRepository;
import com.payflow.payflow.Entity.LeaveRequest;

import java.util.List;

@Service
public class LeaveRequestService {

    @Autowired
    private LeaveRequestRepository repository;

    @Autowired
    private JavaMailSender mailSender;

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

        // ✅ Send email notification
        sendEmail(
                req.getEmployeeEmail(),
                "Leave Request " + status,
                "Hi, your leave request from " + req.getStartDate() +
                        " to " + req.getEndDate() + " has been " + status.toLowerCase() + "."
        );

        return updated;
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            System.out.println("Failed to send email: " + e.getMessage());
        }
    }
}
