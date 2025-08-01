package com.payflow.payflow.repository;

import com.payflow.payflow.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByNameAndPassword(String name, String password);
    Optional<Employee> findByEmailAndPassword(String email, String password);
    
    // ✅ NEW: Find employee by email only (for duplicate checking)
    Optional<Employee> findByEmail(String email);
    
    // ✅ NEW: Find employees by manager ID
    List<Employee> findByManagerId(Long managerId);
    
    // ✅ NEW: Find employees without manager (unassigned)
    List<Employee> findByManagerIdIsNull();
}
