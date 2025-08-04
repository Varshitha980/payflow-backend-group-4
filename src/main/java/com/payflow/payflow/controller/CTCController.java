package com.payflow.payflow.controller;

import com.payflow.payflow.Service.CTCDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ctc")
@CrossOrigin(origins = "http://localhost:3000")
public class CTCController {

    @Autowired
    private CTCDetailsService ctcDetailsService;

    /**
     * Add a new CTC record for an employee
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addCTCDetails(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = ctcDetailsService.addCTCDetails(payload);
        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing CTC record
     */
    @PutMapping("/{ctcId}")
    public ResponseEntity<Map<String, Object>> updateCTCDetails(
            @PathVariable Long ctcId,
            @RequestBody Map<String, Object> payload) {
        Map<String, Object> response = ctcDetailsService.updateCTCDetails(ctcId, payload);
        return ResponseEntity.ok(response);
    }

    /**
     * Get CTC history for an employee
     */
    @GetMapping("/employee/{employeeId}/history")
    public ResponseEntity<Map<String, Object>> getCTCHistory(@PathVariable Long employeeId) {
        Map<String, Object> response = ctcDetailsService.getCTCHistory(employeeId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get current CTC summary for an employee
     */
    @GetMapping("/employee/{employeeId}/summary")
    public ResponseEntity<Map<String, Object>> getCurrentCTCSummary(@PathVariable Long employeeId) {
        Map<String, Object> response = ctcDetailsService.getCurrentCTCSummary(employeeId);
        return ResponseEntity.ok(response);
    }
}