package com.payflow.payflow.repository;

import com.payflow.payflow.model.CTCDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CTCDetailsRepository extends JpaRepository<CTCDetails, Long> {

    // Find all CTC records for an employee, ordered by effective date (newest first)
    List<CTCDetails> findByEmployeeIdOrderByEffectiveFromDesc(Long employeeId);

    // Find the most recent CTC record for an employee
    Optional<CTCDetails> findFirstByEmployeeIdOrderByEffectiveFromDesc(Long employeeId);

    // Find CTC records for an employee within a date range
    List<CTCDetails> findByEmployeeIdAndEffectiveFromBetween(
            Long employeeId,
            LocalDate startDate,
            LocalDate endDate);

    // Find the most recent CTC record for an employee effective before a given date
    List<CTCDetails> findByEmployeeIdAndEffectiveFromBeforeOrderByEffectiveFromDesc(
            Long employeeId,
            LocalDate date);
            
    // Find CTC records that are active on a specific date (effectiveFrom <= date <= effectiveTo or effectiveTo is null)
    @Query("SELECT c FROM CTCDetails c WHERE c.employeeId = :employeeId AND c.effectiveFrom <= :date AND (c.effectiveTo IS NULL OR c.effectiveTo >= :date) ORDER BY c.effectiveFrom DESC")
    List<CTCDetails> findActiveRecordsByEmployeeIdAndDate(
            @Param("employeeId") Long employeeId,
            @Param("date") LocalDate date);
            
    // Find CTC records that are active within a date range for payslip generation
    // This checks if the CTC is effective for any part of the specified month
    @Query("SELECT c FROM CTCDetails c WHERE c.employeeId = :employeeId AND " +
           "((c.effectiveFrom <= :monthEnd AND (c.effectiveTo IS NULL OR c.effectiveTo >= :monthStart)) OR " +
           "(c.effectiveFrom <= :monthStart AND (c.effectiveTo IS NULL OR c.effectiveTo >= :monthStart))) " +
           "ORDER BY c.effectiveFrom DESC")
    List<CTCDetails> findActiveRecordsByEmployeeIdAndDateRange(
            @Param("employeeId") Long employeeId,
            @Param("monthStart") LocalDate monthStart,
            @Param("monthEnd") LocalDate monthEnd);
}
