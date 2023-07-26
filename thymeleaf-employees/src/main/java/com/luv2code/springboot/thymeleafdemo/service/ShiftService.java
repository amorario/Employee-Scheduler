package com.luv2code.springboot.thymeleafdemo.service;

import java.util.List;

import com.luv2code.springboot.thymeleafdemo.entity.Shift;

public interface ShiftService {

	List<Shift> findAll();
	
	Shift findById(int theId);
	
	void save(Shift theShift, int employeeId);
	
	void deleteById(int theId);
	
}
