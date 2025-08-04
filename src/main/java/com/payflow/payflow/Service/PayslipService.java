package com.payflow.payflow.Service;

import com.payflow.payflow.model.CTCDetails;
import com.payflow.payflow.model.Employee;
import com.payflow.payflow.model.Payslip;
import com.payflow.payflow.repository.CTCDetailsRepository;
import com.payflow.payflow.repository.EmployeeRepository;
import com.payflow.payflow.repository.PayslipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

@Service
public class PayslipService {

    @Autowired
    private PayslipRepository payslipRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CTCDetailsRepository ctcDetailsRepository;

    // Month names for validation and display
    private static final List<String> VALID_MONTHS = Arrays.asList(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    );

    /**
     * Generate a payslip for an employee for a specific month and year
     */
    public Map<String, Object> generatePayslip(Map<String, Object> payload) {
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
            
            // Extract and validate month and year
            String month = (String) payload.get("month");
            Integer year = Integer.parseInt(payload.get("year").toString());
            
            if (!VALID_MONTHS.contains(month)) {
                response.put("success", false);
                response.put("message", "Invalid month. Must be one of: " + String.join(", ", VALID_MONTHS));
                return response;
            }
            
            // Check if payslip already exists for this month/year
            Optional<Payslip> existingPayslip = payslipRepository.findByEmployeeIdAndMonthAndYear(employeeId, month, year);
            if (existingPayslip.isPresent()) {
                response.put("success", false);
                response.put("message", "Payslip already exists for " + month + " " + year);
                return response;
            }
            
            // Get the applicable CTC for this month/year
            YearMonth yearMonth = YearMonth.of(year, VALID_MONTHS.indexOf(month) + 1);
            Optional<CTCDetails> ctcOpt = ctcDetailsRepository.findFirstByEmployeeIdOrderByEffectiveFromDesc(employeeId);
            
            if (!ctcOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "No CTC details found for this employee");
                return response;
            }
            
            CTCDetails ctc = ctcOpt.get();
            Employee employee = employeeOpt.get();
            
            // Calculate monthly salary (CTC / 12)
            BigDecimal monthlySalary = ctc.getTotalCTC().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
            
            // Calculate deductions (if any)
            BigDecimal deductions = BigDecimal.ZERO;
            
            // Apply salary deductions for excess leave days if applicable
            Integer salaryDeductionDays = employee.getSalaryDeductionDays();
            if (salaryDeductionDays != null && salaryDeductionDays > 0) {
                // Calculate daily salary (monthly salary / working days in month)
                int workingDays = yearMonth.lengthOfMonth() - 8; // Assuming 8 non-working days per month
                BigDecimal dailySalary = monthlySalary.divide(BigDecimal.valueOf(workingDays), 2, RoundingMode.HALF_UP);
                
                // Calculate deduction amount
                deductions = dailySalary.multiply(BigDecimal.valueOf(salaryDeductionDays));
                
                // Reset the deduction days counter after applying
                employee.setSalaryDeductionDays(0);
                employeeRepository.save(employee);
            }
            
            // Calculate net pay
            BigDecimal netPay = monthlySalary.subtract(deductions);
            
            // Create new payslip
            Payslip payslip = new Payslip();
            payslip.setEmployeeId(employeeId);
            payslip.setMonth(month);
            payslip.setYear(year);
            payslip.setNetPay(netPay);
            payslip.setDeductions(deductions);
            payslip.setGeneratedOn(Timestamp.valueOf(LocalDateTime.now()));
            
            // Generate download link (this would be replaced with actual PDF generation in a real system)
            String downloadLink = "/api/payslip/download-pdf/" + employeeId + "/" + month + "/" + year;
            payslip.setDownloadLink(downloadLink);
            
            // Save the payslip
            Payslip savedPayslip = payslipRepository.save(payslip);
            
            response.put("success", true);
            response.put("message", "Payslip generated successfully");
            response.put("data", savedPayslip);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error generating payslip: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Generate payslips in bulk for all employees for a specific month and year
     */
    public Map<String, Object> generateBulkPayslips(String month, Integer year) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> results = new ArrayList<>();
        
        try {
            // Validate month
            if (!VALID_MONTHS.contains(month)) {
                response.put("success", false);
                response.put("message", "Invalid month. Must be one of: " + String.join(", ", VALID_MONTHS));
                return response;
            }
            
            // Get all active employees
            List<Employee> employees = employeeRepository.findAll();
            int successCount = 0;
            int failCount = 0;
            
            for (Employee employee : employees) {
                // Skip inactive employees
                if (employee.getStatus() == null || !employee.getStatus().equalsIgnoreCase("ACTIVE")) {
                    continue;
                }
                
                // Generate payslip for each employee
                Map<String, Object> payload = new HashMap<>();
                payload.put("employeeId", employee.getId());
                payload.put("month", month);
                payload.put("year", year);
                
                Map<String, Object> result = generatePayslip(payload);
                result.put("employeeName", employee.getName());
                results.add(result);
                
                if ((Boolean) result.get("success")) {
                    successCount++;
                } else {
                    failCount++;
                }
            }
            
            response.put("success", true);
            response.put("message", "Bulk payslip generation completed. Success: " + successCount + ", Failed: " + failCount);
            response.put("results", results);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error generating bulk payslips: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Get all payslips for an employee
     */
    public Map<String, Object> getEmployeePayslips(Long employeeId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verify employee exists
            Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
            if (!employeeOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Employee not found");
                return response;
            }
            
            // Get payslips
            List<Payslip> payslips = payslipRepository.findByEmployeeIdOrderByYearDescMonthDesc(employeeId);
            
            response.put("success", true);
            response.put("data", payslips);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error retrieving payslips: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Get a specific payslip by employee, month and year
     */
    public Map<String, Object> getSpecificPayslip(Long employeeId, String month, Integer year) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate month
            if (!VALID_MONTHS.contains(month)) {
                response.put("success", false);
                response.put("message", "Invalid month. Must be one of: " + String.join(", ", VALID_MONTHS));
                return response;
            }
            
            // Find the payslip
            Optional<Payslip> payslipOpt = payslipRepository.findByEmployeeIdAndMonthAndYear(employeeId, month, year);
            
            if (payslipOpt.isPresent()) {
                response.put("success", true);
                response.put("data", payslipOpt.get());
            } else {
                response.put("success", false);
                response.put("message", "Payslip not found for " + month + " " + year);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error retrieving payslip: " + e.getMessage());
        }
        
        return response;
    }
}