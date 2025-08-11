package com.payflow.payflow.Entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Represents a leave request submitted by an employee.
 * This entity maps to the `leave_request` table in the database.
 */
@Entity
@Table(name = "leave_request")
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "employee_email")
    private String employeeEmail;

    @Column(name = "start_date")
    private LocalDate startDate; // changed from String to LocalDate

    @Column(name = "end_date")
    private LocalDate endDate;   // changed from String to LocalDate

    private String reason;

    private String status; // PENDING, APPROVED, REJECTED

    @Column(name = "days")
    private Integer days;

    @Column(name = "salary_deducted")
    private Boolean salaryDeducted = false;

    @Column(name = "first_login")
    private Boolean firstLogin = true;

    @Column(name = "default_password")
    private String defaultPassword = "1234";

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeEmail() { return employeeEmail; }
    public void setEmployeeEmail(String employeeEmail) { this.employeeEmail = employeeEmail; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

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
