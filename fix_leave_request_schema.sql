-- Fix LeaveRequest table schema
-- Remove old columns that are no longer in the entity
-- Note: Run these commands one by one to avoid errors if columns don't exist

-- First, check if first_login column exists and drop it
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'payflow_db' 
     AND TABLE_NAME = 'leave_request' 
     AND COLUMN_NAME = 'first_login') > 0,
    'ALTER TABLE leave_request DROP COLUMN first_login',
    'SELECT "first_login column does not exist" as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check if default_password column exists and drop it
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'payflow_db' 
     AND TABLE_NAME = 'leave_request' 
     AND COLUMN_NAME = 'default_password') > 0,
    'ALTER TABLE leave_request DROP COLUMN default_password',
    'SELECT "default_password column does not exist" as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add new columns if they don't exist
-- Check if days column exists
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'payflow_db' 
     AND TABLE_NAME = 'leave_request' 
     AND COLUMN_NAME = 'days') = 0,
    'ALTER TABLE leave_request ADD COLUMN days INT DEFAULT 1',
    'SELECT "days column already exists" as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check if salary_deducted column exists
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'payflow_db' 
     AND TABLE_NAME = 'leave_request' 
     AND COLUMN_NAME = 'salary_deducted') = 0,
    'ALTER TABLE leave_request ADD COLUMN salary_deducted BOOLEAN DEFAULT FALSE',
    'SELECT "salary_deducted column already exists" as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Update existing leave requests to calculate days
UPDATE leave_request 
SET days = DATEDIFF(end_date, start_date) + 1 
WHERE days IS NULL OR days = 0;

-- Update existing leave requests to set default salary_deducted
UPDATE leave_request 
SET salary_deducted = FALSE 
WHERE salary_deducted IS NULL;

-- Update Employee table to add salary deduction tracking if not exists
-- Check if salary_deduction_days column exists
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'payflow_db' 
     AND TABLE_NAME = 'employee' 
     AND COLUMN_NAME = 'salary_deduction_days') = 0,
    'ALTER TABLE employee ADD COLUMN salary_deduction_days INT DEFAULT 0',
    'SELECT "salary_deduction_days column already exists" as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check if monthly_salary column exists
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'payflow_db' 
     AND TABLE_NAME = 'employee' 
     AND COLUMN_NAME = 'monthly_salary') = 0,
    'ALTER TABLE employee ADD COLUMN monthly_salary DOUBLE DEFAULT 50000.0',
    'SELECT "monthly_salary column already exists" as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Update existing employees to set default leave balance if not set
UPDATE employee 
SET leave_balance = 12 
WHERE leave_balance IS NULL;

-- Update existing employees to set default monthly salary if not set
UPDATE employee 
SET monthly_salary = 50000.0 
WHERE monthly_salary IS NULL; 