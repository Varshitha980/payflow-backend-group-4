-- Add PF Number column to employee table
-- This script adds the pf_number column to store the Provident Fund number for each employee

ALTER TABLE employee 
ADD COLUMN pf_number VARCHAR(50) NULL COMMENT 'Provident Fund (PF) number of the employee';

-- Add an index on pf_number for better query performance
CREATE INDEX idx_employee_pf_number ON employee(pf_number);

-- Update existing records with a default value if needed (optional)
-- UPDATE employee SET pf_number = CONCAT('PF', LPAD(id, 6, '0')) WHERE pf_number IS NULL; 