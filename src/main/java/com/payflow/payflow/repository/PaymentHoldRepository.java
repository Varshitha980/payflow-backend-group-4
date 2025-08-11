package com.payflow.payflow.repository;

import com.payflow.payflow.model.PaymentHold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for PaymentHold entity.
 * Provides methods to interact with the payment_hold table in the database.
 */
@Repository
public interface PaymentHoldRepository extends JpaRepository<PaymentHold, Long> {
    
    /**
     * Find a payment hold by employee ID.
     * 
     * @param employeeId The ID of the employee.
     * @return An Optional containing the payment hold if found, or empty if not found.
     */
    Optional<PaymentHold> findByEmployeeId(Long employeeId);
    
    /**
     * Find all payment holds for all employees.
     * 
     * @return A list of all payment holds.
     */
    List<PaymentHold> findAll();
    
    /**
     * Delete a payment hold by employee ID.
     * 
     * @param employeeId The ID of the employee.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PaymentHold ph WHERE ph.employeeId = :employeeId")
    void deleteByEmployeeId(@Param("employeeId") Long employeeId);
}