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

    private LocalDate startDate;

    private int leaves = 12;
    private String status = "Active";

    @Lob
    private String education; // Store as JSON string

    @Lob
    private String experiences; // Store as JSON string

    // Optionally keep these if you want
    // private int totalExperience;
    // private String pastExperience;

    // Getters and Setters
    public Long getId() { return id; }
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

    public int getLeaves() { return leaves; }
    public void setLeaves(int leaves) { this.leaves = leaves; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public String getExperiences() { return experiences; }
    public void setExperiences(String experiences) { this.experiences = experiences; }
}