package com.luv2code.springboot.thymeleafdemo.service;

import java.time.Month;
import java.util.List;

import com.luv2code.springboot.thymeleafdemo.entity.Employee;
import com.luv2code.springboot.thymeleafdemo.entity.Shift;

public interface ShiftService {

	List<Shift> findAll();

	List<Employee> findAllEmployees();

	List<Shift> findAllByEmployeeId (int theId);

	Shift findById(int theId);

	void saveWithEmployee(Shift theShift, int employeeId);

    void save(Shift theShift);

    void deleteById(int theId);

	void saveMonthlyShifts(List<Shift> shiftList);

	boolean shiftExists(Month m);

	List<Shift> getMonthlyShifts(int month, int year);

	void assignShiftsToEmployees(List<Shift> shiftList);

	void clearMonthlyShifts(List<Shift> monthlyShifts);

	boolean isUnassigned(List<Shift> monthlyShifts);

	void updateEmployeeList(List<Employee> wrapperList);

	void defaultShiftsAmount(List<Employee> allEmployees);

	void printMonthShifts(List<Shift> shiftList, List<Employee> employeeList);
}