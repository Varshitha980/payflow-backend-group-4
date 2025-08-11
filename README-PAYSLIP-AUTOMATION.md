# Automatic Payslip Generation

This document describes the automatic payslip generation feature implemented in the PayFlow application.

## Overview

The automatic payslip generation feature ensures that payslips are generated for all active employees at the end of each month without manual intervention. This is implemented using Spring's scheduling capabilities.

## Implementation Details

### Components

1. **PayslipSchedulerService**: A service class that uses Spring's `@Scheduled` annotation to automatically generate payslips at the end of each month.

2. **PayslipController**: Contains endpoints for manual payslip generation and a special endpoint for triggering the automatic generation process for testing purposes.

3. **PayslipService**: Contains the business logic for generating payslips, both individually and in bulk.

### Scheduling Configuration

The automatic payslip generation is scheduled to run at 11:59 PM on the last day of each month using the cron expression `0 59 23 L * ?`.

### Manual Trigger

For testing purposes, a manual trigger endpoint is available at `/api/payslip/generate-automatic`. This endpoint generates payslips for all active employees for the previous month.

## Frontend Integration

The frontend includes a button labeled "Auto-Generate Previous Month" in the Payslip Management page. This button triggers the automatic payslip generation process for the previous month, allowing HR personnel to manually initiate the process if needed.

## Business Rules

1. Payslips can only be generated for completed months. The system prevents generation of payslips for the current month.

2. The automatic generation process runs at the end of each month, ensuring that all payslips are ready for the following month.

3. If an employee has excess leave days, the appropriate deductions are applied to their salary before generating the payslip.

## Testing

To test the automatic payslip generation feature:

1. Navigate to the Payslip Management page.

2. Click the "Auto-Generate Previous Month" button.

3. The system will generate payslips for all active employees for the previous month.

4. A success message will be displayed if the generation is successful.

## Troubleshooting

If the automatic payslip generation fails, check the following:

1. Ensure that all employees have valid CTC details.

2. Check the server logs for any errors during the generation process.

3. Verify that the system date and time are correct.

4. Ensure that the database is accessible and has sufficient space.