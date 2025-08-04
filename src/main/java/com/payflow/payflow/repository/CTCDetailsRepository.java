package com.payflow.payflow.repository;

import com.payflow.payflow.model.CTCDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CTCDetailsRepository extends JpaRepository<CTCDetails, Long> {
    
    // Find all CTC records for an employee, ordered by effective date (newest first)
    @Query("SELECT c FROM CTCDetails c WHERE c.employeeId = :employeeId ORDER BY c.effectiveFrom DESC")
    List<CTCDetails> findByEmployeeIdOrderByEffectiveFromDesc(@Param("employeeId") Long employeeId);
    
    // Find the most recent CTC record for an employee
    @Query("SELECT c FROM CTCDetails c WHERE c.employeeId = :employeeId ORDER BY c.effectiveFrom DESC")
    Optional<CTCDetails> findFirstByEmployeeIdOrderByEffectiveFromDesc(@Param("employeeId") Long employeeId);
    
    // Find CTC records for an employee within a date range
    @Query("SELECT c FROM CTCDetails c WHERE c.employeeId = :employeeId AND c.effectiveFrom BETWEEN :startDate AND :endDate ORDER BY c.effectiveFrom DESC")
    List<CTCDetails> findByEmployeeIdAndEffectiveFromBetween(
            @Param("employeeId") Long employeeId, 
            @Param("startDate") java.time.LocalDate startDate, 
            @Param("endDate") java.time.LocalDate endDate);
}