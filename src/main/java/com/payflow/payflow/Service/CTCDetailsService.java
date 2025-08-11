package com.payflow.payflow.Service;

import com.payflow.payflow.model.CTCDetails;
import com.payflow.payflow.model.Employee;
import com.payflow.payflow.repository.CTCDetailsRepository;
import com.payflow.payflow.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for managing CTC (Cost to Company) details for employees.
 * It provides methods to add, update, and retrieve CTC records.
 */
@Service
public class CTCDetailsService {

    @Autowired
    private CTCDetailsRepository ctcDetailsRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * Helper method to safely parse BigDecimal from payload.
     * Handles nulls and ensures correct type conversion.
     */
    private BigDecimal parseBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                System.err.println("Warning: Could not parse BigDecimal from string: " + value);
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * Add a new CTC record for an employee.
     * When a new record is added, the previous active record's effectiveTo date is automatically set.
     */
    public Map<String, Object> addCTCDetails(Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();

        try {
            Object employeeIdObj = payload.get("employeeId");
            if (employeeIdObj == null) {
                response.put("success", false);
                response.put("message", "Employee ID is required.");
                return response;
            }
            Long employeeId;
            if (employeeIdObj instanceof Number) {
                employeeId = ((Number) employeeIdObj).longValue();
            } else if (employeeIdObj instanceof String) {
                employeeId = Long.parseLong((String) employeeIdObj);
            } else {
                response.put("success", false);
                response.put("message", "Employee ID must be a number or number string.");
                return response;
            }

            Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
            if (!employeeOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Employee not found");
                return response;
            }

            Object effectiveFromObj = payload.get("effectiveFrom");
            if (effectiveFromObj == null || !(effectiveFromObj instanceof String)) {
                response.put("success", false);
                response.put("message", "Effective from date is required and must be a date string (YYYY-MM-DD)");
                return response;
            }
            LocalDate effectiveFrom = LocalDate.parse((String) effectiveFromObj);

            // Find the latest CTC record for this employee
            Optional<CTCDetails> latestCTC = ctcDetailsRepository.findFirstByEmployeeIdOrderByEffectiveFromDesc(employeeId);
            if (latestCTC.isPresent()) {
                CTCDetails oldCTC = latestCTC.get();
                if (effectiveFrom.isAfter(oldCTC.getEffectiveFrom())) {
                    // Corrected Logic: Set the effectiveTo date for the previous record
                    oldCTC.setEffectiveTo(effectiveFrom.minusDays(1));
                    ctcDetailsRepository.save(oldCTC);
                } else {
                    response.put("success", false);
                    response.put("message", "New effective from date must be after the current active CTC date.");
                    return response;
                }
            }

            // Create new CTC record with effectiveTo as null
            CTCDetails newCtcDetails = new CTCDetails();
            newCtcDetails.setEmployeeId(employeeId);
            newCtcDetails.setEffectiveFrom(effectiveFrom);
            newCtcDetails.setEffectiveTo(null); // The new record is the current one, so effectiveTo is null

            // Set individual CTC components
            newCtcDetails.setBasicSalary(parseBigDecimal(payload.get("basicSalary")));
            newCtcDetails.setHra(parseBigDecimal(payload.get("hra")));
            newCtcDetails.setAllowances(parseBigDecimal(payload.get("allowances")));
            newCtcDetails.setBonuses(parseBigDecimal(payload.get("bonuses")));
            newCtcDetails.setPfContribution(parseBigDecimal(payload.get("pfContribution")));
            newCtcDetails.setGratuity(parseBigDecimal(payload.get("gratuity")));
            newCtcDetails.setDa(parseBigDecimal(payload.get("da")));
            newCtcDetails.setSpecialAllowance(parseBigDecimal(payload.get("specialAllowance")));

            // Calculate totalCTC if not provided
            BigDecimal providedTotalCTC = parseBigDecimal(payload.get("totalCTC"));
            if (providedTotalCTC.compareTo(BigDecimal.ZERO) == 0) {
                BigDecimal calculatedTotalCTC = newCtcDetails.getBasicSalary()
                        .add(newCtcDetails.getHra())
                        .add(newCtcDetails.getAllowances())
                        .add(newCtcDetails.getBonuses())
                        .add(newCtcDetails.getPfContribution())
                        .add(newCtcDetails.getGratuity())
                        .add(newCtcDetails.getDa())
                        .add(newCtcDetails.getSpecialAllowance());
                newCtcDetails.setTotalCTC(calculatedTotalCTC);
            } else {
                newCtcDetails.setTotalCTC(providedTotalCTC);
            }

            newCtcDetails.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
            CTCDetails savedCtc = ctcDetailsRepository.save(newCtcDetails);

            response.put("success", true);
            response.put("message", "CTC details added successfully");
            response.put("data", savedCtc);

        } catch (NumberFormatException e) {
            response.put("success", false);
            response.put("message", "Invalid number format for a field: " + e.getMessage());
        } catch (DateTimeParseException e) {
            response.put("success", false);
            response.put("message", "Invalid date format for 'effectiveFrom'. Please use YYYY-MM-DD: " + e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error adding CTC details: " + e.getMessage());
            e.printStackTrace();
        }

        return response;
    }

    /**
     * Update an existing CTC record.
     */
    public Map<String, Object> updateCTCDetails(Long ctcId, Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<CTCDetails> ctcDetailsOpt = ctcDetailsRepository.findById(ctcId);
            if (ctcDetailsOpt.isPresent()) {
                CTCDetails ctcDetails = ctcDetailsOpt.get();

                // Handle effectiveFrom date update
                if (payload.containsKey("effectiveFrom")) {
                    Object effectiveFromObj = payload.get("effectiveFrom");
                    if (effectiveFromObj instanceof String) {
                        ctcDetails.setEffectiveFrom(LocalDate.parse((String) effectiveFromObj));
                    } else {
                        response.put("success", false);
                        response.put("message", "Invalid format for 'effectiveFrom'. Must be a date string (YYYY-MM-DD).");
                        return response;
                    }
                }
                
                // Handle effectiveTo date update
                if (payload.containsKey("effectiveTo")) {
                    Object effectiveToObj = payload.get("effectiveTo");
                    if (effectiveToObj instanceof String && !((String) effectiveToObj).isEmpty()) {
                        LocalDate effectiveTo = LocalDate.parse((String) effectiveToObj);
                        if (effectiveTo.isBefore(ctcDetails.getEffectiveFrom())) {
                            response.put("success", false);
                            response.put("message", "Effective To date must be after Effective From date.");
                            return response;
                        }
                        ctcDetails.setEffectiveTo(effectiveTo);
                    } else if (effectiveToObj == null || (effectiveToObj instanceof String && ((String) effectiveToObj).isEmpty())) {
                        ctcDetails.setEffectiveTo(null);
                    }
                }

                // Update individual CTC components
                if (payload.containsKey("basicSalary")) {
                    ctcDetails.setBasicSalary(parseBigDecimal(payload.get("basicSalary")));
                }
                if (payload.containsKey("hra")) {
                    ctcDetails.setHra(parseBigDecimal(payload.get("hra")));
                }
                if (payload.containsKey("allowances")) {
                    ctcDetails.setAllowances(parseBigDecimal(payload.get("allowances")));
                }
                if (payload.containsKey("bonuses")) {
                    ctcDetails.setBonuses(parseBigDecimal(payload.get("bonuses")));
                }
                if (payload.containsKey("pfContribution")) {
                    ctcDetails.setPfContribution(parseBigDecimal(payload.get("pfContribution")));
                }
                if (payload.containsKey("gratuity")) {
                    ctcDetails.setGratuity(parseBigDecimal(payload.get("gratuity")));
                }
                if (payload.containsKey("da")) {
                    ctcDetails.setDa(parseBigDecimal(payload.get("da")));
                }
                if (payload.containsKey("specialAllowance")) {
                    ctcDetails.setSpecialAllowance(parseBigDecimal(payload.get("specialAllowance")));
                }
                
                // Recalculate totalCTC if not provided
                if (payload.containsKey("totalCTC")) {
                    BigDecimal providedTotalCTC = parseBigDecimal(payload.get("totalCTC"));
                    ctcDetails.setTotalCTC(providedTotalCTC);
                } else {
                    BigDecimal calculatedTotalCTC = ctcDetails.getBasicSalary()
                        .add(ctcDetails.getHra())
                        .add(ctcDetails.getAllowances())
                        .add(ctcDetails.getBonuses())
                        .add(ctcDetails.getPfContribution())
                        .add(ctcDetails.getGratuity())
                        .add(ctcDetails.getDa() != null ? ctcDetails.getDa() : BigDecimal.ZERO)
                        .add(ctcDetails.getSpecialAllowance() != null ? ctcDetails.getSpecialAllowance() : BigDecimal.ZERO);
                    ctcDetails.setTotalCTC(calculatedTotalCTC);
                }
                
                CTCDetails updatedCtc = ctcDetailsRepository.save(ctcDetails);
                
                response.put("success", true);
                response.put("message", "CTC details updated successfully");
                response.put("data", updatedCtc);
            } else {
                response.put("success", false);
                response.put("message", "CTC record not found");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating CTC details: " + e.getMessage());
            e.printStackTrace();
        }

        return response;
    }

    /**
     * Delete a CTC record.
     */
    public Map<String, Object> deleteCTCDetails(Long ctcId) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (ctcDetailsRepository.existsById(ctcId)) {
                ctcDetailsRepository.deleteById(ctcId);
                response.put("success", true);
                response.put("message", "CTC record deleted successfully");
            } else {
                response.put("success", false);
                response.put("message", "CTC record not found");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting CTC record: " + e.getMessage());
        }
        return response;
    }

    /**
     * Get the full CTC history for an employee
     */
    public Map<String, Object> getCTCHistory(Long employeeId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
            if (!employeeOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Employee not found");
                return response;
            }

            List<CTCDetails> ctcRecords = ctcDetailsRepository.findByEmployeeIdOrderByEffectiveFromDesc(employeeId);

            if (ctcRecords.isEmpty()) {
                response.put("success", false);
                response.put("message", "No CTC history found for this employee");
            } else {
                response.put("success", true);
                response.put("data", ctcRecords);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error retrieving CTC history: " + e.getMessage());
        }

        return response;
    }
    
    /**
     * Get current CTC summary for an employee
     */
    public Map<String, Object> getCurrentCTCSummary(Long employeeId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
            if (!employeeOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Employee not found");
                return response;
            }

            Optional<CTCDetails> currentCTC = ctcDetailsRepository.findFirstByEmployeeIdOrderByEffectiveFromDesc(employeeId);

            if (currentCTC.isPresent()) {
                response.put("success", true);
                response.put("data", currentCTC.get());
            } else {
                response.put("success", false);
                response.put("message", "No CTC records found for this employee");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error retrieving current CTC: " + e.getMessage());
        }

        return response;
    }
}