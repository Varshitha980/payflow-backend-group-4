package com.payflow.payflow.repository;

import com.payflow.payflow.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByNameAndPassword(String name, String password);
    Optional<Employee> findByEmailAndPassword(String email, String password);
}
