package com.luv2code.springboot.thymeleafdemo.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;


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

	public static int large, sumCount;

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


	public List<Shift> findAllByMonth(Month m) {
		List<Shift> monthlyList = new ArrayList<>();
		int count = 0;
		for (Shift s : shiftRepository.findAll()) {
			if (s.getDate().getMonth().equals(m)) {
				monthlyList.add(s);
				count++;
			}
			if (count > 124)
				break;
		}
		return monthlyList;
	}

	@Override
	public List<Employee> findAllEmployees() {
		return employeeRepository.findAll();
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
	public void saveWithEmployee(Shift theShift, int employeeId) {
		//get employee by id
		theShift.setEmployee(employeeRepository.getReferenceById(employeeId));

		//System.out.println(theShift.getEmployee());

		shiftRepository.save(theShift);
	}

	@Override
	public void save(Shift theShift) {
		shiftRepository.save(theShift);
	}

	@Override
	public void deleteById(int theId) {
		shiftRepository.deleteById(theId);
	}

	@Override
	public List<Shift> getMonthlyShifts (int month, int year) {
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

	@Override
	public void assignShiftsToEmployees(List<Shift> shiftList) {
		List<Employee> employeeListOriginal = new ArrayList<>(employeeRepository.findAll());
		for (Employee e : employeeListOriginal) {
			e.setMonthSet(false);
			e.setWeekends(0);
		}

		/*
		List<Integer> set = new ArrayList<>(Arrays.asList( 16, 16, 7, 18, 3, 18, 13, 16, 17 ));           // 16, 16, 7, 18, 3, 18, 13, 16, 17
		//int monthLength = (intListSum (set) )/ 4;

		String daysOff1 = "15 22 23 24 25 26";                        // Burrow - 16
		String daysOff2 = "27 28 29 30 31";                            // Chase - 16
		String daysOff3 = "1 2 3 4 5 6 7 8 9 10 11 18";         // Jacobs - 7
		String daysOff4 = "1 2 3 21 22 23 24 25 26";               // Pierce - 18
		String daysOff5 = "1";                                              // Jefferson - 3
		String daysOff6 = "28 29 30 31";                                // Smith - 18
		String daysOff7 = "27 28 29 30 31";                            // Goedert - 13
		String daysOff8 = "29 30 31";                                    // Bass - 16
		String daysOff9 = "11 18 22 23 24 25 26 27";                // Evans - 17
		List<String> groupDaysOff = new ArrayList<>(Arrays.asList(daysOff1,daysOff2,
				daysOff3,daysOff4,daysOff5,daysOff6,daysOff7,daysOff8,daysOff9));

		for (int i = 0; i <9 ; i++) {
			employeeListOriginal.get(i).setShiftsAmount(set.get(i));
			employeeListOriginal.get(i).setDaysOff(groupDaysOff.get(i));
		}*/


		List<Employee> employeeList = new ArrayList<>(employeeListOriginal);
		Collections.shuffle(employeeList);
		List<List<Employee>> groupList = new ArrayList<List<Employee>>();


		groupList = eGroupings(employeeList, shiftList);


		shiftList.get(0).getDate().getMonthValue();
		int weekends = getNumberOfWeekendsInMonth(shiftList);
		int weekendsLimit = 2;
		if (weekends == 5)
			weekendsLimit = 3;

		for (List<Employee> eList : groupList) {
			List<Integer> intShiftsList = getGroupShifts(shiftList);            // integer list of unassigned daily shift ID's

			//System.out.println("intShiftList[3] = " + intShiftsList.get(3) + ", shift in shiftList at intShiftList[3] " + shiftList.get(intShiftsList.indexOf(15)));

			Iterator<Employee> itr = eList.iterator();
			Employee e = itr.next();
			boolean isWeekend = false;

			for (int i : intShiftsList) {                                       // assign days off requested first
				if (isWeekend) {
					isWeekend = false;
					continue;
				}
				if (getGroupDaysOff(eList).contains(shiftList.get(i).getDate().getDayOfMonth())) {

					while (shiftList.get(i).getEmployee() == null) {
						if (dayOpen(e, shiftList.get(i).getDate().getDayOfMonth())) {
							assign(e, shiftList.get(i), shiftList);

                            /*//shiftList.get(i).setEmployee(e);

							if (shiftList.get(i).getDate().getDayOfWeek() == DayOfWeek.SATURDAY) {                // automatically assign Saturday and Sunday, no mixing
								//assignNext(shiftList.get(i), e, shiftList);
								isWeekend = true;
							}
							//if (shiftList.get(i).getDate().getDayOfWeek() == DayOfWeek.SUNDAY) {                // automatically assign Saturday and Sunday, no mixing
								//assignPrevious(shiftList.get(i), e, shiftList);
							//}
							//e.setShiftsAmount(e.getShiftsAmount() - 1);*/
						} else {
							if (itr.hasNext())
								e = itr.next();
							else
								itr = eList.iterator();
						}
					}
				}
			}

			for (List<Integer> stretches : getStretches(shiftList, intShiftsList)) {         // assign the remaining empty shift slots

				if (stretches.get(1) == 0) {
					int remaining = stretches.get(0);
					int count = 0;
					while (remaining > 0 && count < 5) {
						for (Employee emp : eList) {
							int tempStretch = 0, divisor = 1;
							if (emp.getShiftsAmount() > 16)
								divisor = 3;
							else if (emp.getShiftsAmount() > 8)
								divisor = 2;
							tempStretch = emp.getShiftsAmount() / divisor;
							remaining -= tempStretch;
							if (remaining <= 2) {
								tempStretch += remaining;
								remaining = 0;
							}

							if (tempStretch >= 6 && emp.getWeekends() == (weekendsLimit -1)) {
								tempStretch += daysUntilWeekend(shiftList.get(intShiftsList.get(intShiftsList.indexOf(stretches.get(2)))).getDate(), tempStretch);
								//System.out.println("Tempstretch = " + tempStretch);
								if (tempStretch > 8)
									tempStretch = 8;
							}

							//System.out.println("remaining = " + remaining + ", tempStretch = " + tempStretch);
							count++;
							//System.out.println("intShiftList = " + intShiftsList);

							for (int i = intShiftsList.indexOf(stretches.get(2)) ; tempStretch > 0 ; tempStretch--) {
								//System.out.println("i = " + i);							// i = index in intShiftList
								if ((isWeekend(shiftList.get(intShiftsList.get(i))) && emp.getWeekends() >= weekendsLimit)
										||!dayOpen(emp, shiftList.get(intShiftsList.get(i)).getDate().getDayOfMonth())
										|| emp.getShiftsAmount() <= 0){
									//System.out.println("weekends limit reached for " + emp.getLastName() + ", weekends working = " + emp.getWeekends() + ", weekendsLimit = " + weekendsLimit);
									remaining = tempStretch;
									tempStretch = 0;
									stretches.set(2, intShiftsList.get(i));
									break;
								}
								assign(emp, shiftList.get(intShiftsList.get(i)), shiftList);
								//System.out.println("Assigning " + emp.getLastName() + " to shift " + shiftList.get(intShiftsList.get(i)) + ", weekends = " + emp.getWeekends());

								if (isWeekend(shiftList.get(intShiftsList.get(i)))) {              // automatically assign Saturday and Sunday, no mixing
									tempStretch--;
									remaining--;
									stretches.set(2, intShiftsList.get(++i));
								}
								stretches.set(2, intShiftsList.get(++i));
							}
						}
					}
				}
			}
			for (Employee emp : eList) {
				emp.setMonthSet(true);
				employeeList.remove(emp);
			}
		}			// Assign pairs whose shift amounts add up to the month's length



		//while (!checkIfAllSet(employeeList)) {
		for (int k = 0 ; k < 4 ; k++){                  // k = number of guys to assign
			sortScheduleDifficulty(employeeList, (shiftList.size()/4));
			//System.out.println("employeeList: " + employeeList);
			int eIndex = (Math.random() < .75) ? 0 : 1;                 // select next most difficult schedule, with 25% chance to select 2nd most
			Employee e = employeeList.get(0);
			if (employeeList.size() > 1)
				e = employeeList.get(eIndex);                          // chance to randomly get 2nd highest difficult schedule

			List<Integer> intShiftsList = bestFit(shiftList, e);
			Employee e2 = findPair (e, employeeList);

			int stretch = 0;
			int openDays = 0;

			if (k<2)
				assignPairs (e, e2, intShiftsList, getStretches(shiftList, intShiftsList), shiftList);

			//printIntShiftList(getStretches(shiftList, intShiftsList), employeeListOriginal);

			for (int i = 0; i < intShiftsList.size() ; i++) {           // iterate through intShiftList
				if (i == 0)
					stretch = getPriorMonthsShifts(e, shiftList.get(0).getDate());
				else
					stretch = 0;
				openDays = getShiftsUntilOff(intShiftsList, e, i, shiftList);
				if ((openDays < 3) || ((openDays < e.getShiftsAmount()) && e.getShiftsAmount() <= 5) ) {
					i+= openDays;
					continue;
				}

				if ((e.getShiftsAmount() <= 8) && (openDays > e.getShiftsAmount())) {       // append the shift index so that to prevent 1, 2 day stretches near month end
					i += openDays - e.getShiftsAmount();
				}

				if ((e.getShiftsAmount() == 9) && (openDays > 6))
					openDays = 5;

				if ((openDays > 7) && (e.getShiftsAmount() >= 11)) {                        // a lot of days left to assign, a lot of shifts left for employee e
					openDays = (int) (Math.random() * 2) + 6;                               // generate random first stretch amount between 6 to 8 shifts
				}



				//System.out.println("Employee: " + e.getLastName() + ", shifts left: " + e.getShiftsAmount());
				//System.out.println("openDays: " + openDays + ", stretch: " + stretch);
				//System.out.println("intShiftList: " + intShiftsList);
				for (int j = 0; j < openDays ; j++) {
					if (//dayOpen(e, shiftList.get(intShiftsList.get(i)).getDate().getDayOfMonth()) &&
							(i+j) < intShiftsList.size() &&
							(shiftList.get(intShiftsList.get(i+j)).getEmployee() == null) &&
									stretch < 7
									&& e.getShiftsAmount() > 0) {
						assign(e, shiftList.get(intShiftsList.get(i+j)), shiftList);
						//System.out.println("Assigning : " + e.getLastName() + " to shift " + shiftList.get(intShiftsList.get(i+j)));
						//System.out.println("j: " + j + ", i: " + i);
						if (shiftList.get(intShiftsList.get(i+j)).getDate().getDayOfWeek() == DayOfWeek.SATURDAY) {
							stretch++;
							j++;
						}

						stretch++;
						//System.out.println("Assigning employee: " + e.getLastName() + ", current stretch = " + stretch );
					}

				}
				i += stretch + 2;

			}
			//System.out.println("getStretches() after assignment: " + getStretches(shiftList, intShiftsList));
			//printIntShiftList(getStretches(shiftList, intShiftsList), employeeListOriginal);
			//System.out.println("employeeList size: " + employeeList.size());
			if (e.getShiftsAmount() == 0) {
				e.setMonthSet(true);

			}
			employeeList.remove(e);
			employeeList.remove(e2);

		}
		//System.out.println("employeeList: " + employeeListOriginal + "\n");
		for (int i = 0; i < 4 ; i++) {
			printIntShiftList(getStretches(shiftList, getIntShiftsList(shiftList, i)), employeeListOriginal);
		}
	}

	@Override
	public void clearMonthlyShifts(List<Shift> monthlyShifts) {
		for (Shift s : monthlyShifts)
			s.setEmployee(null);
		if (monthlyShifts.get(0).getEmployee() == null)
			System.out.println("CLEAR!");
	}

	@Override
	public boolean isUnassigned(List<Shift> monthlyShifts) {
		for (Shift s : monthlyShifts) {
			if (s.getEmployee() != null) {
				System.out.println("Employee found in " + s.getDate().getMonth() + ", IT'S ASSIGNED");
				return false;
			}
		}
		return true;
	}

	@Override
	public void updateEmployeeList(List<Employee> wrapperList) {
		List<Employee> employeeList = employeeRepository.findAll();
		for (int i = 0 ; i < wrapperList.size() ; i++) {
			employeeList.get(i).setShiftsAmount(wrapperList.get(i).getShiftsAmount());
			employeeList.get(i).setDaysOff(wrapperList.get(i).getDaysOff());
		}
		employeeRepository.saveAll(employeeList);

		for (Employee e : employeeRepository.findAll())
			System.out.println(e);
	}


	static List<Integer> getIntShiftsList (List<Shift> shiftList, int start) {
		List<Integer> list = new ArrayList<Integer>();
		for (int i= start ; i < shiftList.size() ; i++ ) {
			list.add(i);
			if (!(shiftList.get(i).getCall().equals("LATE")))
				i += 4;                                        // get to the next day's shifts, unless its LATE cal
		}
		return list;
	}

	static Employee findPair (Employee emp, List<Employee> employeeList) {            // find the best employee w/ a schedule that matches with emp's schedule
		Employee pair = new Employee();
		int compatibility;
		int tempHigh = 0;
		for (Employee e : employeeList) {
			if (e.equals(emp) || e.getShiftsAmount() < 10)
				continue;
			compatibility = 0;
			List<Integer> empDaysOff = emp.getDaysOffIntList();
			for (int eDayOff : e.getDaysOffIntList()) {
				if (empDaysOff.contains(eDayOff))
					compatibility -= 3;
				else
					compatibility++;
			}

			if (compatibility > tempHigh) {
				pair = e;
				tempHigh = compatibility;
			}
		}
		return pair;
	}

	static void assignPairs (Employee e1, Employee e2, List<Integer> intShiftsList, List<List<Integer>> stretches, List<Shift> shiftList) {
		List<List<Integer>> e1Stretches = getStretchesWithDaysOff(stretches, e1, intShiftsList);
		List<List<Integer>> e2Stretches = getStretchesWithDaysOff(stretches, e2, intShiftsList);
		//System.out.println(e1.getFirstName() +" shifts amount = " + e1.getShiftsAmount()+ ", " +e2.getFirstName() +" shifts amount = " + e2.getShiftsAmount());
		int stretchLength = 0;
		int index = 0;

		for (List<Integer> stretch : e1Stretches) {

			stretchLength = stretch.get(0);
			if (stretch.get(1) == 10) {
				for (int i = 0 ; i < stretchLength  ; i++) {
					assign(e2, shiftList.get(intShiftsList.get(index + i)), shiftList);
					if (isWeekend(shiftList.get(intShiftsList.get(index + i))))
						i++;
					//System.out.println(e2.getFirstName() +" shifts amount = " + e2.getShiftsAmount());
				}
			}
			index += stretchLength;

		}
		index = 0;
		for (List<Integer> stretch : e2Stretches) {

			stretchLength = stretch.get(0);
			if (stretch.get(1) == 10) {
				for (int i = 0 ; i < stretchLength; i++) {
					assign(e1, shiftList.get(intShiftsList.get(index + i)), shiftList);
					if (isWeekend(shiftList.get(intShiftsList.get(index + i))))
						i++;
				}
			}
			index += stretchLength;
		}
		stretches = getStretches(shiftList, intShiftsList);

		// Assign 3-day bubbles

		int prior = stretches.get(0).get(1);
		int after = stretches.get(2).get(1);
		stretchLength = stretches.get(0).get(0);
		index = stretchLength + stretches.get(1).get(0);
		Employee temp = e1;

		for (int i = 1;  i < (stretches.size() - 1); i++) {         // i = stretch index in stretch list
			stretchLength = stretches.get(i).get(0);
			prior = stretches.get(i-1).get(1);
			after = stretches.get(i+1).get(1);
			if ((stretches.get(i).get(0) == 3)
					&& (prior == after)
					&& (stretches.get(i).get(1) == 0)) {
				if (prior == e1.getId())
					temp = e2;
				else
					temp = e1;

				for (int j = 0 ; j < stretches.get(i).get(0); j++) {
					assign(temp, shiftList.get(intShiftsList.get(index + j -1)), shiftList);
					if (isWeekend(shiftList.get(intShiftsList.get(index + j -1))))
						j++;
				}
			}

			index += stretchLength;
		}
		stretches = getStretches(shiftList, intShiftsList);
		index = 0;

		// Append covered days to at least 3 day stretches
		for (int i = 0;  i < stretches.size(); i++) {       // i = stretch index in stretch list
			stretchLength = stretches.get(i).get(0);
			if  ((stretches.get(i).get(0) < 3)&& (stretches.get(i).get(1) != 0)) {
				if (stretches.get(i).get(1) == e1.getId())
					temp = e1;
				else
					temp = e2;

				if ( (i != stretches.size() -1)
						&& stretches.get(i+1).get(1) == 0
						&& stretches.get(i+1).get(0) > 2 ) {
					for (int j = 1 ; j < 3; j++) {
						assign(temp, shiftList.get(intShiftsList.get(index + j)), shiftList);
						if (isWeekend(shiftList.get(intShiftsList.get(index + j))))
							j++;
					}
				} else if ( (i != 0)
						&& stretches.get(i-1).get(1) == 0
						&& stretches.get(i-1).get(0) > 2 ) {
					for (int j = 1 ; j < 3; j++) {
						assign(temp, shiftList.get(intShiftsList.get(index - j)), shiftList);
						if (isWeekend(shiftList.get(intShiftsList.get(index - j))))
							j++;
					}
				}
			}
			index += stretchLength;
		}
		stretches = getStretches(shiftList, intShiftsList);


		// Fill in stretches that are < 3 days
		prior = stretches.get(0).get(1);    //
		after = stretches.get(1).get(1);    //
		stretchLength = stretches.get(0).get(0);
		index = 0;
		temp = e1;

		for (int i = 0;  i < stretches.size(); i++) {         // i = stretch index in stretch list
			if (i != stretches.size() - 1)
				after = stretches.get(i+1).get(1);
			if (i == 0 && prior == 0)
				prior = after;

			stretchLength = stretches.get(i).get(0);
			if ((stretches.get(i).get(1) == 0) && (stretches.get(i).get(0) < 3)) {
				if (prior == e1.getId())
					temp = e1;
				else
					temp = e2;

				for (int j = 0 ; j < stretches.get(i).get(0); j++) {
					assign(temp, shiftList.get(intShiftsList.get(index + j)), shiftList);
					if (isWeekend(shiftList.get(intShiftsList.get(index + j))))
						j++;
				}
			}
			prior = stretches.get(i).get(1);
			index += stretchLength;
		}

		stretches = getStretches(shiftList, intShiftsList);


		// Fill in the rest of intShiftList
		prior = stretches.get(0).get(1);
		after = stretches.get(1).get(1);
		stretchLength = stretches.get(0).get(0);
		int length = 0;
		index = 0;
		//System.out.println("e1ShiftAmount = " + e1.getShiftsAmount()+ ", e2ShiftAmount = " + e2.getShiftsAmount());
		for (int i = 0;  i < stretches.size(); i++) {         // i = stretch index in stretch list
			if (i != stretches.size() - 1)
				after = stretches.get(i+1).get(1);
			if (i == 0 && prior == 0)
				prior = after;
			stretchLength = stretches.get(i).get(0);
			if (stretches.get(i).get(1) == 0) {
				while (stretchLength > 0) {
					if (prior == e1.getId()) {
						temp = e1;
						prior = e2.getId();
					} else {
						temp = e2;
						prior = e1.getId();
					}
					length = stretchLength;
					if (stretchLength > 9)
						length = 7;
					if (i != 0)
						length = 8-stretches.get(i-1).get(0);
					if (length > temp.getShiftsAmount())
						length = temp.getShiftsAmount();
					//System.out.println("tempShiftAmount = " + e2.getShiftsAmount()+ ", Length = " + length);
					//System.out.println("temp = " + temp.getFirstName()+ ", Length = " + length);
					for (int j = 0 ; j < length; j++) {
						assign(temp, shiftList.get(intShiftsList.get(index + j)), shiftList);
						if (isWeekend(shiftList.get(intShiftsList.get(index + j))))
							j++;
					}
					stretchLength -= length;
					index += length;
				}
			}
			prior = stretches.get(i).get(1);
			index += stretchLength;
		}
		stretches = getStretches(shiftList, intShiftsList);
		//System.out.println("getStretches: " + stretches);
		//System.out.println(e1.getFirstName() +" shifts amount = " + e1.getShiftsAmount()+ ", " +e2.getFirstName() +" shifts amount = " + e2.getShiftsAmount());
		//System.out.println("e1 weekends = " + (e1.getWeekends() / 2) + ", e2 weekends = " + (e2.getWeekends() /2));

	}

	static boolean isWeekend (Shift s) {
		if (s.getDate().getDayOfWeek() == DayOfWeek.SATURDAY || s.getDate().getDayOfWeek() == DayOfWeek.SUNDAY)
			return true;
		return false;
	}

	static int daysUntilWeekend(LocalDate date, int tempStretch) {
		Long append = (long) tempStretch;
		date = date.plusDays(append);
		int daysUntil = 0;
		append = 1L;
		while (!isWeekend(date)) {
			daysUntil++;
			date = date.plusDays(append);
		}
		return daysUntil;
	}

	private static boolean isWeekend(LocalDate date) {
		if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY)
			return true;
		return false;
	}

	static List<List<Integer>> getStretchesWithDaysOff (List<List<Integer>> stretches, Employee e, List<Integer> intShiftsList) {    // like getStretches(), obtain a list of stretches but factor in days off requested
		List<List<Integer>> newStretches = new ArrayList<List<Integer>>();
		int day = 1;
		int daysOff = 0;
		int status = 0;
		int length = 0;
		int start = 0;
		for (List<Integer> stretch : stretches) {       //stretches = [[31, 0, 0]] --> [[10, 0, 0], [1, 10], [6, 0, start], [1, 10], [3, 0, start], [6, 10], [4, 0, start]]
			if (stretch.get(1) == 0) {                  // empty/available stretch

				status = (dayOpen(e, day)) ? 1 : 0;
				start = day - 1;
				for (int i = 1 ; i <= stretch.get(0) ; i++) {   // iterate through each day in the stretch

					if (!e.getDaysOffIntList().contains(day)) {
						if (status == 0) {
							newStretches.add(new ArrayList<>(Arrays.asList(daysOff, 10)));
							status = 1;
							daysOff= 0;
							start = day -1;
						}
						length++;
					} else {  // found a requested day off
						if (status == 1) {
							newStretches.add(new ArrayList<>(Arrays.asList(length, 0, intShiftsList.get(start))));
							status = 0;
							length = 0;
							start = day- 1;
						}
						daysOff++;
					}
					day++;
				}
				if (status == 0)
					newStretches.add(new ArrayList<>(Arrays.asList(daysOff, 10)));
				if (status == 1)
					newStretches.add(new ArrayList<>(Arrays.asList(length, 0, intShiftsList.get(start))));


			} else {// add stretch as is since it is already allocated by another employee
				newStretches.add(new ArrayList<>(stretch));
				start = intShiftsList.get(stretch.get(0)-1);
				day += stretch.get(0);
				length = 0;
			}
		}

		return newStretches;
	}


	static void assign(Employee e, Shift s, List<Shift> shiftList) {
		s.setEmployee(e);
		if (s.getDate().getDayOfWeek() == DayOfWeek.SATURDAY) {                // automatically assign Saturday and Sunday, no mixing
			//System.out.println("Saturday found: " + s);
			assignNext(s, e, shiftList);
		}
		if (s.getDate().getDayOfWeek() == DayOfWeek.SUNDAY) {                // automatically assign Saturday and Sunday, no mixing
			assignPrevious(s, e, shiftList);
		}
		e.setShiftsAmount(e.getShiftsAmount() - 1);
	}

	static void printIntShiftList(List<List<Integer>> stretches, List<Employee> employeeList) {
		System.out.print("Stretches: ");
		int total = 0;
		for (List<Integer> stretch : stretches) {
			if (stretch.get(1) != 0) {
				total += stretch.get(0);
				System.out.print("[" + stretch.get(0) + " days, " + employeeList.get(stretch.get(1) -1).getFirstName() + "] ");
			} else {
				System.out.print("*" + stretch.get(0) + " days unassigned* ");
			}
		}
		System.out.println("- Assigned shifts = " + total);
	}

	static boolean dayOpen(Employee e, int day) {
		if (e.getDaysOffIntList().contains(day))
			return false;

		return true;
	}

	static List<Integer> getGroupShifts (List<Shift> shiftList) {                          // obtain different shift types for everyday via a list of shift id's
		int start = 0;
		while (start < 4 && shiftList.get(start).getEmployee() != null)                    // on to the next shift if this one is already assigned
			start++;
		if (start == 4)
			start--;
		List<Integer> list = new ArrayList<>();
		for (int i= start ; i < shiftList.size() ; i++ ) {
			list.add(i);
			if (!(shiftList.get(i).getCall().equals("LATE")))
				i += 4;                                        // get to the next day's shifts, unless its LATE cal
		}
		return list;
	}

	static List<Integer> getGroupDaysOff (List<Employee> eList) {
		List<Integer> daysOff= new ArrayList<>();
		for (Employee e : eList) {
			for (int i : e.getDaysOffIntList()) {
				daysOff.add(i);
			}
		}
		Collections.sort(daysOff);
		return daysOff;
	}

	static List<List<Integer>> getStretches (List<Shift> shiftList, List<Integer> intShiftsList) {

		List<List<Integer>> stretches = new ArrayList<List<Integer>>();
		int length = 0;
		Employee temp;
		if (shiftList.get(intShiftsList.get(0)).getEmployee() != null)
			temp = shiftList.get(intShiftsList.get(0)).getEmployee();
		else
			temp = null;

		int nullStart = 0;

		for (int i : intShiftsList) {
			Shift s = shiftList.get(i);
			if ((s.getEmployee() != null) && (s.getEmployee().equals(temp)))       // found the same employee
				length++;
			else {
				if (s.getEmployee() != null) {                                      // found different employee
					if (temp != null)
						stretches.add(new ArrayList<>(Arrays.asList(length, temp.getId())));
					else
						stretches.add(new ArrayList<>(Arrays.asList(length, 0, nullStart)));
					temp = s.getEmployee();
					length = 1;
				} else {                                                              // found empty/null employee
					if (temp != null) {
						stretches.add(new ArrayList<>(Arrays.asList(length, temp.getId())));
						temp = null;
						length = 1;
						nullStart = i;
					} else
						length++;

				}

			}
			if (intShiftsList.indexOf(i) == (intShiftsList.size() -1)) {             // if at the end of the shift list
				if (temp != null)
					stretches.add(new ArrayList<>(Arrays.asList(length, temp.getId())));
				else
					stretches.add(new ArrayList<>(Arrays.asList(length, 0, nullStart)));
			}
		}
		//System.out.println(stretches);                      // stretches = [assign/unassigned stretch from daysOff, 0 if unassigned, assigned employee ID# or shift ID# to start at]
		return stretches;
	}


	static void assignNext(Shift s, Employee e, List<Shift> shiftList) {
		int shiftId = shiftList.indexOf(s);
		if (!s.getCall().equals("LATE"))                         // LATE call on a Saturday
			shiftId += 4;

		shiftId++;
		//System.out.println("shiftId = " +shiftId);
		shiftList.get(shiftId).setEmployee(e);
		e.setShiftsAmount(e.getShiftsAmount() - 1);
		e.setWeekends(e.getWeekends()+1);
	}

	static void assignPrevious(Shift s, Employee e, List<Shift> shiftList) {
		int shiftId = shiftList.indexOf(s);
		if (!s.getCall().equals("NO"))                         // NO call on a Sunday
			shiftId -= 4;
		shiftId--;
		//System.out.println("shiftId = " +shiftId);
		shiftList.get(shiftId).setEmployee(e);
		e.setShiftsAmount(e.getShiftsAmount() - 1);
		e.setWeekends(e.getWeekends()+1);
	}

	static int getNumberOfWeekendsInMonth (List<Shift> shiftList) {
		int weekends =0;
		//System.out.println("shiftList size = " + shiftList.size());
		for (int i = 0 ; i < shiftList.size() ;) {
			//System.out.println("Shift date day: " + shiftList.get(i).getDate().getDayOfWeek());
			if ((shiftList.get(i).getDate().getDayOfWeek() == DayOfWeek.SATURDAY) || (shiftList.get(i).getDate().getDayOfWeek() == DayOfWeek.SUNDAY)) {
				weekends++;

			}
			i += 4;
		}
		//System.out.println("# of weekend days = " + weekends);
		return weekends /2;
	}

	static int getShiftsUntilOff(List<Integer> intShiftsList, Employee e, int start, List<Shift> shiftList) {
		int openDays = 0;
		while (start < intShiftsList.size()
				&& dayOpen(e, shiftList.get(intShiftsList.get(start)).getDate().getDayOfMonth())
				&& shiftList.get(intShiftsList.get(start)).getEmployee() == null) {
			openDays++;
			start++;
		}
		return openDays;
	}

	/*************************************************************************************************************************************************************************************/

	static List<Integer> bestFit(List<Shift> shiftList, Employee e) {

		List<Integer> list = new ArrayList<>();

		int start = 3;
		int temp = (shiftList.size() / 4) - e.getDaysOffIntList().size();                       // length of month = max openings

		if (e.getShiftsAmount() < 8) {                               // find a single stretch w/ a weekend
			for (int i = 3; i >= 0 ; i--) {
				int open = 0;
				int weekendDays = 0;
				for (int j= i ; j < shiftList.size() ; j++ ) {


					if ((shiftList.get(j).getEmployee() == null) && dayOpen(e, shiftList.get(j).getDate().getDayOfMonth())) {
						open++;
						DayOfWeek dayOfWeek = shiftList.get(j).getDate().getDayOfWeek();
						if ((dayOfWeek == DayOfWeek.SATURDAY) || (dayOfWeek == DayOfWeek.SUNDAY))
							weekendDays++;
					}
					//System.out.println("Looking for weekends");
					else {
						open = 0;
						weekendDays = 0;
					}

					if ((open >= e.getShiftsAmount()) && (weekendDays == 2)) {
						start = i;
						//System.out.println("Weekend stretch found for  " + e.getFirstName() + ", starting at " + (shiftList.get(j).getDate().getDayOfMonth() - open));
						for (int k= start ; k < shiftList.size() ; k++ ) {
							list.add(k);
							if (!(shiftList.get(k).getCall().equals("LATE")))
								k += 4;                                        // get to the next day's shifts, unless its LATE cal
						}
						return list;
						//break;
					}
					if (!(shiftList.get(j).getCall().equals("LATE")))
						j += 4;                                        // get to the next day's shifts, unless its LATE cal
				}/*
                if ((open >= e.getShiftsAmount()) && (weekendDays == 2))
                    break;*/
			}
		}

		for (int i = 3; i >= 0 ; i--) {
			int open = 0;
			for (int j= i ; j < shiftList.size() ; j++ ) {
				if ((shiftList.get(j).getEmployee() == null) && dayOpen(e, shiftList.get(j).getDate().getDayOfMonth()))
					open++;
				if (!(shiftList.get(j).getCall().equals("LATE")))
					j += 4;                                        // get to the next day's shifts, unless its LATE cal
			}
			//System.out.println("start = " + start + ", open days = " + open + ", temp (low) = " + temp);


			// ************************************************VITAL FACTOR***************************************************************


			if (((open <= temp) && (open > (e.getShiftsAmount() -5))) || ((e.getShiftsAmount() < 8) && (open >= e.getShiftsAmount()))) {
				start = i;
				temp = open;
				//System.out.println("start = " + start + ", open days = " + open + ", temp (low) = " + temp);
			}

			// ************************************************VITAL FACTOR***************************************************************

		}

		for (int i= start ; i < shiftList.size() ; i++ ) {
			list.add(i);
			if (!(shiftList.get(i).getCall().equals("LATE")))
				i += 4;                                        // get to the next day's shifts, unless its LATE cal
		}
		return list;
	}

	static boolean isSubsetSum(List<Integer> set, int n, int sum) {
		sumCount++;
		if (sum == 0){
			large = set.get(n);
			return true;
		}

		if (n == 0)
			return false;

		if (set.get(n-1) > sum)
			return isSubsetSum(set, n - 1, sum);

		return isSubsetSum(set, n - 1, sum) || isSubsetSum(set, n - 1, sum - set.get(n-1));
	}

	static int intListSum (List<Integer> set) {
		int sum= 0;
		for (int i : set)
			sum += i;
		return sum;
	}


	static List<List<Employee>> eGroupings(List<Employee> list, List<Shift> shiftList) {
		//sortShiftsAmountDescending(list);
		sortScheduleDifficulty(list, (shiftList.size()/4));
		List<Integer> set = new ArrayList<>();
		List<Employee> eList = new ArrayList<>();
		List<List<Employee>> groupList = new ArrayList<List<Employee>>();
		int sum = 0;
		for (Employee e : list) {
			int amount = e.getShiftsAmount();
			sum += amount;
			set.add(amount);    // set = integer list of shift amounts in descending order
		}
		sum = sum/4;            // sum == # of days in month
		int n = set.size();

		while (isSubsetSum(set, n, sum) == true) {
			eList.clear();
			set.remove(Integer.valueOf(large));
			int eListSum = 0;
			//System.out.println("List before: " + list);
			for (Employee e : list) {
				if (e.getShiftsAmount() == large) {
					eList.add(e);
					eListSum = large;
					list.remove(e);
					//System.out.println(e);
					break;
				}
			}
			while (eListSum != sum) {
				int tempSum = eListSum;

				for (int i : set) {
					if ((tempSum + i) <= sum) {
						eListSum += i;
						for (Employee e : list) {
							if (e.getShiftsAmount() == i && checkRequests(eList, e)) {
								eList.add(e);
								list.remove(e);
								break;
							}
						}
						if (eList.size() >= 2)
							set.remove(Integer.valueOf(i));
						break;

					}
				}
			}
			eListSum = 0;
			for (Employee e : eList)
				eListSum += e.getShiftsAmount();
			if (eListSum == sum)
				groupList.add(new ArrayList<>(eList));
			else {
				for (Employee e : eList)
					list.add(e);
			}
			//System.out.println("List after: " + list);
			n = set.size();
		}
		if (isSubsetSum(set, n, sum) == false) {
			//System.out.println("No subset with given sum");
			//System.out.println("\tEmployees left :" + list.size() + "\n\t" + list);
		}

		return groupList;
	}

	static boolean checkRequests(List<Employee> eList, Employee emp) {
		for (Employee e : eList) {
			for(int i : emp.getDaysOffIntList()) {
				if (e.getDaysOffIntList().contains(i))
					return false;
			}
		}
		return true;
	}

	static void sortShiftsAmountDescending (List<Employee> list) {
		list.sort((Employee o1, Employee o2) -> Integer.compare(o2.getShiftsAmount(), o1.getShiftsAmount()));
	}

	static void sortScheduleDifficulty (List<Employee> list, int monthLength) {
		list.sort((Employee o1, Employee o2) -> Double.compare(scheduleDifficulty(o2, monthLength), scheduleDifficulty(o1, monthLength)));
	}

	static int getMonthLengthFromEmployeeList (List<Employee> list) {
		int sum = 0;
		for (Employee e : list) {
			sum += e.getShiftsAmount();
		}
		return sum/4;
	}

	static double scheduleDifficulty(Employee e, int monthLength) {
		return (double) e.getShiftsAmount() / (monthLength - e.getDaysOffIntList().size());
	}

	public int getPriorMonthsShifts(Employee e, LocalDate firstOfMonth) {
		// send user id and date to service method and return number of shifts worked in last 8 days prior to start of next month
		List<Shift> lastMonthsList = findAllByMonth(firstOfMonth.minusMonths(1L).getMonth());
		if (lastMonthsList == null)
			return 0;

		int daysOn = 0, shiftsOff = 0;
		int idFound = 0;
		Shift shiftFound = null;
		LocalDate minus8Days = firstOfMonth.minusDays(8);

		for (Shift s : lastMonthsList) {                // go through all until shift w/ target date is found
			//System.out.println(s);
			if (s.getDate().isEqual(minus8Days)) {
				shiftFound = s;
				idFound = lastMonthsList.indexOf(s);
				break;
			}
		}
		//int shiftsToCheck = lastMonthsList.size() - idFound;
		//System.out.println("Shift found: " + shiftFound + "; amount to check = " + shiftsToCheck + ", ID to check " + id);
		if (shiftFound == null)
			return 0;
		int dayOfMonth =0;
		for (int i = 0 ; i < 32 ; i++) {                // cycle through the next 8 days worth of shifts

			if (shiftFound.getEmployee().equals(e)) {
				//System.out.println("Shift found for " + e.getLastName() + " - " +  shiftFound + " = " + shiftFound.getEmployee().getLastName());
				daysOn++;
				//if (!(shiftFound.getCall().equals("LATE")))
					//i+=4;
				shiftsOff = 0;
				dayOfMonth = shiftFound.getDate().getDayOfMonth();
			}
			else {
				shiftsOff++;
				//if (daysOn > 0)
					//shiftsOff += 4;
				if (shiftFound.getDate().getDayOfMonth() == (dayOfMonth + 2))
					daysOn = 0;
			}
			//System.out.println("ID found - " + shiftFound.getEmployee().getId() + ", stretch =  " + daysOn + " or days off = " + (shiftsOff/4) + ", i = " + i );
			if (i < 31)
				shiftFound = lastMonthsList.get(++idFound);
		}
		if (daysOn > 0)
			System.out.println("Stretch for " +e.getLastName()+ " from previous month = " + daysOn);

		return daysOn;
	}

}





