package com.payflow.payflow.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Service class for scheduling automatic payslip generation.
 * This service uses Spring's scheduling capabilities to automatically
 * generate payslips for all employees at the end of each month.
 */
@Service
public class PayslipSchedulerService {

    private static final Logger logger = Logger.getLogger(PayslipSchedulerService.class.getName());

    @Autowired
    private PayslipService payslipService;

    /**
     * Scheduled task to automatically generate payslips for all employees.
     * This method is temporarily set to run every minute for testing.
     * Original: @Scheduled(cron = "0 59 23 L * ?") // Runs at 11:59 PM on the last day of each month
     */
    @Scheduled(cron = "0 * * * * ?") // TEMPORARY: Run every minute for testing purposes
    public void generateMonthlyPayslips() {
        // Get the current date (which is the last day of the month when this runs)
        LocalDate currentDate = LocalDate.now();

        // Get the month and year for which we're generating payslips
        // (the current month that's ending)
        String month = currentDate.getMonth().toString();
        int year = currentDate.getYear();

        // Convert month to title case (e.g., "JANUARY" to "January")
        month = month.substring(0, 1) + month.substring(1).toLowerCase();

        logger.info("Starting automatic payslip generation for " + month + " " + year);

        // Generate payslips for all employees
        Map<String, Object> result = payslipService.generateBulkPayslips(month, year);

        if ((Boolean) result.get("success")) {
            logger.info("Automatic payslip generation completed successfully: " + result.get("message"));
        } else {
            logger.severe("Automatic payslip generation failed: " + result.get("message"));
        }
    }
}