package com.luv2code.springboot.thymeleafdemo.service;

import java.time.Month;
import java.util.List;

import com.luv2code.springboot.thymeleafdemo.entity.Shift;

public interface ShiftService {

	List<Shift> findAll();

	List<Shift> findAllByEmployeeId (int theId);

	Shift findById(int theId);

	void save(Shift theShift, int employeeId);

	void deleteById(int theId);

	void saveMonthlyShifts(List<Shift> shiftList);

	boolean shiftExists(Month m);

	List<Shift> getMonthlyShifts(int month, int year);
}