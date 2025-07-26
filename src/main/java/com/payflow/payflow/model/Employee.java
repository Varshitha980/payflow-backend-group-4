package com.payflow.payflow.model;

import jakarta.persistence.*;

@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String password;
    private String email;
    private boolean firstLogin = true;
    private String role;

    // Constructors
    public Employee() {}

    public Employee(String name, String password, String email) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.role = "EMPLOYEE";
        this.firstLogin = true;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isFirstLogin() { return firstLogin; }
    public void setFirstLogin(boolean firstLogin) { this.firstLogin = firstLogin; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
