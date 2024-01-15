package com.luv2code.springboot.thymeleafdemo.controller;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.time.format.TextStyle;
import java.util.Locale;

import com.luv2code.springboot.thymeleafdemo.entity.Employee;
import com.luv2code.springboot.thymeleafdemo.entity.EmployeeListWrapper;
import com.luv2code.springboot.thymeleafdemo.entity.Shift;
import com.luv2code.springboot.thymeleafdemo.service.EmployeeService;
import com.luv2code.springboot.thymeleafdemo.service.ShiftService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

@Controller
//@SessionAttributes("monthString")
@RequestMapping("employees/shifts")
public class ShiftController {

	private ShiftService shiftService;

	public ShiftController(ShiftService theShiftService) {
		shiftService = theShiftService;
	}

	@GetMapping("/shifts-list")
	public String listShifts(@RequestParam("employeeId") int theId, Model theModel) {

		// get the shifts from db and add to the spring model
		theModel.addAttribute("shifts",shiftService.findAllByEmployeeId(theId));

		return "employees/shifts/list-shifts";
	}

	@GetMapping("/showFormForShiftAdd")
	public String showFormForAdd(Model theModel) {

		// create model attribute to bind form data
		Shift theShift = new Shift();

		theModel.addAttribute("shift", theShift);

		return "employees/shifts/save-shift";
	}

	@GetMapping("/showFormForShiftUpdate")
	public String showFormForUpdate(@RequestParam("shiftId") int theId, Model theModel) {

		System.out.println("Inside method for showFormShiftUpdate");

		// get the shift from the service
		Shift theShift = shiftService.findById(theId);

		System.out.println("Found the shift: " + theShift.getId() + " - " + theShift.getCall() + " - " + theShift.getDate());

		// set shift as a model attribute to pre-populate the form
		theModel.addAttribute("shift", theShift);


		String theMonth = theShift.getDate().getMonth() + " " + theShift.getDate().getYear();
		System.out.println("the month redirecting to " + theMonth);

		theModel.addAttribute("month", theMonth);
		theModel.addAttribute("employeeList", shiftService.findAllEmployees());

		// send over to our form
		return "employees/shifts/save-shift";
	}

	@GetMapping("/showAssignmentForm")
	public String showAssignmentForm(@ModelAttribute("month") String theMonth, Model theModel) {

		System.out.println("Inside method for showAssignmentForm");

		// get the employee list from the service
		EmployeeListWrapper wrapper = new EmployeeListWrapper();
		wrapper.setEmployeeList(shiftService.findAllEmployees());

		// set shift as a model attribute to pre-populate the form
		theModel.addAttribute("wrapper", wrapper);

		System.out.println("the month redirecting to " + theMonth);

		theModel.addAttribute("month", theMonth);

		// send over to our form
		return "employees/shifts/assignment-form";
	}

	@GetMapping("/deleteShift")
	public String delete(@RequestParam("shiftId") int theId) {

		// delete the shift
		shiftService.deleteById(theId);

		System.out.println("In the Shift Controller");

		// redirect to /shifts/list
		return "redirect:/employees/list";

	}

	@PostMapping("/saveShiftWithEmployee")
	public String saveShiftWithEmployee(@RequestParam("employeeId") int theId,
										@ModelAttribute("shift") Shift theShift, Model theModel) {

		// save the shift
		shiftService.saveWithEmployee(theShift, theId);

		// use a redirect to prevent duplicate submissions

		//return "redirect:/employees/list";
		return listShifts(theId, theModel);
	}

	@PostMapping("/saveShift")
	public String saveShift(@ModelAttribute("shift") Shift theShift, Model theModel) {

		System.out.println("in saveShift and the employee found is " + theShift.getEmployee());
		// save the shift
		shiftService.save(theShift);

		// use a redirect to prevent duplicate submissions
		String theMonth = theShift.getDate().getMonth() + " " + theShift.getDate().getYear();
		System.out.println("in saveShift and the month found is " + theMonth);

		theModel.addAttribute("month", theMonth);
		return "redirect:/employees/shifts/viewMonth?month=" + theMonth;
	}

	@PostMapping("/assign")
	public String assign(@ModelAttribute("month") String theMonth, @ModelAttribute("wrapper") EmployeeListWrapper wrapper, Model theModel) {
		String[] splitStr = theMonth.split("\\s+");
		Month m = Month.valueOf(splitStr[0].toUpperCase());
		int year = Integer.parseInt(splitStr[1]);
		List<Shift> monthlyShifts = shiftService.getMonthlyShifts(m.getValue(), year);

		//System.out.println(wrapper.getEmployeeList() != null ? wrapper.getEmployeeList().size() : "null list");

		//for (Employee e : wrapper.getEmployeeList())
			//System.out.println(e);

		shiftService.updateEmployeeList(wrapper.getEmployeeList());

			if (!shiftService.isUnassigned(monthlyShifts))
				shiftService.clearMonthlyShifts(monthlyShifts);
		shiftService.assignShiftsToEmployees(monthlyShifts);

		for (Shift s : monthlyShifts) {
			if (s.getEmployee() != null) {
				//System.out.println(s.getDate() + " - " + s.getCall() + " --- controller --- " + s.getEmployee().getLastName());
				shiftService.save(s);
			}
		}

		return "redirect:/employees/shifts/viewMonth?month=" + theMonth;
	}

	@GetMapping("/generateMonthlyShifts")
	public String generateMonthlyShifts(@ModelAttribute("month") String theMonth, Model theModel) {
		String[] splitStr = theMonth.split("\\s+");
		System.out.println("The month selected is: "+theMonth);
		Month m = Month.valueOf(splitStr[0].toUpperCase());         // Month object chosen by user
		int year = Integer.parseInt(splitStr[1]);                   // year integer chosen by user
		String call;
		LocalDate date = LocalDate.of(year, m.getValue(), 1);
		List<Shift> monthlyShifts;

		// check for month's shifts
		if (shiftService.shiftExists(m)) {
			monthlyShifts = shiftService.getMonthlyShifts(m.getValue(), year);
			for (Shift s : monthlyShifts) {
				s.setEmployee(null);
			}
		} else {
			monthlyShifts = new ArrayList<>();
			LocalDate nextMonth = date.plusMonths(1);
			for (; date.isBefore(nextMonth); date = date.plusDays(1)) {
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
							call = "BACKUP";
							break;
					}
					Shift s = new Shift(call, date);
					monthlyShifts.add(s);
				}
			}
			shiftService.saveMonthlyShifts(monthlyShifts);
		}

		theModel.addAttribute("shifts", monthlyShifts);


		return "employees/shifts/list-monthly-shifts";
	}

	@GetMapping("/viewMonth")
	public String viewMonth(@ModelAttribute("month") String theMonth, Model theModel, HttpSession session) {
		session.setAttribute("monthString", theMonth);
		System.out.println("In viewMonth() and the month chosen is " + theMonth);
		String[] splitStr = theMonth.split("\\s+");
		Month m = Month.valueOf(splitStr[0].toUpperCase());
		int year = Integer.parseInt(splitStr[1]);
		System.out.println("In viewMonth() and the month chosen is " + m+ " and the year is " + year);

		List<Shift> monthlyShifts = shiftService.getMonthlyShifts(m.getValue(), year);
		theModel.addAttribute("shifts", monthlyShifts);
		theModel.addAttribute("month", theMonth);
		return "employees/shifts/list-monthly-shifts";
	}

}