-- Create payment_hold table in payflow_db database
USE payflow_db;

-- Create payment_hold table
CREATE TABLE IF NOT EXISTS payment_hold (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    reason TEXT,
    applied_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    applied_by BIGINT,
    CONSTRAINT fk_payment_hold_employee FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE
);

-- Add index for faster lookups by employee_id
CREATE INDEX IF NOT EXISTS idx_payment_hold_employee_id ON payment_hold(employee_id);

-- Table stores information about payment holds placed on employees
