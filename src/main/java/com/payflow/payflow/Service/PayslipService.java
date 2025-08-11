package com.payflow.payflow.Service;

import com.payflow.payflow.model.CTCDetails;
import com.payflow.payflow.model.Employee;
import com.payflow.payflow.model.Payslip;
import com.payflow.payflow.repository.CTCDetailsRepository;
import com.payflow.payflow.repository.EmployeeRepository;
import com.payflow.payflow.repository.PayslipRepository;
import com.payflow.payflow.Service.PaymentHoldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Service class for handling all business logic related to payslips.
 * This service provides methods for generating single or bulk payslips,
 * retrieving payslips for an employee, and fetching a specific payslip.
 */
@Service
public class PayslipService {

    @Autowired
    private PayslipRepository payslipRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CTCDetailsRepository ctcDetailsRepository;

    @Autowired
    private PaymentHoldService paymentHoldService;

    // Month names for validation and display
    private static final List<String> VALID_MONTHS = Arrays.asList(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    );

    /**
     * Generates a single payslip for a specified employee, month, and year.
     * The method calculates the monthly salary based on the employee's CTC
     * and applies any deductions for excess leave days.
     *
     * @param payload A Map containing the employeeId, month, and year.
     * @return A Map containing the success status, a message, and the generated payslip data.
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

            Employee employee = employeeOpt.get();

            // Check if employee has a payment hold
            if (paymentHoldService.hasPaymentHold(employeeId)) {
                response.put("success", false);
                response.put("message", "Cannot generate payslip: Employee has a payment hold");
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
            LocalDate payslipDate = yearMonth.atDay(1);
            
            // Fixed CTC lookup: Find CTC records that are effective for the entire month
            // We need to check if the CTC is effective for any part of the month
            LocalDate monthStart = yearMonth.atDay(1);
            LocalDate monthEnd = yearMonth.atEndOfMonth();
            
            List<CTCDetails> ctcRecords = ctcDetailsRepository.findActiveRecordsByEmployeeIdAndDateRange(
                employeeId, monthStart, monthEnd);
            
            if (ctcRecords.isEmpty()) {
                response.put("success", false);
                response.put("message", "No CTC details found for this employee for the specified period.");
                return response;
            }
            CTCDetails ctc = ctcRecords.get(0); // Get the most recent active record

            // Calculate monthly gross salary based on the individual CTC components
            BigDecimal monthlyGrossSalary = ctc.getBasicSalary()
                    .add(ctc.getHra())
                    .add(ctc.getAllowances())
                    .add(ctc.getBonuses())
                    .add(ctc.getPfContribution()); // PF is an employer contribution but is part of Gross in the provided example
                    // In a real scenario, this PF would not be included in the gross, but we're following the user's data.

            // Calculate deductions
            BigDecimal totalDeductions = BigDecimal.ZERO;
            
            // Employee contributions are actual deductions
            BigDecimal employeePfContribution = ctc.getPfContribution();
            BigDecimal employeeGratuity = ctc.getGratuity();
            
            BigDecimal leaveDeduction = BigDecimal.ZERO;

            // Handle excess leave deduction separately
            int leavesUsedThisMonth = employee.getLeavesUsedThisMonth() != null ? employee.getLeavesUsedThisMonth() : 0;
            int totalLeavesUsedThisYear = employee.getTotalLeavesUsedThisYear() != null ? employee.getTotalLeavesUsedThisYear() : 0;
            final int ANNUAL_LEAVE_LIMIT = 12;

            if (totalLeavesUsedThisYear > ANNUAL_LEAVE_LIMIT) {
                int excessLeaves = totalLeavesUsedThisYear - ANNUAL_LEAVE_LIMIT;

                // Correctly calculate the number of days in the month to get the daily rate
                YearMonth ym = YearMonth.of(year, VALID_MONTHS.indexOf(month) + 1);
                BigDecimal daysInMonth = BigDecimal.valueOf(ym.lengthOfMonth());
                BigDecimal dailySalary = monthlyGrossSalary.divide(daysInMonth, 2, RoundingMode.HALF_UP);
                leaveDeduction = dailySalary.multiply(BigDecimal.valueOf(excessLeaves));
            }

            // Sum up all deductions
            totalDeductions = employeePfContribution.add(employeeGratuity).add(leaveDeduction);

            // Calculate net pay
            BigDecimal netPay = monthlyGrossSalary.subtract(totalDeductions);

            // Create new payslip
            Payslip payslip = new Payslip();
            payslip.setEmployeeId(employeeId);
            payslip.setMonth(month);
            payslip.setYear(year);
            payslip.setNetPay(netPay);
            payslip.setDeductions(totalDeductions);
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
     * Generates payslips in bulk for all active employees for a specific month and year.
     * It iterates through the list of employees and calls the generatePayslip method for each.
     *
     * @param month The month for which to generate payslips.
     * @param year The year for which to generate payslips.
     * @return A Map containing the overall success status, a summary message, and a list of individual results.
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
     * Retrieves all payslips for a specific employee, sorted by year and month in descending order.
     *
     * @param employeeId The ID of the employee.
     * @return A Map containing the success status and a list of payslips for the employee.
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
     * Retrieves a specific payslip for an employee by month and year.
     *
     * @param employeeId The ID of the employee.
     * @param month The month of the payslip.
     * @param year The year of the payslip.
     * @return A Map containing the success status and the payslip data if found.
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

    /**
     * Helper method to count the number of working days (Monday-Friday) in a given month and year.
     * @param year The year.
     * @param month The month (1-12).
     * @return The number of working days.
     */
    private long countWorkingDays(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();

        return startOfMonth.datesUntil(endOfMonth.plusDays(1))
                .filter(date -> date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY)
                .count();
    }
}