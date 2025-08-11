package com.payflow.payflow.Service;

import com.payflow.payflow.model.CTCDetails;
import com.payflow.payflow.model.Employee;
import com.payflow.payflow.Entity.LeaveRequest;
import com.payflow.payflow.model.Payslip;
import com.payflow.payflow.repository.CTCDetailsRepository;
import com.payflow.payflow.repository.EmployeeRepository;
import com.payflow.payflow.repository.LeaveRequestRepository;
import com.payflow.payflow.repository.PayslipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Service class responsible for generating and managing payroll for all employees.
 * This service calculates monthly payslips based on an employee's CTC, approved leaves,
 * and handles deductions for unpaid leave. It also supports regenerating payroll for a
 * specific period.
 */
@Service
public class PayrollService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CTCDetailsRepository ctcDetailsRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private PayslipRepository payslipRepository;

    /**
     * Generates or regenerates payslips for all employees for a specified month and year.
     * The process involves:
     * 1. Iterating through all active employees.
     * 2. Finding the most recent CTC record for each employee.
     * 3. Summing up approved leave days for the payroll month.
     * 4. Calculating deductions for any unpaid leave (when approved leave exceeds the employee's balance).
     * 5. Creating and saving a new Payslip entry with the calculated net pay and deductions.
     *
     * @param month The month for which payroll is to be generated (e.g., "January").
     * @param year The year for which payroll is to be generated (e.g., 2025).
     * @param regenerate A boolean flag. If true, all existing payslips for the specified
     * month and year are deleted before new ones are generated. If false,
     * payslips are only generated for employees who do not already have one.
     */
    @Transactional
    public void generatePayroll(String month, int year, boolean regenerate) {
        System.out.println("Initiating payroll generation for month: " + month + ", year: " + year);

        // Format the month and year to match your database format
        String formattedMonth = String.format("%s-%d", month, year);

        if (regenerate) {
            System.out.println("Regenerate flag is true. Deleting existing payslips for " + formattedMonth);
            payslipRepository.deleteByMonthAndYear(month, year);
        }

        List<Employee> employees = employeeRepository.findAll();
        YearMonth yearMonth = YearMonth.of(year, getMonthNumber(month));
        int totalWorkingDays = yearMonth.lengthOfMonth();

        for (Employee emp : employees) {
            if (!regenerate && payslipRepository.findByEmployeeIdAndMonthAndYear(emp.getId(), month, year).isPresent()) {
                System.out.println("Payslip for employee " + emp.getId() + " already exists for " + formattedMonth + ". Skipping.");
                continue;
            }

            // 1. Get CTC from the ctc_details table
            // This logic finds the most recent CTC record that was effective before the payroll month
            Optional<CTCDetails> ctcDetailsOpt = ctcDetailsRepository.findByEmployeeIdAndEffectiveFromBeforeOrderByEffectiveFromDesc(
                    emp.getId(), LocalDate.of(year, getMonthNumber(month), 1)).stream().findFirst();

            if (ctcDetailsOpt.isEmpty()) {
                System.out.println("⚠️ Skipping employee " + emp.getId() + " - No CTC details found.");
                continue;
            }

            BigDecimal annualCtc = ctcDetailsOpt.get().getTotalCTC();
            BigDecimal monthlySalary = annualCtc.divide(BigDecimal.valueOf(12), 2, BigDecimal.ROUND_HALF_UP);

            // 2. Sum approved leaves from leave_request table for the given month
            List<LeaveRequest> approvedLeaves = leaveRequestRepository.findByEmployeeIdAndStatusAndStartDateBetween(
                    emp.getId(), "Approved", yearMonth.atDay(1), yearMonth.atEndOfMonth());

            int approvedLeaveDays = approvedLeaves.stream().mapToInt(LeaveRequest::getDays).sum();
            int unpaidLeaves = Math.max(0, approvedLeaveDays - emp.getLeaveBalance());

            // 3. Calculate leave deduction
            BigDecimal leaveDeduction = BigDecimal.ZERO;
            if (unpaidLeaves > 0) {
                BigDecimal perDaySalary = monthlySalary.divide(BigDecimal.valueOf(totalWorkingDays), 2, BigDecimal.ROUND_HALF_UP);
                leaveDeduction = perDaySalary.multiply(BigDecimal.valueOf(unpaidLeaves));
            }

            // 4. Calculate net pay
            BigDecimal netPay = monthlySalary.subtract(leaveDeduction);

            // 5. Save to the Payslip table
            Payslip payslip = new Payslip();
            payslip.setEmployeeId(emp.getId());
            payslip.setMonth(month);
            payslip.setYear(year);
            payslip.setNetPay(netPay);
            payslip.setDeductions(leaveDeduction); // Store leave deduction in the 'deductions' column

            payslipRepository.save(payslip);
            System.out.printf("✅ Generated payslip for Employee ID %d (Net Pay: %.2f) for month %s\n", emp.getId(), netPay, formattedMonth);
        }
    }

    /**
     * Converts a month name string to its corresponding integer value (1-12).
     *
     * @param monthName The name of the month (e.g., "January", "February").
     * @return The integer representation of the month.
     */
    private int getMonthNumber(String monthName) {
        return java.time.Month.valueOf(monthName.toUpperCase()).getValue();
    }
}
