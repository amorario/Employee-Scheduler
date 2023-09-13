package com.luv2code.springboot.thymeleafdemo.service;

import java.time.LocalDate;
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
		} else {
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

	@Override                                                // save a list of UNASSIGNED shifts
	public void saveMonthlyShifts(List<Shift> shiftList) {
		for (Shift s : shiftList)
			shiftRepository.save(s);
	}

	public void assignShiftsToEmployees(List<Shift> shiftList, List<Shift> lastMonthsList) {            // shiftList is a generated shift list WITHOUT employees
        
        List<Employee> employeeList = employeeRepository.findAllByOrderByLastNameAsc();
        
        //for (Employee e : employeeList)
            //System.out.println("ID= " + e.getId());
        
        Collections.shuffle(employeeList);
        int stretch, stretchNum;
        /*  Shifts: 1 of 4 amounts of shifts are generated-
        1. 30 days * 4 shifts/day = 120 shifts, 120/8 = 15 shifts/employee; stretches = 7,8 each
        2. 31 days * 4 shifts/day = 124 shifts, 124/8 = 15 shifts for 4 employees, 16 for the rest; stretches = 7,8 for 4 employees, 8,8 for the rest
        3. 28 days * 4 shifts/day = 112 shifts, 112/8 = 14 shifts/employee; stretches = 7,7 each
        4. 29 days * 4 shifts/day = 116 shifts, 116/8 = 14 shifts for 4 employees, 15 for the rest; stretches = 7,7 for 4 employees, 7,8 for the rest
        */
        boolean oddMonth = false;
        List<Shift> previousShifts = null;
        if (shiftList.size() == 116 || shiftList.size() == 124) {                                   // month has odd # of days
            oddMonth = true;
            int previousMonth = shiftList.get(0).getDate().minusMonths(1L).getMonthValue();        // WRONG - previous odd month CAN BE minus 1 month
            int previousYear = shiftList.get(0).getDate().minusMonths(1L).getYear();
            //previousShifts = getMonthlyShifts(previousMonth, previousYear);        // list of shifts of month that had odd # of days
        }

        int monthLength = shiftList.get(0).getDate().lengthOfMonth();        // current month length
        int [] e1Stretches, e2Stretches = new int[3];
        for (Employee e : employeeList) 
            e.setMonthSet(false);
        
        int start = 0;
        int iter;
        int[] hasMoreShifts = {0, 0, 0, 1, 0, 1, 1, 1};
                            // 0  1  2  3  4  5  6  7

        while (!checkIfAllSet(employeeList)) {
            iter = start;
            stretchNum = 0;
            Employee e1 = employeeList.get(iter);
            while (e1.isMonthSet()) {
                e1 = employeeList.get(++iter);
                start = iter;
            }

            Employee e2 = employeeList.get(++iter);
            // while (e2.isMonthSet() || (oddMonth && (e1.getShiftsAmount(previousShifts) == e2.getShiftsAmount(previousShifts)))) {
            while (e2.isMonthSet() || (oddMonth && (hasMoreShifts[iter] == hasMoreShifts[start]))) {
                e2 = employeeList.get(++iter);
            }

            int e1ShiftsAmount = shiftList.size() / 8;
            //if (oddMonth && (e1.getShiftsAmount(previousShifts) != 16 || (previousShifts.size() == 116 && e1.getShiftsAmount(previousShifts) != 15))) {
            if (oddMonth && hasMoreShifts[start] == 1) {
                e1ShiftsAmount++;       // Employee e1 did LESS shifts the last odd #'d month
            }
            e1Stretches = generateStretches(e1ShiftsAmount);
            e2Stretches = generateStretches(monthLength - e1ShiftsAmount);

            // Employee e1 and e2, e1 and e2 respective stretches [x,y,0], current/unassigned shiftList            
            int prevStretch = 0;
            if (lastMonthsList.get(0).getEmployee() != null) {
                prevStretch = checkShiftsInPriorMonth(e1.getId(), shiftList.get(0).getDate(), lastMonthsList);
                
                //System.out.println("prevStretch = " + prevStretch);
            }
            if (prevStretch == 0)
                prevStretch = (int) ((Math.random() * (8 - 3)) + 3);
            //int prevStretch = checkShiftsInPriorMonth(e1.getId(), shiftList.get(0).getDate());        // e1's current stretch from last month
            
            
            int max = e1Stretches[0]-prevStretch;
            stretch = (int) ((Math.random() * (max - 3)) + 3);
            if (stretch < e1Stretches[0])
                e1Stretches[0] = stretch;
            e1Stretches[2] = e1ShiftsAmount - (e1Stretches[0] + e1Stretches[1]);        //15 - (7 + 8) = 0
            
            
            System.out.println("E1 stretches generated: " + e1Stretches[0] + " " +e1Stretches[1] + " " +e1Stretches[2]);
            System.out.println("E2 stretches generated: " + e2Stretches[0] + " " +e2Stretches[1] + " " +e2Stretches[2]);
            
            Employee e = e1;
            int i = 0;
            while (!e1.isMonthSet() && !e2.isMonthSet()) {
                
                while (i < shiftList.size() && shiftList.get(i).getEmployee() != null) {                   // on to the next shift if this one is already assigned
                    i++;
                }
                
                while (stretch > 0) {
                    shiftList.get(i).setEmployee(e);
        
                    if (!(shiftList.get(i).getCall().equals("LATE")))
                        i += 4;                                        // get to the next day's shifts, unless its LATE call
                    i++;
                    stretch--;
                    if (i > shiftList.size()) 
                        break;
                }
                if (e.equals(e1)) {
                    e = e2;
                    if (stretchNum < 3)
                        stretch = e2Stretches[stretchNum];
                }
                else {
                    
                    e = e1;
                    stretchNum++;
                    if (stretchNum == 3) {
                      e1.setMonthSet(true);
                      e2.setMonthSet(true);
                    }
                    else 
                      stretch = e1Stretches[stretchNum];
                }
                

            }

        }
        System.out.println(shiftList.get(0).getDate().getMonth() + " is assigned!");
    }

	private boolean checkIfAllSet(List<Employee> employeeList) {
		for (Employee e : employeeList) {
			if (!e.isMonthSet())
				return false;
		}
		return true;
	}


	public int[] generateStretches(int shiftsAmount) {
		int [] stretches = new int[3];            // only 2-3 stretches per month
		stretches[2] = 0;                        // for now, only 2 stretches allowed
		if (shiftsAmount == 15)
			stretches[0] = (Math.random() < .5) ? 7 : 8;
		else                                // shiftsAmount = 16
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

	private int checkShiftsInPriorMonth(int id, LocalDate firstOfMonth, List<Shift> lastMonthsList) {
        // send user id and date to service method and return number of shifts worked in last 8 days prior to start of next month
        if (lastMonthsList.isEmpty())
            return 0;
        
        int daysOn = 0, shiftsOff = 0;
        int idFound = 0;
        Shift shiftFound = null;
        LocalDate minus8Days = firstOfMonth.minusDays(8);

        for (Shift s : lastMonthsList) {                // go through all until shift w/ target date is found
        //System.out.println(s);
            if (s.getDate().isEqual(minus8Days)) {
                shiftFound = s;
                idFound = shiftFound.getId();
                break;
            }
        }
        int shiftsToCheck = lastMonthsList.size() - idFound;
        //System.out.println("Shift found: " + shiftFound + "; amount to check = " + shiftsToCheck + ", ID to check " + id);
        for (int i = 1 ; i <= shiftsToCheck ; i++) {                // cycle through the next 8 days worth of shifts
            
            if (shiftFound.getEmployee().getId() == id) {
                daysOn++;
                if (!(shiftFound.getCall().equals("LATE"))) 
                    i+=4;
                shiftsOff = 0;
            }
            else {
                shiftsOff++;
                if (daysOn > 0)
                    shiftsOff += 4;
                daysOn = 0;
            }
            //System.out.println("ID found - " + shiftFound.getEmployee().getId() + ", stretch =  " + daysOn + " or days off = " + (shiftsOff/4) + ", i = " + i );
            if (i < shiftsToCheck)
                shiftFound = lastMonthsList.get(idFound+i);
        }

        return daysOn;
    }
}





