// Test script to debug leave request functionality
// Run this in the browser console or as a Node.js script

const BASE_URL = 'http://localhost:8081/api';

async function testLeaveRequests() {
    console.log('🧪 Testing Leave Request Functionality...\n');

    try {
        // 1. Get debug data
        console.log('1. Getting debug data...');
        const debugRes = await fetch(`${BASE_URL}/leaves/debug`);
        const debugData = await debugRes.json();
        console.log('✅ Debug data retrieved');
        console.log('   - Total leave requests:', debugData.totalLeaveRequests);
        console.log('   - Total employees:', debugData.totalEmployees);
        console.log('   - Leave requests:', debugData.leaveRequests);
        console.log('   - Employees:', debugData.employees);

        // 2. Find a manager
        const managers = debugData.employees.filter(emp => emp.role === 'MANAGER');
        console.log('\n2. Found managers:', managers);

        if (managers.length === 0) {
            console.log('❌ No managers found. Please create a manager first.');
            return;
        }

        const manager = managers[0];
        console.log('   Using manager:', manager.name, '(ID:', manager.id, ')');

        // 3. Find employees assigned to this manager
        const assignedEmployees = debugData.employees.filter(emp => emp.managerId === manager.id);
        console.log('\n3. Employees assigned to manager:', assignedEmployees);

        if (assignedEmployees.length === 0) {
            console.log('❌ No employees assigned to this manager. Please assign employees first.');
            return;
        }

        const employee = assignedEmployees[0];
        console.log('   Using employee:', employee.name, '(ID:', employee.id, ', Email:', employee.email, ')');

        // 4. Test manager-specific leave requests endpoint
        console.log('\n4. Testing manager-specific leave requests...');
        const managerLeavesRes = await fetch(`${BASE_URL}/leaves/manager/${manager.id}`);
        const managerLeaves = await managerLeavesRes.json();
        console.log('   Manager leave requests:', managerLeaves);

        // 5. Create a test leave request
        console.log('\n5. Creating test leave request...');
        const testLeave = {
            employeeEmail: employee.email,
            startDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0], // 7 days from now
            endDate: new Date(Date.now() + 10 * 24 * 60 * 60 * 1000).toISOString().split('T')[0], // 10 days from now
            reason: 'Test leave request for debugging'
        };

        const createRes = await fetch(`${BASE_URL}/leaves/create`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(testLeave)
        });

        if (createRes.ok) {
            const createdLeave = await createRes.json();
            console.log('✅ Test leave request created:', createdLeave);
        } else {
            console.log('❌ Failed to create test leave request');
            const error = await createRes.text();
            console.log('   Error:', error);
        }

        // 6. Test manager-specific endpoint again
        console.log('\n6. Testing manager-specific leave requests again...');
        const managerLeavesRes2 = await fetch(`${BASE_URL}/leaves/manager/${manager.id}`);
        const managerLeaves2 = await managerLeavesRes2.json();
        console.log('   Manager leave requests (after creation):', managerLeaves2);

        // 7. Check if the new leave request appears
        const newLeave = managerLeaves2.find(leave => 
            leave.employeeEmail === employee.email && 
            leave.reason === 'Test leave request for debugging'
        );

        if (newLeave) {
            console.log('✅ New leave request found in manager dashboard!');
            console.log('   Leave ID:', newLeave.id);
            console.log('   Status:', newLeave.status);
        } else {
            console.log('❌ New leave request NOT found in manager dashboard');
            console.log('   This indicates the issue with the filtering logic');
        }

    } catch (error) {
        console.error('❌ Error during testing:', error);
    }
}

// Run the test
testLeaveRequests(); 