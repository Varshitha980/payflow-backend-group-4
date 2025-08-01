-- Check and fix email mismatches in leave requests
-- First, let's see the current state

SELECT '=== CURRENT LEAVE REQUESTS ===' as info;
SELECT id, employee_id, employee_email, start_date, end_date, reason, status FROM leave_request;

SELECT '=== EMPLOYEES ASSIGNED TO MANAGER 24 ===' as info;
SELECT id, name, email, manager_id FROM employee WHERE manager_id = 24;

SELECT '=== MISMATCHED EMAILS ===' as info;
SELECT 
    lr.id as leave_id,
    lr.employee_id,
    lr.employee_email as leave_email,
    e.email as employee_email,
    e.name as employee_name
FROM leave_request lr
LEFT JOIN employee e ON lr.employee_id = e.id
WHERE lr.employee_email != e.email OR e.email IS NULL;

-- Fix the email mismatches
UPDATE leave_request lr
JOIN employee e ON lr.employee_id = e.id
SET lr.employee_email = e.email
WHERE lr.employee_email != e.email;

-- Verify the fix
SELECT '=== AFTER FIXING EMAILS ===' as info;
SELECT 
    lr.id as leave_id,
    lr.employee_id,
    lr.employee_email as leave_email,
    e.email as employee_email,
    e.name as employee_name
FROM leave_request lr
LEFT JOIN employee e ON lr.employee_id = e.id;

-- Show final result for manager 24
SELECT '=== FINAL RESULT FOR MANAGER 24 ===' as info;
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