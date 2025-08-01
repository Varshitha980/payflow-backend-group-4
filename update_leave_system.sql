-- Update LeaveRequest table to add days and salaryDeducted fields
ALTER TABLE leave_request 
ADD COLUMN days INT DEFAULT 1,
ADD COLUMN salary_deducted BOOLEAN DEFAULT FALSE;

-- Update Employee table to add salary deduction tracking
ALTER TABLE employee 
ADD COLUMN salary_deduction_days INT DEFAULT 0,
ADD COLUMN monthly_salary DOUBLE DEFAULT 50000.0;

-- Update existing leave requests to calculate days
UPDATE leave_request 
SET days = DATEDIFF(end_date, start_date) + 1 
WHERE days IS NULL OR days = 0;

-- Update existing employees to set default leave balance if not set
UPDATE employee 
SET leave_balance = 12 
WHERE leave_balance IS NULL;

-- Update existing employees to set default monthly salary if not set
UPDATE employee 
SET monthly_salary = 50000.0 
WHERE monthly_salary IS NULL; 