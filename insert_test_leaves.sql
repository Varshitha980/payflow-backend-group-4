-- Insert test leave requests for employees assigned to manager 24 (mgr2)
-- Based on your employee table data, employees 7, 12, and 16 are assigned to manager 24

-- First, let's check if the leave_request table exists and what's in it
SELECT 'Checking leave_request table structure' as info;
DESCRIBE leave_request;

SELECT 'Current leave requests:' as info;
SELECT * FROM leave_request;

-- Insert test leave requests for employees assigned to manager 24 (mgr2)
INSERT INTO leave_request (employee_id, employee_email, start_date, end_date, reason, status) 
VALUES 
(7, '23wh1a1204@bvrithyderabad.edu.in', '2025-01-15', '2025-01-17', 'Personal leave', 'PENDING'),
(12, 'tlsvarshitha@gmail.com', '2025-01-20', '2025-01-22', 'Medical appointment', 'PENDING'),
(16, 'tlsvarshitha05@gmail.com', '2025-01-25', '2025-01-27', 'Family vacation', 'PENDING');

-- Insert test leave requests for employees assigned to manager 9
INSERT INTO leave_request (employee_id, employee_email, start_date, end_date, reason, status) 
VALUES 
(1, 'vineela@gmail.com', '2025-01-10', '2025-01-12', 'Sick leave', 'PENDING'),
(9, 'test@example.com', '2025-01-18', '2025-01-20', 'Conference attendance', 'PENDING'),
(11, 'test3@example.com', '2025-01-30', '2025-02-01', 'Training session', 'PENDING'),
(14, 'testemployee@company.com', '2025-02-05', '2025-02-07', 'Personal emergency', 'PENDING');

-- Verify the insertions
SELECT 'After insertions:' as info;
SELECT * FROM leave_request;

-- Test query to see employees assigned to manager 24
SELECT 'Employees assigned to manager 24:' as info;
SELECT id, name, email, manager_id FROM employee WHERE manager_id = 24;

-- Test query to see leave requests for manager 24's employees
SELECT 'Leave requests for manager 24 employees:' as info;
SELECT lr.*, e.name as employee_name 
FROM leave_request lr 
JOIN employee e ON lr.employee_id = e.id 
WHERE e.manager_id = 24; 