-- Test script for Manager/HR creation and password reset

-- 1. Create test manager
INSERT INTO user (username, password, role, status, first_login) 
VALUES ('testmanager', '1234', 'MANAGER', 'ACTIVE', true)
ON DUPLICATE KEY UPDATE username = username;

-- 2. Create test HR
INSERT INTO user (username, password, role, status, first_login) 
VALUES ('testhr', '1234', 'HR', 'ACTIVE', true)
ON DUPLICATE KEY UPDATE username = username;

-- 3. Create test employee
INSERT INTO employee (name, email, password, role, first_login, status, manager_id, age, total_experience) 
VALUES ('Test Employee', 'test@example.com', '1234', 'EMPLOYEE', true, 'ACTIVE', 1, 25, 0)
ON DUPLICATE KEY UPDATE name = name;

-- 4. Verify the data
SELECT 'Users (Managers/HR):' as info;
SELECT id, username, role, first_login FROM user WHERE role IN ('MANAGER', 'HR');

SELECT 'Employees:' as info;
SELECT id, name, email, role, first_login FROM employee WHERE role = 'EMPLOYEE';

-- 5. Test password reset (run these after testing the reset functionality)
-- UPDATE user SET password = 'newpassword123', first_login = false WHERE username = 'testmanager';
-- UPDATE user SET password = 'newpassword123', first_login = false WHERE username = 'testhr';
-- UPDATE employee SET password = 'newpassword123', first_login = false WHERE email = 'test@example.com'; 