package com.luv2code.springboot.thymeleafdemo.service;

import java.util.List;
import java.util.Collections;
import java.util.Optional;

import com.luv2code.springboot.thymeleafdemo.dao.EmployeeRepository;
import com.luv2code.springboot.thymeleafdemo.entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.luv2code.springboot.thymeleafdemo.dao.ShiftRepository;
import com.luv2code.springboot.thymeleafdemo.entity.Shift;

@Service
public class ShiftServiceImpl implements ShiftService {

	private ShiftRepository shiftRepository;

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	public ShiftServiceImpl(ShiftRepository theShiftRepository) {
		shiftRepository = theShiftRepository;
	}

	@Override
	public List<Shift> findAll() {
		return shiftRepository.findAll();
	}

	@Override
	public List<Shift> findAllByEmployeeId (int theId) {
		return employeeRepository.getReferenceById(theId).getShifts();
	}
	
	@Override
	public Shift findById(int theId) {
		Optional<Shift> result = shiftRepository.findById(theId);
		
		Shift theShift = null;
		
		if (result.isPresent()) {
			theShift = result.get();
		}
		else {
			// we didn't find the shift
			throw new RuntimeException("Did not find shift id - " + theId);
		}
		
		return theShift;
	}

	@Override
	public void save(Shift theShift, int employeeId) {
		//get employee by id
		theShift.setEmployee(employeeRepository.getReferenceById(employeeId));

		//System.out.println(theShift.getEmployee());

		shiftRepository.save(theShift);
	}

	@Override
	public void deleteById(int theId) {
		shiftRepository.deleteById(theId);
	}
	
	public void assignShiftsToEmployees(List<Shift> shiftList) {
	      
	      List<Employee> employeeList = employeeRepository.findAllByOrderByLastNameAsc();
	      Collections.shuffle(employeeList);
	      int stretch; 
	      int j =0;
	  
	      for (Employee e : employeeList) {
		
		for (int i = j; i <= shiftList.size() ; i+4) {
		    if (stretch > 0 && shiftList.get(i).getEmployee() != null) {
		      shiftList.get(i).setEmployee(e);
		    }
		}
		
		j++;
		
	      }
	    
	  }
}






