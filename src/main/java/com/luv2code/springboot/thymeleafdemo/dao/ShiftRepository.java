package com.luv2code.springboot.thymeleafdemo.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.luv2code.springboot.thymeleafdemo.entity.Shift;

import java.util.List;

public interface ShiftRepository extends JpaRepository<Shift, Integer> {

	// that's it ... no need to write any code LOL!

    // add a method to sort by last name
    // public List<Shift> findAllByOrderByLastNameAsc();

}
