package com.payflow.payflow.repository;

import com.payflow.payflow.model.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PayslipRepository extends JpaRepository<Payslip, Long> {
    
    // Find all payslips for an employee, ordered by year and month (newest first)
    @Query("SELECT p FROM Payslip p WHERE p.employeeId = :employeeId ORDER BY p.year DESC, p.month DESC")
    List<Payslip> findByEmployeeIdOrderByYearDescMonthDesc(@Param("employeeId") Long employeeId);
    
    // Find a specific payslip by employee, month and year
    Optional<Payslip> findByEmployeeIdAndMonthAndYear(Long employeeId, String month, Integer year);
    
    // Find payslips for a specific year
    List<Payslip> findByYear(Integer year);
    
    // Find payslips for a specific month and year
    List<Payslip> findByMonthAndYear(String month, Integer year);
}