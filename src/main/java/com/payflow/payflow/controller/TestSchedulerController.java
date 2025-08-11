package com.payflow.payflow.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.payflow.payflow.Service.SchedulerTestService;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for testing scheduler functionality.
 * Provides endpoints to check the status of scheduled tasks.
 */
@RestController
@RequestMapping("/api/test/scheduler")
public class TestSchedulerController {

    @Autowired
    private SchedulerTestService schedulerTestService;

    /**
     * Endpoint to check if the hourly test scheduler is working.
     * Returns the last execution time of the hourly job.
     *
     * @return ResponseEntity containing status and last execution time
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSchedulerStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Scheduler job status");
        response.put("lastExecutionTime", schedulerTestService.getLastExecutionTime());
        response.put("isRunning", schedulerTestService.isSchedulerRunning());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to manually trigger the hourly test job.
     * Useful for testing without waiting for the scheduled time.
     *
     * @return ResponseEntity containing status and execution time
     */
    @GetMapping("/trigger")
    public ResponseEntity<Map<String, Object>> triggerSchedulerJob() {
        String executionTime = schedulerTestService.runSchedulerTest();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Scheduler job triggered manually");
        response.put("executionTime", executionTime);
        
        return ResponseEntity.ok(response);
    }
}