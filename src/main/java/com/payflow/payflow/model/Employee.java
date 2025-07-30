package com.payflow.payflow.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String phone;
    private String address;
    private String position;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    private Integer leaves;
    private String status;
    private String password;
    
    @Column(name = "first_login")
    private Boolean firstLogin = true;
    
    @Column(columnDefinition = "LONGTEXT")
    private String education;
    
    @Column(columnDefinition = "LONGTEXT")
    private String experiences;
    
    private String role;
    @Column(name = "age", nullable = false)
    private Integer age;
    
    @Column(name = "leave_balance")
    private Integer leaveBalance;
    
    @Column(name = "past_experience")
    private String pastExperience;
    
    @Column(name = "total_experience", nullable = false)
    private Integer totalExperience;
    
    private String username;

    // Constructors
    public Employee() {}

    public Employee(String name, String email) {
        this.name = name;
        this.email = email;
        this.role = "EMPLOYEE";
        this.firstLogin = true;
        this.status = "ACTIVE";
        this.leaves = 12;
        this.leaveBalance = 12;
        this.age = 25; // Default age
        this.totalExperience = 0; // Default experience
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public Integer getLeaves() { return leaves; }
    public void setLeaves(Integer leaves) { this.leaves = leaves; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Boolean getFirstLogin() { return firstLogin; }
    public void setFirstLogin(Boolean firstLogin) { this.firstLogin = firstLogin; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public String getExperiences() { return experiences; }
    public void setExperiences(String experiences) { this.experiences = experiences; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public Integer getLeaveBalance() { return leaveBalance; }
    public void setLeaveBalance(Integer leaveBalance) { this.leaveBalance = leaveBalance; }

    public String getPastExperience() { return pastExperience; }
    public void setPastExperience(String pastExperience) { this.pastExperience = pastExperience; }

    public Integer getTotalExperience() { return totalExperience; }
    public void setTotalExperience(Integer totalExperience) { this.totalExperience = totalExperience; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
