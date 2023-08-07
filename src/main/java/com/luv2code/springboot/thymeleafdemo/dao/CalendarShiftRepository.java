package org.daypilot.demo.html5Shiftcalendarspring.repository;

import java.time.LocalDate;
import java.util.List;

import org.daypilot.demo.html5Shiftcalendarspring.domain.Shift;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import org.springframework.data.jpa.repository.JpaRepository;

import com.luv2code.springboot.thymeleafdemo.entity.Shift;

import java.util.List;

public interface ShiftRepository extends JpaRepository<Shift, Integer> {
	@Query("from Shift s where not(s.end < :from or s.start > :to)")
	public List<Shift> findBetween(@Param("from") @DateTimeFormat(iso=ISO.DATE) LocalDate start, @Param("to") @DateTimeFormat(iso=ISO.DATE) LocalDate end);
}