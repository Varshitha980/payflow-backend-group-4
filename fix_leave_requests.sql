-- Fix existing leave requests by updating employee emails to match actual employees
-- Based on your employee data, employee ID 7 has email '23wh1a1204@bvrithyderabad.edu.in'

-- First, let's see what we have
SELECT 'Current leave requests:' as info;
SELECT * FROM leave_request;

-- Update leave requests to use correct employee emails
UPDATE leave_request 
SET employee_email = '23wh1a1204@bvrithyderabad.edu.in' 
WHERE employee_id = 7 AND employee_email = 'employee7@payflow.com';

-- Add more leave requests for other employees assigned to manager 24
INSERT INTO leave_request (employee_id, employee_email, start_date, end_date, reason, status) 
VALUES 
(12, 'tlsvarshitha@gmail.com', '2025-01-20', '2025-01-22', 'Medical appointment', 'PENDING'),
(16, 'tlsvarshitha05@gmail.com', '2025-01-25', '2025-01-27', 'Family vacation', 'PENDING');

-- Verify the updates
SELECT 'After updates:' as info;
SELECT * FROM leave_request;

-- Test query to see leave requests for manager 24's employees
SELECT 'Leave requests for manager 24 employees:' as info;
SELECT lr.*, e.name as employee_name 
FROM leave_request lr 
JOIN employee e ON lr.employee_id = e.id 
WHERE e.manager_id = 24; 