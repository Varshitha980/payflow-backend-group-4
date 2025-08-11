package com.payflow.payflow.Controller;

import com.payflow.payflow.Service.PayslipService;
import com.payflow.payflow.model.CTCDetails;
import com.payflow.payflow.model.Employee;
import com.payflow.payflow.model.Payslip;
import com.payflow.payflow.repository.CTCDetailsRepository;
import com.payflow.payflow.repository.EmployeeRepository;
import com.payflow.payflow.repository.PayslipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payslip")
@CrossOrigin(origins = "http://localhost:3000")
public class PayslipController {

    @Autowired
    private PayslipService payslipService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CTCDetailsRepository ctcDetailsRepository;

    @Autowired
    private PayslipRepository payslipRepository;

    /**
     * Endpoint to generate a payslip for a specific employee based on the provided payload.
     * @param payload A Map containing the necessary details (e.g., employeeId, month, year).
     * @return A ResponseEntity with a status and a map containing the success status and the generated payslip data.
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generatePayslip(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = payslipService.generatePayslip(payload);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to generate payslips in bulk for all employees for a given month and year.
     * @param month The month for which to generate payslips (e.g., "January").
     * @param year The year for which to generate payslips (e.g., 2025).
     * @return A ResponseEntity with a status and a map containing the success status and a summary of the bulk generation.
     */
    @PostMapping("/generate-bulk")
    public ResponseEntity<Map<String, Object>> generateBulkPayslips(
            @RequestParam String month,
            @RequestParam Integer year) {
        Map<String, Object> response = payslipService.generateBulkPayslips(month, year);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint to generate payslips for all employees for a given month and year.
     * This endpoint is used by the frontend for bulk generation.
     * @param payload A Map containing the month and year for which to generate payslips.
     * @return A ResponseEntity with a status and a map containing the success status and a summary of the bulk generation.
     */
    @PostMapping("/generate-all")
    public ResponseEntity<Map<String, Object>> generateAllPayslips(@RequestBody Map<String, Object> payload) {
        String month = (String) payload.get("month");
        Integer year = Integer.parseInt(payload.get("year").toString());
        Map<String, Object> response = payslipService.generateBulkPayslips(month, year);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint to manually trigger the automatic payslip generation process.
     * This endpoint is primarily for testing purposes and can be used by administrators
     * to manually trigger the generation process for the previous month.
     * @return A ResponseEntity with a status and a message indicating the result of the operation.
     */
    @PostMapping("/generate-automatic")
    public ResponseEntity<Map<String, Object>> triggerAutomaticPayslipGeneration() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Get the previous month and year
            LocalDate currentDate = LocalDate.now();
            LocalDate previousMonth = currentDate.minusMonths(1);
            String month = previousMonth.getMonth().toString();
            int year = previousMonth.getYear();
            
            // Convert month to title case (e.g., "JANUARY" to "January")
            month = month.substring(0, 1) + month.substring(1).toLowerCase();
            
            // Generate payslips for all employees for the previous month
            Map<String, Object> result = payslipService.generateBulkPayslips(month, year);
            
            response.put("success", result.get("success"));
            response.put("message", "Manual trigger for " + month + " " + year + ": " + result.get("message"));
            response.put("data", result.get("results"));
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error triggering automatic payslip generation: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to retrieve all payslips for a given employee.
     * @param employeeId The ID of the employee.
     * @return A ResponseEntity with a status and a map containing the success status and a list of all payslips for the employee.
     */
    @GetMapping("/employee/{employeeId}/all")
    public ResponseEntity<Map<String, Object>> getEmployeePayslips(@PathVariable Long employeeId) {
        Map<String, Object> response = payslipService.getEmployeePayslips(employeeId);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to retrieve a specific payslip for an employee based on the month and year.
     * @param employeeId The ID of the employee.
     * @param month The month of the payslip.
     * @param year The year of the payslip.
     * @return A ResponseEntity with a status and a map containing the success status and the payslip data if found.
     */
    @GetMapping("/employee/{employeeId}/{month}/{year}")
    public ResponseEntity<Map<String, Object>> getSpecificPayslip(
            @PathVariable Long employeeId,
            @PathVariable String month,
            @PathVariable Integer year) {
        Map<String, Object> response = payslipService.getSpecificPayslip(employeeId, month, year);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to download a specific payslip as a PDF.
     * This endpoint retrieves the payslip data, generates a placeholder PDF, and sends it as a downloadable file.
     * In a production environment, a dedicated PDF generation library would be used here.
     * @param employeeId The ID of the employee.
     * @param month The month of the payslip.
     * @param year The year of the payslip.
     * @return A ResponseEntity containing the PDF file as a byte array, or a 404/500 error if not found or an error occurs.
     */
    @GetMapping("/download-pdf/{employeeId}/{month}/{year}")
    public ResponseEntity<byte[]> downloadPayslipPdf(
            @PathVariable Long employeeId,
            @PathVariable String month,
            @PathVariable Integer year) {
        try {
            // Verify payslip exists
            Optional<Payslip> payslipOpt = payslipRepository.findByEmployeeIdAndMonthAndYear(employeeId, month, year);
            if (!payslipOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            // Get employee details
            Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
            if (!employeeOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Employee employee = employeeOpt.get();
            Payslip payslip = payslipOpt.get();

            // Get CTC details
            Optional<CTCDetails> ctcOpt = ctcDetailsRepository.findFirstByEmployeeIdOrderByEffectiveFromDesc(employeeId);
            if (!ctcOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            CTCDetails ctc = ctcOpt.get();

            // Create a simple PDF content (in a real system, use a PDF library)
            // This is a placeholder for demonstration purposes
            String pdfContent = generatePayslipContent(employee, payslip, ctc, month, year);
            byte[] pdfBytes = pdfContent.getBytes();

            // Set up response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "payslip_" + employee.getName() + "_" + month + "_" + year + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Helper method to generate placeholder text content for the payslip PDF.
     * This method formats the employee and payslip data into a readable string.
     * @param employee The employee object.
     * @param payslip The payslip object.
     * @param ctc The current CTC details.
     * @param month The month of the payslip.
     * @param year The year of the payslip.
     * @return A formatted String representing the payslip content.
     */
    private String generatePayslipContent(Employee employee, Payslip payslip, CTCDetails ctc, String month, Integer year) {
        StringBuilder content = new StringBuilder();

        // Company header
        content.append("PAYFLOW\n");
        content.append("PAYSLIP FOR " + month.toUpperCase() + " " + year + "\n\n");

        // Employee details
        content.append("Employee Name: " + employee.getName() + "\n");
        content.append("Employee ID: " + employee.getId() + "\n\n");

        // Calculate monthly components
        BigDecimal divisor = BigDecimal.valueOf(12);
        BigDecimal basicSalary = ctc.getBasicSalary().divide(divisor, 2, RoundingMode.HALF_UP);
        BigDecimal hra = ctc.getHra().divide(divisor, 2, RoundingMode.HALF_UP);

        // Earnings
        content.append("EARNINGS:\n");
        content.append("Basic Salary: " + basicSalary + "\n");
        content.append("HRA: " + hra + "\n");

        if (ctc.getDa() != null) {
            BigDecimal da = ctc.getDa().divide(divisor, 2, RoundingMode.HALF_UP);
            content.append("DA: " + da + "\n");
        }

        if (ctc.getSpecialAllowance() != null) {
            BigDecimal specialAllowance = ctc.getSpecialAllowance().divide(divisor, 2, RoundingMode.HALF_UP);
            content.append("Special Allowance: " + specialAllowance + "\n");
        }

        if (ctc.getAllowances() != null && ctc.getAllowances().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal allowances = ctc.getAllowances().divide(divisor, 2, RoundingMode.HALF_UP);
            content.append("Other Allowances: " + allowances + "\n");
        }

        // Calculate gross pay (monthly CTC)
        BigDecimal grossPay = ctc.getTotalCTC().divide(divisor, 2, RoundingMode.HALF_UP);
        content.append("Gross Pay: " + grossPay + "\n\n");

        // Deductions
        content.append("DEDUCTIONS:\n");

        if (ctc.getPfContribution() != null && ctc.getPfContribution().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal pf = ctc.getPfContribution().divide(divisor, 2, RoundingMode.HALF_UP);
            content.append("PF Contribution: " + pf + "\n");
        }

        // Leave deductions
        content.append("Leave Deductions: " + payslip.getDeductions() + "\n");

        // Net pay
        content.append("\nNET PAY: " + payslip.getNetPay() + "\n\n");

        // Footer
        content.append("This is a computer-generated payslip and does not require a signature.\n");

        return content.toString();
    }
}
