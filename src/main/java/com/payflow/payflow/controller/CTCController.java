package com.payflow.payflow.Controller;

import com.payflow.payflow.Service.CTCDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for managing all CTC (Cost to Company) related endpoints.
 * This includes adding new CTC records, updating existing ones,
 * and retrieving an employee's full CTC history.
 */
@RestController
@RequestMapping("/api/ctc")
public class CTCController {

    @Autowired
    private CTCDetailsService ctcDetailsService;

    /**
     * Endpoint to add a new CTC record for an employee.
     * @param payload A map containing the employeeId, totalCtc, and effectiveFrom date.
     * @return A ResponseEntity with a status and a map containing the success status and message.
     */
    @PostMapping("/")
    public ResponseEntity<Map<String, Object>> addCTCDetails(@RequestBody Map<String, Object> payload) {
        System.out.println("Received payload for POST /: " + payload);
        try {
            Map<String, Object> response = ctcDetailsService.addCTCDetails(payload);
            System.out.println("Response for POST /: " + response);
            HttpStatus status = (boolean) response.get("success") ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
            return new ResponseEntity<>(response, status);
        } catch (Exception e) {
            System.out.println("Error in POST /: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error adding CTC: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Alternative endpoint to add a new CTC record for an employee.
     * This endpoint is used by the frontend application.
     * @param payload A map containing the employeeId, basicSalary, hra, allowances, bonuses, pfContribution, gratuity, totalCTC, effectiveFrom date, and optionally effectiveTo date.
     * @return A ResponseEntity with a status and a map containing the success status and message.
     */
    @PutMapping("/add")
    public ResponseEntity<Map<String, Object>> addCTCDetailsAlternative(@RequestBody Map<String, Object> payload) {
        System.out.println("Received payload for /add: " + payload);
        try {
            Map<String, Object> response = ctcDetailsService.addCTCDetails(payload);
            System.out.println("Response for /add: " + response);
            HttpStatus status = (boolean) response.get("success") ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
            return new ResponseEntity<>(response, status);
        } catch (Exception e) {
            System.out.println("Error in /add: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error adding CTC: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Endpoint to update an existing CTC record for an employee.
     * @param ctcId The ID of the CTC record to update.
     * @param payload A map containing the updated totalCtc, effectiveFrom date, and optionally effectiveTo date.
     * @return A ResponseEntity with a status and a map containing the success status and message.
     */
    @PutMapping("/{ctcId}")
    public ResponseEntity<Map<String, Object>> updateCTCDetails(
            @PathVariable Long ctcId,
            @RequestBody Map<String, Object> payload) {

        System.out.println("Received payload for PUT /{ctcId}: " + payload + " with ctcId: " + ctcId);
        try {
            Map<String, Object> response = ctcDetailsService.updateCTCDetails(ctcId, payload);
            System.out.println("Response for PUT /{ctcId}: " + response);
            HttpStatus status = (boolean) response.get("success") ? HttpStatus.OK : HttpStatus.NOT_FOUND;
            return new ResponseEntity<>(response, status);
        } catch (Exception e) {
            System.out.println("Error in PUT /{ctcId}: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error updating CTC: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Alternative endpoint to update an existing CTC record for an employee.
     * This endpoint is used by the frontend application.
     * @param payload A map containing the ctcId, employeeId, basicSalary, hra, allowances, bonuses, pfContribution, gratuity, totalCTC, effectiveFrom date, and optionally effectiveTo date.
     * @return A ResponseEntity with a status and a map containing the success status and message.
     */
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateCTCDetailsAlternative(@RequestBody Map<String, Object> payload) {
        System.out.println("Received payload for /update: " + payload);
        try {
            Long ctcId = Long.parseLong(payload.get("ctcId").toString());
            Map<String, Object> response = ctcDetailsService.updateCTCDetails(ctcId, payload);
            System.out.println("Response for /update: " + response);
            HttpStatus status = (boolean) response.get("success") ? HttpStatus.OK : HttpStatus.NOT_FOUND;
            return new ResponseEntity<>(response, status);
        } catch (Exception e) {
            System.out.println("Error in /update: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error updating CTC: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Endpoint to retrieve the complete CTC history for an employee.
     * @param employeeId The ID of the employee.
     * @return A ResponseEntity with a status and a map containing the success status and a list of all CTC records.
     */
    @GetMapping("/employee/{employeeId}/history")
    public ResponseEntity<Map<String, Object>> getCTCHistory(@PathVariable Long employeeId) {
        Map<String, Object> response = ctcDetailsService.getCTCHistory(employeeId);
        HttpStatus status = (boolean) response.get("success") ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        return new ResponseEntity<>(response, status);
    }

    /**
     * Endpoint to retrieve the current CTC summary for an employee.
     * This returns the active CTC record for the current date (where current date is between effectiveFrom and effectiveTo).
     * If no active record is found, it returns the most recent CTC record.
     * @param employeeId The ID of the employee.
     * @return A ResponseEntity with a status and a map containing the success status and the current CTC record.
     */
    @GetMapping("/employee/{employeeId}/summary")
    public ResponseEntity<Map<String, Object>> getCurrentCTCSummary(@PathVariable Long employeeId) {
        Map<String, Object> response = ctcDetailsService.getCurrentCTCSummary(employeeId);
        HttpStatus status = (boolean) response.get("success") ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        return new ResponseEntity<>(response, status);
    }
}
