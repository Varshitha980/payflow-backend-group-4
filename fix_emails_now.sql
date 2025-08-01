-- DIRECT FIX: Update all leave requests with correct employee emails
-- This will fix the "employee7@payflow.com" issue

-- First, show what we're fixing
SELECT 'BEFORE FIX:' as info;
SELECT id, employee_id, employee_email FROM leave_request WHERE employee_email = 'employee7@payflow.com';

-- Fix employee ID 7 emails
UPDATE leave_request 
SET employee_email = '23wh1a1204@bvrithyderabad.edu.in' 
WHERE employee_id = 7;

-- Fix any other incorrect emails
UPDATE leave_request lr
JOIN employee e ON lr.employee_id = e.id
SET lr.employee_email = e.email
WHERE lr.employee_email != e.email;

-- Show the result
SELECT 'AFTER FIX:' as info;
SELECT id, employee_id, employee_email FROM leave_request;

-- Show final result for manager 24
SELECT 'FINAL RESULT FOR MANAGER 24:' as info;
SELECT 
    lr.id,
    e.name as employee_name,
    lr.employee_email,
    lr.start_date,
    lr.end_date,
    lr.reason,
    lr.status
FROM leave_request lr
JOIN employee e ON lr.employee_id = e.id
WHERE e.manager_id = 24
ORDER BY lr.id; 