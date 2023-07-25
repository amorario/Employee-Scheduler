package com.luv2code.springboot.thymeleafdemo.controller;

import java.util.List;

import com.luv2code.springboot.thymeleafdemo.entity.Employee;
import com.luv2code.springboot.thymeleafdemo.entity.Shift;
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
	public String listShifts(@RequestParam("shiftId") int theId, Model theModel) {

		System.out.println("In the Shift Controller method listShifts()");
		// get the shifts from db
		List<Shift> theShifts = shiftService.findAll();

		Shift theShift = shiftService.findById(theId);
		Employee theEmployee = theShift.getEmployee();

		// add to the spring model
		//theModel.addAttribute("shifts", theShifts);

		theModel.addAttribute("shifts", theEmployee.getShifts());

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
	public String saveShift(@ModelAttribute("shift") Shift theShift) {


		// save the shift
		shiftService.save(theShift);

		// use a redirect to prevent duplicate submissions
		return "redirect:employees/shifts/shifts-list";
	}
}









