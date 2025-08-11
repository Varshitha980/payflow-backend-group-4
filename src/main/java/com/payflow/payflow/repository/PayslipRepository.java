package com.payflow.payflow.repository;

import com.payflow.payflow.model.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PayslipRepository extends JpaRepository<Payslip, Long> {

    // Find a payslip by employee, month, and year
    Optional<Payslip> findByEmployeeIdAndMonthAndYear(Long employeeId, String month, Integer year);

    // Find all payslips for an employee, sorted by year and month
    List<Payslip> findByEmployeeIdOrderByYearDescMonthDesc(Long employeeId);

    // Delete all payslips for a given month and year
    @Transactional
    void deleteByMonthAndYear(String month, Integer year);
}
