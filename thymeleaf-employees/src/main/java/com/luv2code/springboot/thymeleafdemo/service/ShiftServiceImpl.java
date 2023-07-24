package com.luv2code.springboot.thymeleafdemo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.luv2code.springboot.thymeleafdemo.dao.ShiftRepository;
import com.luv2code.springboot.thymeleafdemo.entity.Shift;

@Service
public class ShiftServiceImpl implements ShiftService {

	private ShiftRepository shiftRepository;
	
	@Autowired
	public ShiftServiceImpl(ShiftRepository theShiftRepository) {
		shiftRepository = theShiftRepository;
	}
	
	@Override
	public List<Shift> findAll() {
		return shiftRepository.findAll();
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
	public void save(Shift theShift) {
		shiftRepository.save(theShift);
	}

	@Override
	public void deleteById(int theId) {
		shiftRepository.deleteById(theId);
	}

}






