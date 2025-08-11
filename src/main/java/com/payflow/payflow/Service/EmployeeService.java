package com.payflow.payflow.Service;

import com.payflow.payflow.model.Employee;
import com.payflow.payflow.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper; // Added import for ObjectMapper

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private CTCDetailsService ctcDetailsService;

    // Initialize ObjectMapper once
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public Employee createEmployeeWithOnboarding(Map<String, Object> payload) throws Exception {
        try {
            // Log the full payload for debugging
            System.out.println("Received payload for employee creation: " + payload);
            System.out.println("Payload keys: " + payload.keySet());
            
            // Log each field value for debugging
            for (Map.Entry<String, Object> entry : payload.entrySet()) {
                System.out.println("Field: " + entry.getKey() + " = " + entry.getValue() + " (Type: " + (entry.getValue() != null ? entry.getValue().getClass().getSimpleName() : "null") + ")");
            }

            // Validate required fields
            String name = (String) payload.get("name");
            String email = (String) payload.get("email");

            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Employee name is required");
            }

            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("Employee email is required");
            }

            // Check if employee with same email already exists
            Optional<Employee> existingEmployee = employeeRepository.findByEmail(email);
            if (existingEmployee.isPresent()) {
                throw new IllegalArgumentException("Employee with this email already exists.");
            }

            Employee newEmployee = new Employee();
            newEmployee.setName(name);
            newEmployee.setEmail(email);

            // Populate all other fields from the payload, with type conversions and defaults
            if (payload.containsKey("phone")) {
                newEmployee.setPhone((String) payload.get("phone"));
            }
            if (payload.containsKey("address")) {
                newEmployee.setAddress((String) payload.get("address"));
            }
            if (payload.containsKey("designation")) { // Maps to 'position' in Employee entity
                newEmployee.setPosition((String) payload.get("designation"));
            }
            // Use "joiningDate" from payload, as sent by frontend
            if (payload.containsKey("joiningDate")) { 
                String joiningDateStr = (String) payload.get("joiningDate");
                if (joiningDateStr != null) {
                    try {
                        newEmployee.setStartDate(LocalDate.parse(joiningDateStr));
                    } catch (DateTimeParseException e) {
                        throw new IllegalArgumentException("Invalid date format for 'joiningDate'. Please use YYYY-MM-DD.");
                    }
                }
            } else {
                newEmployee.setStartDate(LocalDate.now()); // Default to current date if not provided
            }

            // Numeric fields with nullable=false in DB, ensure they are set
            // FIX: More robust age handling
            Integer ageValue = 0; // Default value for age
            if (payload.containsKey("age")) {
                Object ageObj = payload.get("age");
                if (ageObj instanceof Number) {
                    ageValue = ((Number) ageObj).intValue();
                } else if (ageObj instanceof String && !((String) ageObj).isEmpty()) {
                    try {
                        ageValue = Integer.parseInt((String) ageObj);
                    } catch (NumberFormatException e) {
                        System.err.println("Warning: Could not parse age from string: " + ageObj + ". Using default 0.");
                    }
                }
            }
            newEmployee.setAge(ageValue); // Set the determined age value

            if (payload.containsKey("totalExperience")) {
                Object totalExperienceObj = payload.get("totalExperience");
                if (totalExperienceObj instanceof Number) {
                    newEmployee.setTotalExperience(((Number) totalExperienceObj).intValue());
                } else if (totalExperienceObj instanceof String) {
                    newEmployee.setTotalExperience(Integer.parseInt((String) totalExperienceObj));
                } else {
                    throw new IllegalArgumentException("Invalid format for 'totalExperience'. Must be a number.");
                }
            } else {
                newEmployee.setTotalExperience(0); // Default experience if not provided
            }

            if (payload.containsKey("pfNumber")) {
                newEmployee.setPfNumber((String) payload.get("pfNumber"));
            }

            if (payload.containsKey("leaves")) {
                Object leavesObj = payload.get("leaves");
                if (leavesObj instanceof Number) {
                    newEmployee.setLeaves(((Number) leavesObj).intValue());
                } else if (leavesObj instanceof String) {
                    newEmployee.setLeaves(Integer.parseInt((String) leavesObj));
                }
            } else {
                newEmployee.setLeaves(12); // Default from schema
            }

            if (payload.containsKey("status")) {
                newEmployee.setStatus((String) payload.get("status"));
            } else {
                newEmployee.setStatus("ACTIVE"); // Default status
            }

            if (payload.containsKey("password")) {
                newEmployee.setPassword((String) payload.get("password"));
            } else {
                newEmployee.setPassword("1234"); // Default password from schema
            }

            if (payload.containsKey("firstLogin")) {
                newEmployee.setFirstLogin((Boolean) payload.get("firstLogin"));
            } else {
                newEmployee.setFirstLogin(true); // Default from schema
            }

            // FIX: Handle education and experiences as JSON strings
            if (payload.containsKey("education")) {
                Object educationObj = payload.get("education");
                if (educationObj != null) {
                    try {
                        newEmployee.setEducation(objectMapper.writeValueAsString(educationObj));
                    } catch (Exception e) {
                        System.err.println("Warning: Could not serialize education to string: " + e.getMessage());
                        newEmployee.setEducation(""); // Default to empty string on error
                    }
                } else {
                    newEmployee.setEducation("");
                }
            } else {
                newEmployee.setEducation("");
            }

            if (payload.containsKey("experiences")) {
                Object experiencesObj = payload.get("experiences");
                if (experiencesObj != null) {
                    try {
                        newEmployee.setExperiences(objectMapper.writeValueAsString(experiencesObj));
                    } catch (Exception e) {
                        System.err.println("Warning: Could not serialize experiences to string: " + e.getMessage());
                        newEmployee.setExperiences(""); // Default to empty string on error
                    }
                } else {
                    newEmployee.setExperiences("");
                }
            } else {
                newEmployee.setExperiences("");
            }

            if (payload.containsKey("role")) {
                newEmployee.setRole((String) payload.get("role"));
            } else {
                newEmployee.setRole("EMPLOYEE"); // Default role
            }
            
            if (payload.containsKey("leaveBalance")) {
                 Object leaveBalanceObj = payload.get("leaveBalance");
                if (leaveBalanceObj instanceof Number) {
                    newEmployee.setLeaveBalance(((Number) leaveBalanceObj).intValue());
                } else if (leaveBalanceObj instanceof String) {
                    newEmployee.setLeaveBalance(Integer.parseInt((String) leaveBalanceObj));
                }
            } else {
                newEmployee.setLeaveBalance(12); // Default from schema
            }

            if (payload.containsKey("pastExperience")) {
                newEmployee.setPastExperience((String) payload.get("pastExperience"));
            }
            
            if (payload.containsKey("username")) {
                newEmployee.setUsername((String) payload.get("username"));
            } else {
                newEmployee.setUsername(email); // Default username to email
            }

            if (payload.containsKey("managerId")) {
                Object managerIdObj = payload.get("managerId");
                if (managerIdObj instanceof Number) {
                    newEmployee.setManagerId(((Number) managerIdObj).longValue());
                } else if (managerIdObj instanceof String) {
                    newEmployee.setManagerId(Long.parseLong((String) managerIdObj));
                } else {
                    newEmployee.setManagerId(null); // Default to null if invalid or not provided
                }
            } else {
                newEmployee.setManagerId(null); // Default to null if not provided
            }

            if (payload.containsKey("salaryDeductionDays")) {
                Object salaryDeductionDaysObj = payload.get("salaryDeductionDays");
                if (salaryDeductionDaysObj instanceof Number) {
                    newEmployee.setSalaryDeductionDays(((Number) salaryDeductionDaysObj).intValue());
                } else if (salaryDeductionDaysObj instanceof String) {
                    newEmployee.setSalaryDeductionDays(Integer.parseInt((String) salaryDeductionDaysObj));
                }
            } else {
                newEmployee.setSalaryDeductionDays(0); // Default from schema
            }

            if (payload.containsKey("monthlySalary")) {
                Object monthlySalaryObj = payload.get("monthlySalary");
                if (monthlySalaryObj instanceof Number) {
                    newEmployee.setMonthlySalary(((Number) monthlySalaryObj).doubleValue());
                } else if (monthlySalaryObj instanceof String) {
                    newEmployee.setMonthlySalary(Double.parseDouble((String) monthlySalaryObj));
                } else {
                    newEmployee.setMonthlySalary(50000.0); // Default from schema if invalid or not provided
                }
            } else {
                newEmployee.setMonthlySalary(50000.0); // Default from schema
            }
            
            // --- Debugging: Log Employee object state before saving ---
            System.out.println("Employee object state before saving:");
            System.out.println("  ID: " + newEmployee.getId()); // Will be null for new entity
            System.out.println("  Name: " + newEmployee.getName());
            System.out.println("  Email: " + newEmployee.getEmail());
            System.out.println("  Phone: " + newEmployee.getPhone());
            System.out.println("  Address: " + newEmployee.getAddress());
            System.out.println("  Position: " + newEmployee.getPosition());
            System.out.println("  StartDate: " + newEmployee.getStartDate());
            System.out.println("  Leaves: " + newEmployee.getLeaves());
            System.out.println("  Status: " + newEmployee.getStatus());
            System.out.println("  Password: " + newEmployee.getPassword());
            System.out.println("  FirstLogin: " + newEmployee.getFirstLogin());
            System.out.println("  Education: " + newEmployee.getEducation());
            System.out.println("  Experiences: " + newEmployee.getExperiences());
            System.out.println("  Role: " + newEmployee.getRole());
            System.out.println("  Age: " + newEmployee.getAge());
            System.out.println("  LeaveBalance: " + newEmployee.getLeaveBalance());
            System.out.println("  PastExperience: " + newEmployee.getPastExperience());
            System.out.println("  TotalExperience: " + newEmployee.getTotalExperience());
            System.out.println("  PFNumber: " + newEmployee.getPfNumber());
            System.out.println("  Username: " + newEmployee.getUsername());
            System.out.println("  ManagerId: " + newEmployee.getManagerId());
            System.out.println("  SalaryDeductionDays: " + newEmployee.getSalaryDeductionDays());
            System.out.println("  MonthlySalary: " + newEmployee.getMonthlySalary());
            // --- End Debugging Log ---

            // Save the employee first to get an ID
            Employee savedEmployee = employeeRepository.save(newEmployee);
            System.out.println("Employee created successfully: " + savedEmployee.getName() + " (ID: " + savedEmployee.getId() + ")");
            
            // --- CTC Details Handling ---
            // Check if CTC details are provided in the payload
            boolean hasCTCDetails = payload.containsKey("basicSalary") || 
                                     payload.containsKey("hra") || 
                                     payload.containsKey("allowances") || 
                                     payload.containsKey("bonuses") || 
                                     payload.containsKey("pfContribution") || 
                                     payload.containsKey("gratuity") || 
                                     payload.containsKey("totalCTC") || 
                                     payload.containsKey("totalCtc") || 
                                     payload.containsKey("da") || 
                                     payload.containsKey("specialAllowance");
            
            // If CTC details are provided, create a CTC record for the employee
            if (hasCTCDetails) {
                try {
                    System.out.println("Creating CTC details for employee: " + savedEmployee.getId());
                    // Create a new map for CTC details
                    Map<String, Object> ctcPayload = new HashMap<>();
                    
                    // Set the employee ID
                    ctcPayload.put("employeeId", savedEmployee.getId());
                    
                    // Set effective from date (use joining date if available, otherwise current date)
                    LocalDate effectiveFrom = savedEmployee.getStartDate() != null ? 
                                              savedEmployee.getStartDate() : 
                                              LocalDate.now();
                    ctcPayload.put("effectiveFrom", effectiveFrom.toString());
                    
                    // Copy CTC-related fields from the original payload
                    if (payload.containsKey("basicSalary")) ctcPayload.put("basicSalary", payload.get("basicSalary"));
                    if (payload.containsKey("hra")) ctcPayload.put("hra", payload.get("hra"));
                    if (payload.containsKey("allowances")) ctcPayload.put("allowances", payload.get("allowances"));
                    if (payload.containsKey("bonuses")) ctcPayload.put("bonuses", payload.get("bonuses"));
                    if (payload.containsKey("pfContribution")) ctcPayload.put("pfContribution", payload.get("pfContribution"));
                    if (payload.containsKey("gratuity")) ctcPayload.put("gratuity", payload.get("gratuity"));
                    if (payload.containsKey("totalCTC")) ctcPayload.put("totalCTC", payload.get("totalCTC")); 
                    if (payload.containsKey("totalCtc")) ctcPayload.put("totalCTC", payload.get("totalCtc")); // Map totalCtc to totalCTC
                    if (payload.containsKey("da")) ctcPayload.put("da", payload.get("da"));
                    if (payload.containsKey("specialAllowance")) ctcPayload.put("specialAllowance", payload.get("specialAllowance"));
                    
                    System.out.println("CTC payload prepared: " + ctcPayload);
                    
                    // Call the CTCDetailsService to add the CTC details
                    Map<String, Object> ctcResponse = ctcDetailsService.addCTCDetails(ctcPayload);
                    
                    if (!(boolean) ctcResponse.get("success")) {
                        System.err.println("Warning: Failed to add CTC details for employee " + savedEmployee.getId() + ": " + ctcResponse.get("message"));
                    } else {
                        System.out.println("CTC details added successfully for employee: " + savedEmployee.getId());
                    }
                } catch (Exception e) {
                    System.err.println("Warning: Error adding CTC details for employee " + savedEmployee.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                    // Don't fail the entire employee creation if CTC creation fails
                }
            } else {
                System.out.println("No CTC details provided for employee: " + savedEmployee.getId());
            }

            return savedEmployee;

        } catch (IllegalArgumentException e) {
            System.err.println("Validation error: " + e.getMessage());
            throw e; // Re-throw to be caught by the controller
        } catch (Exception e) {
            System.err.println("Error creating employee: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for detailed debugging
            throw new Exception("Failed to create employee: " + e.getMessage()); // Re-throw as a generic exception
        }
    }

    public Employee findByEmailAndPassword(String email, String password) {
        return employeeRepository.findByEmailAndPassword(email, password).orElse(null);
    }

    public List<Employee> getEmployeesByManager(Long managerId) {
        return employeeRepository.findByManagerId(managerId);
    }

    public List<Employee> getUnassignedEmployees() { // Renamed method
        return employeeRepository.findByManagerIdIsNull();
    }

    public Employee assignEmployeeToManager(Long employeeId, Long managerId) throws Exception {
        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
        if (!employeeOpt.isPresent()) {
            throw new Exception("Employee not found");
        }

        Employee employee = employeeOpt.get();
        employee.setManagerId(managerId);
        return employeeRepository.save(employee);
    }
    
    /**
     * Get an employee by ID.
     * 
     * @param id The ID of the employee to retrieve.
     * @return The employee if found, or null if not found.
     */
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id).orElse(null);
    }
}
