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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CTCDetailsService {

    @Autowired
    private CTCDetailsRepository ctcDetailsRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * Add a new CTC record for an employee
     */
    public Map<String, Object> addCTCDetails(Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Extract and validate employee ID
            Long employeeId = Long.parseLong(payload.get("employeeId").toString());
            Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
            
            if (!employeeOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Employee not found");
                return response;
            }
            
            // Create new CTC record
            CTCDetails ctcDetails = new CTCDetails();
            ctcDetails.setEmployeeId(employeeId);
            
            // Parse effective date
            String effectiveFromStr = (String) payload.get("effectiveFrom");
            LocalDate effectiveFrom = LocalDate.parse(effectiveFromStr);
            ctcDetails.setEffectiveFrom(effectiveFrom);
            
            // Parse salary components
            ctcDetails.setBasicSalary(new BigDecimal(payload.get("basicSalary").toString()));
            ctcDetails.setHra(new BigDecimal(payload.get("hra").toString()));
            
            // Optional components
            if (payload.containsKey("allowances")) {
                ctcDetails.setAllowances(new BigDecimal(payload.get("allowances").toString()));
            } else {
                ctcDetails.setAllowances(BigDecimal.ZERO);
            }
            
            if (payload.containsKey("bonuses")) {
                ctcDetails.setBonuses(new BigDecimal(payload.get("bonuses").toString()));
            } else {
                ctcDetails.setBonuses(BigDecimal.ZERO);
            }
            
            if (payload.containsKey("pfContribution")) {
                ctcDetails.setPfContribution(new BigDecimal(payload.get("pfContribution").toString()));
            } else {
                ctcDetails.setPfContribution(BigDecimal.ZERO);
            }
            
            if (payload.containsKey("gratuity")) {
                ctcDetails.setGratuity(new BigDecimal(payload.get("gratuity").toString()));
            } else {
                ctcDetails.setGratuity(BigDecimal.ZERO);
            }
            
            if (payload.containsKey("da")) {
                ctcDetails.setDa(new BigDecimal(payload.get("da").toString()));
            }
            
            if (payload.containsKey("specialAllowance")) {
                ctcDetails.setSpecialAllowance(new BigDecimal(payload.get("specialAllowance").toString()));
            }
            
            // Calculate total CTC
            BigDecimal totalCTC = ctcDetails.getBasicSalary().add(ctcDetails.getHra());
            if (ctcDetails.getAllowances() != null) totalCTC = totalCTC.add(ctcDetails.getAllowances());
            if (ctcDetails.getBonuses() != null) totalCTC = totalCTC.add(ctcDetails.getBonuses());
            if (ctcDetails.getPfContribution() != null) totalCTC = totalCTC.add(ctcDetails.getPfContribution());
            if (ctcDetails.getGratuity() != null) totalCTC = totalCTC.add(ctcDetails.getGratuity());
            if (ctcDetails.getDa() != null) totalCTC = totalCTC.add(ctcDetails.getDa());
            if (ctcDetails.getSpecialAllowance() != null) totalCTC = totalCTC.add(ctcDetails.getSpecialAllowance());
            
            ctcDetails.setTotalCTC(totalCTC);
            ctcDetails.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
            
            // Save the CTC record
            CTCDetails savedCTC = ctcDetailsRepository.save(ctcDetails);
            
            // Update employee's monthly salary
            Employee employee = employeeOpt.get();
            employee.setMonthlySalary(totalCTC.doubleValue() / 12);
            employeeRepository.save(employee);
            
            response.put("success", true);
            response.put("message", "CTC details added successfully");
            response.put("data", savedCTC);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error adding CTC details: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Update an existing CTC record
     */
    public Map<String, Object> updateCTCDetails(Long ctcId, Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<CTCDetails> ctcDetailsOpt = ctcDetailsRepository.findById(ctcId);
            
            if (!ctcDetailsOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "CTC record not found");
                return response;
            }
            
            CTCDetails ctcDetails = ctcDetailsOpt.get();
            
            // Update fields if provided
            if (payload.containsKey("effectiveFrom")) {
                String effectiveFromStr = (String) payload.get("effectiveFrom");
                LocalDate effectiveFrom = LocalDate.parse(effectiveFromStr);
                ctcDetails.setEffectiveFrom(effectiveFrom);
            }
            
            if (payload.containsKey("basicSalary")) {
                ctcDetails.setBasicSalary(new BigDecimal(payload.get("basicSalary").toString()));
            }
            
            if (payload.containsKey("hra")) {
                ctcDetails.setHra(new BigDecimal(payload.get("hra").toString()));
            }
            
            if (payload.containsKey("allowances")) {
                ctcDetails.setAllowances(new BigDecimal(payload.get("allowances").toString()));
            }
            
            if (payload.containsKey("bonuses")) {
                ctcDetails.setBonuses(new BigDecimal(payload.get("bonuses").toString()));
            }
            
            if (payload.containsKey("pfContribution")) {
                ctcDetails.setPfContribution(new BigDecimal(payload.get("pfContribution").toString()));
            }
            
            if (payload.containsKey("gratuity")) {
                ctcDetails.setGratuity(new BigDecimal(payload.get("gratuity").toString()));
            }
            
            if (payload.containsKey("da")) {
                ctcDetails.setDa(new BigDecimal(payload.get("da").toString()));
            }
            
            if (payload.containsKey("specialAllowance")) {
                ctcDetails.setSpecialAllowance(new BigDecimal(payload.get("specialAllowance").toString()));
            }
            
            // Recalculate total CTC
            BigDecimal totalCTC = ctcDetails.getBasicSalary().add(ctcDetails.getHra());
            if (ctcDetails.getAllowances() != null) totalCTC = totalCTC.add(ctcDetails.getAllowances());
            if (ctcDetails.getBonuses() != null) totalCTC = totalCTC.add(ctcDetails.getBonuses());
            if (ctcDetails.getPfContribution() != null) totalCTC = totalCTC.add(ctcDetails.getPfContribution());
            if (ctcDetails.getGratuity() != null) totalCTC = totalCTC.add(ctcDetails.getGratuity());
            if (ctcDetails.getDa() != null) totalCTC = totalCTC.add(ctcDetails.getDa());
            if (ctcDetails.getSpecialAllowance() != null) totalCTC = totalCTC.add(ctcDetails.getSpecialAllowance());
            
            ctcDetails.setTotalCTC(totalCTC);
            
            // Save the updated CTC record
            CTCDetails savedCTC = ctcDetailsRepository.save(ctcDetails);
            
            // Update employee's monthly salary if this is the most recent CTC
            Optional<CTCDetails> latestCTC = ctcDetailsRepository.findFirstByEmployeeIdOrderByEffectiveFromDesc(ctcDetails.getEmployeeId());
            if (latestCTC.isPresent() && latestCTC.get().getCtcId().equals(ctcId)) {
                Optional<Employee> employeeOpt = employeeRepository.findById(ctcDetails.getEmployeeId());
                if (employeeOpt.isPresent()) {
                    Employee employee = employeeOpt.get();
                    employee.setMonthlySalary(totalCTC.doubleValue() / 12);
                    employeeRepository.save(employee);
                }
            }
            
            response.put("success", true);
            response.put("message", "CTC details updated successfully");
            response.put("data", savedCTC);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating CTC details: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Get CTC history for an employee
     */
    public Map<String, Object> getCTCHistory(Long employeeId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verify employee exists
            Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
            if (!employeeOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Employee not found");
                return response;
            }
            
            // Get CTC history
            List<CTCDetails> ctcHistory = ctcDetailsRepository.findByEmployeeIdOrderByEffectiveFromDesc(employeeId);
            
            response.put("success", true);
            response.put("data", ctcHistory);
            
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
            // Verify employee exists
            Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
            if (!employeeOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Employee not found");
                return response;
            }
            
            // Get most recent CTC record
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