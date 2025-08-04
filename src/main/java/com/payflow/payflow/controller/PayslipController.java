package com.payflow.payflow.controller;

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
     * Generate a payslip for an employee
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generatePayslip(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = payslipService.generatePayslip(payload);
        return ResponseEntity.ok(response);
    }

    /**
     * Generate payslips in bulk for all employees
     */
    @PostMapping("/generate-bulk")
    public ResponseEntity<Map<String, Object>> generateBulkPayslips(
            @RequestParam String month,
            @RequestParam Integer year) {
        Map<String, Object> response = payslipService.generateBulkPayslips(month, year);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all payslips for an employee
     */
    @GetMapping("/employee/{employeeId}/all")
    public ResponseEntity<Map<String, Object>> getEmployeePayslips(@PathVariable Long employeeId) {
        Map<String, Object> response = payslipService.getEmployeePayslips(employeeId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific payslip by employee, month and year
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
     * Download a payslip as PDF
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
     * Generate payslip content (placeholder for PDF generation)
     */
    private String generatePayslipContent(Employee employee, Payslip payslip, CTCDetails ctc, String month, Integer year) {
        StringBuilder content = new StringBuilder();
        
        // Company header
        content.append("PAYFLOW COMPANY\n");
        content.append("PAYSLIP FOR " + month.toUpperCase() + " " + year + "\n\n");
        
        // Employee details
        content.append("Employee Name: " + employee.getName() + "\n");
        content.append("Employee ID: " + employee.getId() + "\n");
        content.append("Position: " + employee.getPosition() + "\n\n");
        
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