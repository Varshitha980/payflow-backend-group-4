package com.payflow.payflow.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "leave_request")
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "employee_id")
    private Long employeeId;
    
    @Column(name = "employee_email")
    private String employeeEmail; // ✅ NEW FIELD
    
    @Column(name = "start_date")
    private String startDate;
    
    @Column(name = "end_date")
    private String endDate;
    
    private String reason;
    private String status; // PENDING, APPROVED, REJECTED
    
    @Column(name = "days")
    private Integer days; // Number of days for this leave request
    
    @Column(name = "salary_deducted")
    private Boolean salaryDeducted = false; // Track if salary was deducted for this leave
    
    @Column(name = "first_login")
    private Boolean firstLogin = true; // Keep existing field
    
    @Column(name = "default_password")
    private String defaultPassword = "1234"; // Keep existing field

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeEmail() { return employeeEmail; }
    public void setEmployeeEmail(String employeeEmail) { this.employeeEmail = employeeEmail; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getDays() { return days; }
    public void setDays(Integer days) { this.days = days; }

    public Boolean getSalaryDeducted() { return salaryDeducted; }
    public void setSalaryDeducted(Boolean salaryDeducted) { this.salaryDeducted = salaryDeducted; }
    
    public Boolean getFirstLogin() { return firstLogin; }
    public void setFirstLogin(Boolean firstLogin) { this.firstLogin = firstLogin; }
    
    public String getDefaultPassword() { return defaultPassword; }
    public void setDefaultPassword(String defaultPassword) { this.defaultPassword = defaultPassword; }
}

