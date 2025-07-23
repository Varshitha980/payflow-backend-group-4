package com.payflow.payflow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.payflow.model.Employee;
import com.payflow.payflow.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ObjectMapper objectMapper;
    
    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }


    @PostMapping("/create")
    public Employee createEmployee(@RequestBody Map<String, Object> payload) throws Exception {
        Employee emp = new Employee();
        emp.setName((String) payload.get("name"));
        emp.setEmail((String) payload.get("email"));
        emp.setPhone((String) payload.get("phone"));
        emp.setAddress((String) payload.get("address"));
        emp.setPosition((String) payload.get("position"));
        emp.setStatus((String) payload.getOrDefault("status", "Active"));

        // Fix: handle leaves as Number and default to 12
        Object leavesObj = payload.get("leaves");
        emp.setLeaves(leavesObj != null ? ((Number) leavesObj).intValue() : 12);

        // Parse startDate if present
        if (payload.get("startDate") != null) {
            emp.setStartDate(java.time.LocalDate.parse((String) payload.get("startDate")));
        }

        // Convert education and experiences to JSON strings
        emp.setEducation(objectMapper.writeValueAsString(payload.get("education")));
        emp.setExperiences(objectMapper.writeValueAsString(payload.get("experiences")));

        return employeeRepository.save(emp);
    }
}