package com.luv2code.springboot.thymeleafdemo.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.luv2code.springboot.thymeleafdemo.entity.Employee;
import com.luv2code.springboot.thymeleafdemo.service.EmployeeService;

import com.luv2code.springboot.thymeleafdemo.service.ShiftService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

	private EmployeeService employeeService;

	private static List<Integer> nearbyMonths;
	static {
		LocalDate prior2, prior1, current, next1, next2, next3;		// previous 2 months, current month, and the next 3 months
		int currentMonth = LocalDate.now().getMonthValue();
		int currentYear = LocalDate.now().getYear();
		current = LocalDate.of(currentYear, currentMonth, 1);
		prior2 = current.minusMonths(2);
		prior1 = current.minusMonths(1);
		next1 = current.plusMonths(1);
		next2 = current.plusMonths(2);
		next3 = current.plusMonths(3);
		nearbyMonths = new ArrayList<>(Arrays.asList(prior2.getMonthValue(), prior1.getMonthValue(),
				current.getMonthValue(), next1.getMonthValue(), next2.getMonthValue(), next3.getMonthValue()));
	}
	public EmployeeController(EmployeeService theEmployeeService) {
		employeeService = theEmployeeService;
	}

	// add mapping for "/list"

	@GetMapping("/list")
	public String listEmployees(Model theModel) {

		// get the employees from db
		List<Employee> theEmployees = employeeService.findAll();

		// add to the spring model
		theModel.addAttribute("employees", theEmployees);
		theModel.addAttribute("nearbyMonths", nearbyMonths);
		return "employees/list-employees";
	}

	@GetMapping("/showFormForAdd")
	public String showFormForAdd(Model theModel) {

		// create model attribute to bind form data
		Employee theEmployee = new Employee();

		theModel.addAttribute("employee", theEmployee);

		return "employees/employee-form";
	}

	@GetMapping("/showFormForUpdate")
	public String showFormForUpdate(@RequestParam("employeeId") int theId,
									Model theModel) {

		// get the employee from the service
		Employee theEmployee = employeeService.findById(theId);

		// set employee as a model attribute to pre-populate the form
		theModel.addAttribute("employee", theEmployee);

		// send over to our form
		return "employees/employee-form";
	}

	@PostMapping("/save")
	public String saveEmployee(@ModelAttribute("employee") Employee theEmployee) {

		// save the employee
		employeeService.save(theEmployee);

		// use a redirect to prevent duplicate submissions
		return "redirect:/employees/list";
	}

	@GetMapping("/delete")
	public String delete(@RequestParam("employeeId") int theId) {

		// delete the employee
		employeeService.deleteById(theId);

		// redirect to /employees/list
		return "redirect:/employees/list";

	}

	@GetMapping("/shifts-list")
	public String listShifts(@RequestParam("employeeId") int theId,
								Model theModel) {

		// get the employee from the service
		Employee theEmployee = employeeService.findById(theId);

		System.out.println("In the Employee Controller list");

		// add to the spring model
		theModel.addAttribute("shifts", theEmployee.getShifts());

		return "employees/shifts/list-shifts";
	}

}









