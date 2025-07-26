package com.payflow.payflow.Service;

import com.payflow.payflow.model.Employee;
import com.payflow.payflow.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee createEmployeeWithOnboarding(Map<String, Object> payload) {
        String name = (String) payload.get("name");
        String email = (String) payload.get("email");

        // Default password and role
        String password = "1234";
        String role = "EMPLOYEE";

        Employee employee = new Employee(name, password, email);
        employee.setFirstLogin(true);
        employee.setRole(role);

        return employeeRepository.save(employee);
    }

    public Employee findByEmailAndPassword(String email, String password) {
        return employeeRepository.findByEmailAndPassword(email, password).orElse(null);
    }
}
