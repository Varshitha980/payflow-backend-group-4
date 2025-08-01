-- Test and fix the database state
-- Run this to see what's actually in your database

-- 1. Check what leave requests exist
SELECT '=== LEAVE REQUESTS IN DATABASE ===' as info;
SELECT id, employee_id, employee_email, start_date, end_date, reason, status FROM leave_request;

-- 2. Check employees assigned to manager 24
SELECT '=== EMPLOYEES FOR MANAGER 24 ===' as info;
SELECT id, name, email, manager_id FROM employee WHERE manager_id = 24;

-- 3. Show the mismatch
SELECT '=== EMAIL MISMATCH ===' as info;
SELECT 
    lr.id as leave_id,
    lr.employee_id,
    lr.employee_email as wrong_email,
    e.email as correct_email,
    e.name as employee_name
FROM leave_request lr
JOIN employee e ON lr.employee_id = e.id
WHERE lr.employee_email != e.email;

-- 4. Fix the emails NOW
UPDATE leave_request lr
JOIN employee e ON lr.employee_id = e.id
SET lr.employee_email = e.email;

-- 5. Verify the fix
SELECT '=== AFTER FIX ===' as info;
SELECT 
    lr.id as leave_id,
    lr.employee_id,
    lr.employee_email as fixed_email,
    e.email as employee_email,
    e.name as employee_name
FROM leave_request lr
JOIN employee e ON lr.employee_id = e.id;

-- 6. Show final result for manager 24
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