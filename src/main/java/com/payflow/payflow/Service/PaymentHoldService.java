package com.payflow.payflow.Service;

import com.payflow.payflow.model.PaymentHold;
import com.payflow.payflow.repository.PaymentHoldRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for managing payment holds.
 * This class provides business logic for applying, removing, and retrieving payment holds.
 */
@Service
public class PaymentHoldService {

    private final PaymentHoldRepository paymentHoldRepository;
    private final EmployeeService employeeService;

    /**
     * Constructor for dependency injection.
     * 
     * @param paymentHoldRepository Repository for payment hold operations.
     * @param employeeService Service for employee operations.
     */
    @Autowired
    public PaymentHoldService(PaymentHoldRepository paymentHoldRepository, EmployeeService employeeService) {
        this.paymentHoldRepository = paymentHoldRepository;
        this.employeeService = employeeService;
    }

    /**
     * Get all employees with payment holds.
     * 
     * @return A list of all payment holds.
     */
    public List<PaymentHold> getAllPaymentHolds() {
        try {
            System.out.println("Attempting to fetch all payment holds...");
            List<PaymentHold> holds = paymentHoldRepository.findAll();
            System.out.println("Successfully fetched " + holds.size() + " payment holds");
            return holds;
        } catch (Exception e) {
            System.out.println("Error fetching payment holds: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch payment holds: " + e.getMessage(), e);
        }
    }

    /**
     * Check if an employee has a payment hold.
     * 
     * @param employeeId The ID of the employee.
     * @return true if the employee has a payment hold, false otherwise.
     */
    public boolean hasPaymentHold(Long employeeId) {
        boolean hasHold = paymentHoldRepository.findByEmployeeId(employeeId).isPresent();
        System.out.println("Checking payment hold for employee " + employeeId + ": " + hasHold);
        return hasHold;
    }

    /**
     * Get payment hold details for an employee.
     * 
     * @param employeeId The ID of the employee.
     * @return An Optional containing the payment hold if found, or empty if not found.
     */
    public Optional<PaymentHold> getPaymentHoldByEmployeeId(Long employeeId) {
        return paymentHoldRepository.findByEmployeeId(employeeId);
    }

    /**
     * Apply a payment hold for an employee.
     * 
     * @param employeeId The ID of the employee.
     * @param reason The reason for the payment hold.
     * @param appliedBy The ID of the user applying the hold.
     * @return A map containing the result of the operation.
     */
    public Map<String, Object> applyPaymentHold(Long employeeId, String reason, Long appliedBy) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("Applying payment hold for employee: " + employeeId + ", reason: " + reason + ", appliedBy: " + appliedBy);
            
            // Check if employee exists
            if (employeeService.getEmployeeById(employeeId) == null) {
                System.out.println("Employee not found: " + employeeId);
                response.put("success", false);
                response.put("message", "Employee not found");
                return response;
            }
            
            // Check if payment hold already exists
            Optional<PaymentHold> existingHold = paymentHoldRepository.findByEmployeeId(employeeId);
            if (existingHold.isPresent()) {
                System.out.println("Payment hold already exists for employee: " + employeeId);
                response.put("success", false);
                response.put("message", "Payment hold already exists for this employee");
                return response;
            }
            
            // Create and save new payment hold
            PaymentHold paymentHold = new PaymentHold();
            paymentHold.setEmployeeId(employeeId);
            paymentHold.setReason(reason);
            paymentHold.setAppliedBy(appliedBy);
            paymentHold.setAppliedOn(LocalDateTime.now());
            
            PaymentHold savedHold = paymentHoldRepository.save(paymentHold);
            System.out.println("Payment hold saved successfully: " + savedHold.getId());
            
            response.put("success", true);
            response.put("message", "Payment hold applied successfully");
            response.put("paymentHold", savedHold);
            
        } catch (Exception e) {
            System.out.println("Error applying payment hold: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error applying payment hold: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Remove a payment hold for an employee.
     * 
     * @param employeeId The ID of the employee.
     * @return A map containing the result of the operation.
     */
    public Map<String, Object> removePaymentHold(Long employeeId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if employee exists
            if (employeeService.getEmployeeById(employeeId) == null) {
                response.put("success", false);
                response.put("message", "Employee not found");
                return response;
            }
            
            // Check if payment hold exists
            Optional<PaymentHold> existingHold = paymentHoldRepository.findByEmployeeId(employeeId);
            if (!existingHold.isPresent()) {
                response.put("success", false);
                response.put("message", "No payment hold found for this employee");
                return response;
            }
            
            // Remove payment hold
            paymentHoldRepository.deleteByEmployeeId(employeeId);
            
            response.put("success", true);
            response.put("message", "Payment hold removed successfully");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error removing payment hold: " + e.getMessage());
            e.printStackTrace();
        }
        
        return response;
    }
}