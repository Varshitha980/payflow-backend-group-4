# Scheduler Test for Payflow Application

## Overview

This README explains how to test the scheduler functionality in the Payflow application. The scheduler is configured to run a test job every minute to verify that the Spring Boot scheduling system is working correctly.

## Implementation Details

1. **SchedulerTestService**: A service class that runs a scheduled task every minute and logs the execution time.
   - Located at: `src/main/java/com/payflow/payflow/Service/SchedulerTestService.java`
   - Uses Spring's `@Scheduled` annotation with a cron expression
   - For production, you can change the cron expression to run hourly instead of every minute

2. **TestSchedulerController**: A REST controller that provides endpoints to check the scheduler status and manually trigger the scheduler job.
   - Located at: `src/main/java/com/payflow/payflow/Controller/TestSchedulerController.java`
   - Provides two endpoints:
     - `/api/test/scheduler/status`: Returns the last execution time and whether the scheduler is running
     - `/api/test/scheduler/trigger`: Manually triggers the scheduler job

## How to Test

1. Start the Payflow application
2. Wait for at least one minute to allow the scheduler to run
3. Access the status endpoint to check if the scheduler is running:
   ```
   GET http://localhost:8081/api/test/scheduler/status
   ```
   Expected response:
   ```json
   {
     "success": true,
     "message": "Scheduler job status",
     "lastExecutionTime": "2023-06-01 12:00:00",
     "isRunning": true
   }
   ```

4. Manually trigger the scheduler job:
   ```
   GET http://localhost:8081/api/test/scheduler/trigger
   ```
   Expected response:
   ```json
   {
     "success": true,
     "message": "Scheduler job triggered manually",
     "executionTime": "2023-06-01 12:05:30"
   }
   ```

## Logs

Check the application logs to see the scheduler job execution. You should see log entries like:

```
Minutely test cron job running at: 2023-06-01 12:00:00
Minutely test cron job running at: 2023-06-01 12:01:00
Scheduler test job manually triggered at: 2023-06-01 12:05:30
```

## Changing the Schedule

To change the schedule from every minute to hourly (for production use):

1. Open `SchedulerTestService.java`
2. Change the cron expression from `0 * * * * ?` to `0 0 * * * ?`
3. Restart the application

## Troubleshooting

If the scheduler is not running:

1. Verify that `@EnableScheduling` is present in the main application class (`PayflowApplication.java`)
2. Check the application logs for any errors related to scheduling
3. Ensure that the cron expression is valid
4. Restart the application