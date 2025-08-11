package com.payflow.payflow.Controller;

import com.payflow.payflow.Service.PaymentHoldService;
import com.payflow.payflow.model.PaymentHold;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST Controller for managing payment holds.
 * This controller provides endpoints for applying, removing, and retrieving payment holds.
 */
@RestController
@RequestMapping("/api")
public class PaymentHoldController {

    private final PaymentHoldService paymentHoldService;

    /**
     * Constructor for dependency injection.
     * 
     * @param paymentHoldService Service for payment hold operations.
     */
    @Autowired
    public PaymentHoldController(PaymentHoldService paymentHoldService) {
        this.paymentHoldService = paymentHoldService;
    }

    /**
     * Get all employees with payment holds.
     * 
     * @return A ResponseEntity containing a list of all payment holds.
     */
    @GetMapping("/employees/payment-hold")
    public ResponseEntity<?> getAllPaymentHolds() {
        try {
            System.out.println("Controller: Attempting to fetch all payment holds...");
            List<PaymentHold> paymentHolds = paymentHoldService.getAllPaymentHolds();
            System.out.println("Controller: Successfully fetched " + paymentHolds.size() + " payment holds");
            
            // Convert to safe response objects to avoid serialization issues
            List<Map<String, Object>> safePaymentHolds = paymentHolds.stream()
                .map(ph -> {
                    Map<String, Object> safeHold = new HashMap<>();
                    safeHold.put("id", ph.getId());
                    safeHold.put("employeeId", ph.getEmployeeId());
                    safeHold.put("reason", ph.getReason());
                    safeHold.put("appliedOn", ph.getAppliedOn());
                    safeHold.put("appliedBy", ph.getAppliedBy());
                    return safeHold;
                })
                .collect(Collectors.toList());
            
            return new ResponseEntity<>(safePaymentHolds, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Controller: Error fetching payment holds: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch payment holds");
            errorResponse.put("message", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Check if an employee has a payment hold.
     * 
     * @param employeeId The ID of the employee.
     * @return A ResponseEntity containing the payment hold details if found.
     */
    @GetMapping("/employees/{employeeId}/payment-hold")
    public ResponseEntity<?> getPaymentHoldByEmployeeId(@PathVariable Long employeeId) {
        try {
            System.out.println("Controller: Checking payment hold for employee: " + employeeId);
            Optional<PaymentHold> paymentHold = paymentHoldService.getPaymentHoldByEmployeeId(employeeId);
            
            if (paymentHold.isPresent()) {
                System.out.println("Controller: Payment hold found for employee: " + employeeId);
                // Create a safe response object to avoid serialization issues
                Map<String, Object> response = new HashMap<>();
                response.put("hasPaymentHold", true);
                response.put("id", paymentHold.get().getId());
                response.put("employeeId", paymentHold.get().getEmployeeId());
                response.put("reason", paymentHold.get().getReason());
                response.put("appliedOn", paymentHold.get().getAppliedOn());
                response.put("appliedBy", paymentHold.get().getAppliedBy());
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                System.out.println("Controller: No payment hold found for employee: " + employeeId);
                Map<String, Object> response = new HashMap<>();
                response.put("hasPaymentHold", false);
                response.put("message", "No payment hold found for this employee");
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        } catch (Exception e) {
            System.out.println("Controller: Error checking payment hold for employee " + employeeId + ": " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to check payment hold status");
            errorResponse.put("message", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Apply a payment hold for an employee.
     * 
     * @param employeeId The ID of the employee.
     * @param requestBody The request body containing the reason and appliedBy fields.
     * @return A ResponseEntity containing the result of the operation.
     */
    @PostMapping("/employees/{employeeId}/apply-payment-hold")
    public ResponseEntity<Map<String, Object>> applyPaymentHold(
            @PathVariable Long employeeId,
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            System.out.println("Controller: Applying payment hold for employee: " + employeeId);
            System.out.println("Controller: Request body: " + requestBody);
            
            String reason = (String) requestBody.get("reason");
            Long appliedBy = Long.valueOf(requestBody.get("appliedBy").toString());
            
            Map<String, Object> response = paymentHoldService.applyPaymentHold(employeeId, reason, appliedBy);
            
            System.out.println("Controller: Payment hold service response: " + response);
            
            HttpStatus status = (Boolean) response.get("success") ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
            return new ResponseEntity<>(response, status);
        } catch (Exception e) {
            System.out.println("Controller: Error applying payment hold for employee " + employeeId + ": " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to apply payment hold");
            errorResponse.put("message", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Remove a payment hold for an employee.
     * 
     * @param employeeId The ID of the employee.
     * @return A ResponseEntity containing the result of the operation.
     */
    @PostMapping("/employees/{employeeId}/remove-payment-hold")
    public ResponseEntity<Map<String, Object>> removePaymentHold(@PathVariable Long employeeId) {
        Map<String, Object> response = paymentHoldService.removePaymentHold(employeeId);
        
        HttpStatus status = (Boolean) response.get("success") ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(response, status);
    }
}