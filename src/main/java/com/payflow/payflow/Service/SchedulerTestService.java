package com.payflow.payflow.Service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

/**
 * Service class for testing scheduler functionality.
 * This service runs a simple task every minute to verify that the scheduling system is working.
 */
@Service
public class SchedulerTestService {

    private static final Logger logger = Logger.getLogger(SchedulerTestService.class.getName());
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private String lastExecutionTime = "Not executed yet";
    private boolean schedulerRunning = true;

    /**
     * Scheduled task that runs every minute to test if the scheduling system is working.
     * This method logs the current time when it runs.
     * Note: For production, change this to hourly with: 0 0 * * * ?
     */
    @Scheduled(cron = "0 * * * * ?") // Run every minute for testing
    public void minutelyTest() {
        LocalDateTime now = LocalDateTime.now();
        String formattedTime = now.format(formatter);
        
        lastExecutionTime = formattedTime;
        logger.info("Minutely test cron job running at: " + formattedTime);
        
        // You can add more test logic here if needed
        // For example, checking database connectivity, API availability, etc.
    }
    
    /**
     * Manually run the scheduler test job.
     * 
     * @return The execution time as a formatted string
     */
    public String runSchedulerTest() {
        LocalDateTime now = LocalDateTime.now();
        String formattedTime = now.format(formatter);
        
        lastExecutionTime = formattedTime;
        logger.info("Scheduler test job manually triggered at: " + formattedTime);
        
        return formattedTime;
    }
    
    /**
     * Get the last time the scheduler job was executed.
     * 
     * @return The last execution time as a formatted string
     */
    public String getLastExecutionTime() {
        return lastExecutionTime;
    }
    
    /**
     * Check if the scheduler is currently running.
     * 
     * @return true if the scheduler is running, false otherwise
     */
    public boolean isSchedulerRunning() {
        return schedulerRunning;
    }
}