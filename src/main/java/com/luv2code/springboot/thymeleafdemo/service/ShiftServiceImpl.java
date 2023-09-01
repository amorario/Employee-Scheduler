package com.luv2code.springboot.thymeleafdemo.service;

import java.time.Month;
import java.util.ArrayList;
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

	@Override
	public List<Shift> getMonthlyShifts (int month, int year) {
		//allShifts = this.findAll();
		monthlyShifts.clear();
		for (Shift s : this.findAll()) {
			if ((s.getDate().getMonthValue() == month) && (s.getDate().getYear() == year))
				monthlyShifts.add(s);

			if (monthlyShifts.size() == 124)
				return monthlyShifts;

		}
		return monthlyShifts;
	}

	@Override												// save a list of UNASSIGNED shifts
	public void saveMonthlyShifts(List<Shift> shiftList) {
		for (Shift s : shiftList)
			shiftRepository.save(s);
	}

	public void assignShiftsToEmployees(List<Shift> shiftList) {			// shiftList is a generated shift list WITHOUT employees

		List<Employee> employeeList = employeeRepository.findAllByOrderByLastNameAsc();
		Collections.shuffle(employeeList);
		int stretch, stretchNum = 0;

		int previousMonth = shiftList.get(0).getDate().minusMonths(1L).getMonthValue();
		int previousYear = shiftList.get(0).getDate().minusMonths(1L).getYear();
		if ((previousMonth == 2) || (previousMonth == 4) || (previousMonth == 6) || (previousMonth == 9) || (previousMonth == 11))	{
			// need 2 months prior to current month
			previousMonth = shiftList.get(0).getDate().minusMonths(2L).getMonthValue();
			previousYear = shiftList.get(0).getDate().minusMonths(2L).getYear();
		}

		List<Shift> previousShifts = getMonthlyShifts(previousMonth, previousYear);		// list of shifts of month that had odd # of days
		int monthLength = shiftList.get(0).getDate().lengthOfMonth();		// current month length
		int [] stretches = new int[3];

		for (Employee e : employeeList) {
			if ((monthLength == 30) || (monthLength == 28))
				stretches = generateStretches(shiftList.size()/8);
			else if(e.getShiftsAmount(previousShifts) == 16) {				// # of previous shifts from last odd # days (31 or 29) month
				stretches = generateStretches(15);						// Employee e did more shifts the last odd #'d month
			} else
				stretches = generateStretches(16);						// Employee e did less shifts the last odd #'d month

			// Employee e, stretches [x,y,0], current/unassigned shiftList
			stretch = 0;
			for (int i = 0; i <= shiftList.size()-1 ;) {
				if (shiftList.get(i).getEmployee() != null)					// on to the next shift if this one is already assigned
					continue;
				stretch = stretch + stretches[stretchNum];
				while (stretch > 0) {
					shiftList.get(i).setEmployee(e);
					if (!(shiftList.get(i).getCall().equals("LATE")))
						i += 4;										// get to the next day's shifts, unless its LATE call

					i++;
					stretch--;

					if (shiftList.get(i).getEmployee() != null)
						break;
				}
				i = i + 28;										// 7 days later...
				stretchNum++;
			}

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

	public boolean shiftExists(Month m) {
		for (Shift s : shiftRepository.findAll()) {
			if (s.getDate().getMonth() == (m))
				return true;
		}
		return false;
	}

	private static int checkShiftsInPriorMonth (int id, LocalDate firstOfMonth) {
	  // send user id and month to service method and return number of shifts worked in  last 8 days prior to start of next month
	  // List<Shift> monthlyShifts = shiftService.getMonthlyShifts(currentMonth.getMonth().getValue(), currentMonth.getYear());
	  int stretch;
	  LocalDate minus8Days = firstOfMonth.minusDays(8);
	  
	  for (Shift s : this.findAll()) {
	    if (s.getDate().
	    
	  }
	  
	  
	  
	  
	}

}





