package com.luv2code.springboot.thymeleafdemo.controller;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import com.luv2code.springboot.thymeleafdemo.entity.Employee;
import com.luv2code.springboot.thymeleafdemo.entity.Shift;
import com.luv2code.springboot.thymeleafdemo.service.EmployeeService;
import com.luv2code.springboot.thymeleafdemo.service.ShiftService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("employees/shifts")
public class ShiftController {

	private ShiftService shiftService;

	public ShiftController(ShiftService theShiftService) {
		shiftService = theShiftService;
	}

	// add mapping for "/list"

	@GetMapping("/shifts-list")
	public String listShifts(@RequestParam("employeeId") int theId, Model theModel) {

		System.out.println("In the Shift Controller method listShifts()");
		// get the shifts from db
		//List<Shift> theShifts = shiftService.findAll();

		//Employee theEmployee = theShift.getEmployee();

		// add to the spring model
		//theModel.addAttribute("shifts", theShifts);

		// theModel.addAttribute("shifts", theEmployee.getShifts());
		theModel.addAttribute("shifts",shiftService.findAllByEmployeeId(theId));
		return "employees/shifts/list-shifts";
	}

	/*@GetMapping("/shifts-list")
	public String listShifts(@RequestParam("employeeId") int theId,
							 Model theModel) {

		// get the employee from the service
		Employee theEmployee = employeeService.findById(theId);

		// add to the spring model
		theModel.addAttribute("shifts", theEmployee.getShifts());

		return "employees/shifts/list-shifts";
	}*/

	@GetMapping("/showFormForShiftAdd")
	public String showFormForAdd(Model theModel) {

		// create model attribute to bind form data
		Shift theShift = new Shift();

		theModel.addAttribute("shift", theShift);

		return "employees/shifts/save-shift";
	}

	@GetMapping("/showFormForShiftUpdate")
	public String showFormForUpdate(@RequestParam("shiftId") int theId,
									Model theModel) {

		System.out.println("Inside method for showFormShiftUpdate");

		// get the shift from the service
		Shift theShift = shiftService.findById(theId);

		// set shift as a model attribute to pre-populate the form
		theModel.addAttribute("shift", theShift);

		// send over to our form
		return "employees/shifts/save-shift";
	}

	/*@PostMapping("/save")
	public String saveShift(@ModelAttribute("shift") Shift theShift) {

		// save the shift
		shiftService.save(theShift);

		// use a redirect to prevent duplicate submissions
		return "redirect:/shifts/list";
	}*/

	@GetMapping("/deleteShift")
	public String delete(@RequestParam("shiftId") int theId) {

		// delete the shift
		shiftService.deleteById(theId);

		System.out.println("In the Shift Controller");

		// redirect to /shifts/list
		return "redirect:/employees/list";

	}
	
	@PostMapping("/saveShift")
	public String saveShift(@RequestParam("employeeId") int theId,
							@ModelAttribute("shift") Shift theShift, Model theModel) {

		// save the shift
		shiftService.save(theShift, theId);

		// use a redirect to prevent duplicate submissions

		//return "redirect:/employees/list";
		return listShifts(theId, theModel);
	}
	
	@PostMapping("/generateMonthlyShifts")
	public String generateMonthlyShifts(@RequestParam("month") int theMonth, Model theModel) {

		List<Shift> monthlyShifts = new ArrayList<>();
		Month m = Month.of(theMonth);
		String call;
		LocalDate date;

		for (date= LocalDate.of(2023, theMonth, 1); date.isBefore(LocalDate.of(2023, theMonth, m.length(false))); date = date.plusDays(1)) { // tbd isLeapYear
			for (int i = 0; i < 4; i++) {
				switch (i) {
					case 0:
						call = "NO";
						break;
					case 1:
						call = "EARLY";
						break;
					case 2:
						call = "MID";
						break;
					case 3:
						call = "LATE";
						break;
					default:
						call = "NO";
						break;

				}
				Shift s = new Shift(call, date);
				monthlyShifts.add(s);
			}

			theModel.addAttribute("shifts", monthlyShifts);

		}
		return "employees/shifts/list-monthly-shifts";
	}
}









