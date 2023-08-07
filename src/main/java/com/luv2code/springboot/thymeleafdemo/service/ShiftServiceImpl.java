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
  
	private List<Shift> monthlyShifts = new ArrayList<>();

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
	
	public List<Shifts> getMonthlyShifts (int month, int year) {
	  //allShifts = this.findAll();
	  monthlyShifts.clear();
	  for (Shift s : this.findAll()) {
	    if ((s.date.getMonthValue() == month) && (s.date.getYear() == year))
	      monthlyShifts.add(s);
	    
	    if (monthlyShifts.size() == 124) 
	      return monthlyShifts;
	    
	  }
	  return monthlyShifts;
	}
	
	public void assignShiftsToEmployees(List<Shift> shiftList) {			// shiftList is a generated shift list WITHOUT employees
	      
	      List<Employee> employeeList = employeeRepository.findAllByOrderByLastNameAsc();
	      Collections.shuffle(employeeList);
	      int stretch; 
	      int j =0;
	      int previousMonth = shiftList.get(0).getDate().minusMonths(1L).getMonth();
	      if ((previousMonth == 2) || (previousMonth == 4) || (previousMonth == 6) || (previousMonth == 9) || (previousMonth == 11))	{
		// need 2 months prior to current month
		previousMonth = shiftList.get(0).getDate().minusMonths(2L).getMonth();
	      }
	      int previousYear = shiftList.get(0).getDate().minusMonths(1L).getYear();
	      List<Shifts> previousShifts = getMonthlyShifts(previousMonth, previousYear);
	      int monthLength = shiftList.get(0).getDate().lengthOfMonth();
	      boolean[] moreShifts = new boolean[8];
	      int [] stretches = new int[3];
	      
	      for (Employee e : employeeList) {
		if ((monthLength == 30) || (monthLength == 28))
		  stretches = generateStretches(shiftList.size()/8);
		else if(e.getShiftsAmount(previousShifts) == 16) {				// # of previous shifts from last odd # days (31 or 29) month
		  stretches = generateStretches(15);
		} else
		  stretches = generateStretches(16);
		
		  
		  for (int i = j; i <= shiftList.size() ; i++) {
			stretch = 0;
			for (int k = 0; ;  )
			if (stretch > 0 && shiftList.get(i).getEmployee() != null) {
			  shiftList.get(i).setEmployee(e);
			}
		  }
		j++;
	      }
	    
	  }
	/*  Shifts: 1 of 4 amounts of shifts are generated-
		1. 30 days * 4 shifts/day = 120 shifts, 120/8 = 15 shifts/employee; stretches = 7,8 each
		2. 31 days * 4 shifts/day = 124 shifts, 124/8 = 15 shifts for 4 employees, 16 for the rest; stretches = 7,8 for 4 employees, 8,8 for the rest
		3. 28 days * 4 shifts/day = 112 shifts, 112/8 = 14 shifts/employee; stretches = 7,7 each
		4. 29 days * 4 shifts/day = 116 shifts, 116/8 = 14 shifts for 4 employees, 15 for the rest; stretches = 7,7 for 4 employees, 7,8 for the rest
*/
	public int[] generateStretches(int shiftsAmount) {
		int [] stretches = new int[3];			// only 2-3 stretches per month
		stretches[2] = 0;						// for now, only 2 stretches allowed
		if (shiftsAmount == 15)
		  stretches[0] = (Math.random() < .5) ? 7 : 8;
		else								// shiftsAmount = 16
		  stretches[0] = 8;
		stretches[1] = shiftsAmount - stretches[0];
		return stretches;
	}


}





