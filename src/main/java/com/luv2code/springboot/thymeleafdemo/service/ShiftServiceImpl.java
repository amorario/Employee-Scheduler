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

	public void assignShiftsToEmployees(List<Shift> shiftList) {            // shiftList is a generated shift list WITHOUT employees

		List<Employee> employeeList = employeeRepository.findAllByOrderByLastNameAsc();
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
            int previousMonth = shiftList.get(0).getDate().minusMonths(2L).getMonthValue();        // WRONG - previous odd month CAN BE minus 1 month
            int previousYear = shiftList.get(0).getDate().minusMonths(2L).getYear();
            //previousShifts = getMonthlyShifts(previousMonth, previousYear);        // list of shifts of month that had odd # of days
        }

        int monthLength = shiftList.get(0).getDate().lengthOfMonth();        // current month length
        int [] e1Stretches, e2Stretches = new int[3];
        for (Employee e : employeeList) {
            e.setMonthSet(false);
        }
        int start = 0;
        int iter;
        int[] hasMoreShifts = {1, 1, 0,1,0,0,1,0};

        while (!checkIfAllSet(employeeList)) {
            iter = start;
            stretchNum = 0;
            Employee e1 = employeeList.get(iter);
            while (e1.isMonthSet()) {
                e1 = employeeList.get(++iter);
                start = iter;
            }
            /*if (iter == 7)
                iter = 0;
            // System.out.println("e1 iter = " + iter);*/
            Employee e2 = employeeList.get(++iter);
            
                            
            // while (e2.isMonthSet() || (oddMonth && (e1.getShiftsAmount(previousShifts) == e2.getShiftsAmount(previousShifts)))) {
            while (e2.isMonthSet() || (oddMonth && (hasMoreShifts[iter] == hasMoreShifts[iter-1]))) {
                e2 = employeeList.get(++iter);
            }
            /*if (iter == 7)
                iter = 0;
            //System.out.println("e2 iter= " + iter);*/

            int e1ShiftsAmount = shiftList.size() / 8;
            //if (oddMonth && (e1.getShiftsAmount(previousShifts) != 16 || (previousShifts.size() == 116 && e1.getShiftsAmount(previousShifts) != 15))) {
            if (oddMonth && hasMoreShifts[iter-1] == 1) {
                e1ShiftsAmount++;       // Employee e1 did LESS shifts the last odd #'d month
            }
            e1Stretches = generateStretches(e1ShiftsAmount);
            e2Stretches = generateStretches(monthLength - e1ShiftsAmount);

            // Employee e1 and e2, e1 and e2 respective stretches [x,y,0], current/unassigned shiftList
            //int prevStretch = checkShiftsInPriorMonth(e1.getId(), shiftList.get(0).getDate());        // e1's current stretch from last month
            int prevStretch = 0;
            
            stretch = e1Stretches[0]-prevStretch;                                    // TODO- set to random int from 3-8
            if (stretch < e1Stretches[0])
                e1Stretches[0] = stretch;
            e1Stretches[2] = e1ShiftsAmount - (e1Stretches[0] + e1Stretches[1]);        //15 - (7 + 8) = 0
            Employee e = e1;
            int i = 0;
            while (!e1.isMonthSet() && !e2.isMonthSet()) {
                
                while (i < shiftList.size() && shiftList.get(i).getEmployee() != null) {                   // on to the next shift if this one is already assigned
                    i++;
                }
                
                while (stretch > 0) {
                    shiftList.get(i).setEmployee(e);
                   
                    
                    //System.out.println("Employee: " + shiftList.get(i).getEmployee().getFirstName() + " " + shiftList.get(i).getEmployee().getLastName() +", shift # = " + i + ", call = " + shiftList.get(i).getCall() + ", i = " + i+ ", stretch = " + stretch);
                     if (!(shiftList.get(i).getCall().equals("LATE")))
                        i += 4;                                        // get to the next day's shifts, unless its LATE call
                    i++;
                    stretch--;
                    //if (i > shiftList.size() || shiftList.get(i).getEmployee() != null) 
                    if (i > shiftList.size()) 
                        break;
                }
                if (e.equals(e1)) {
                    //System.out.println("\tSwitching to " + e2.getFirstName() + " " + e2.getLastName());
                    e = e2;
                    if (stretchNum < 3)
                        stretch = e2Stretches[stretchNum];
                }
                else {
                    
                    e = e1;
                    stretchNum++;
                    //System.out.println("\tSwitching to " + e1.getFirstName() + " " + e1.getLastName() + ", stretch # = " + stretchNum);
                    if (stretchNum == 3) {
                      e1.setMonthSet(true);
                      e2.setMonthSet(true);
                      //System.out.println("Done with e1 and e2");
                    }
                    else 
                      stretch = e1Stretches[stretchNum];
                }
                

            }

        }

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

	private int checkShiftsInPriorMonth(int id, LocalDate firstOfMonth) {
		// send user id and date to service method and return number of shifts worked in last 8 days prior to start of next month
		int stretch = 0;
		int idFound = 0;
		Shift shiftFound = null;
		LocalDate minus8Days = firstOfMonth.minusDays(8);

		for (Shift s : this.findAll()) {                // go through all until shift w/ target date is found
			if (s.getDate().isEqual(minus8Days)) {
				shiftFound = s;
				idFound = shiftFound.getId();
				break;
			}
		}
		for (int i = 0 ; i <32 ; i++) {                // cycle through the next 8 days worth of shifts
			if (shiftFound.getEmployee().getId() == id) {
				stretch++;
			}
			shiftFound = findById(idFound + i);
		}

		return stretch;
	}
}





