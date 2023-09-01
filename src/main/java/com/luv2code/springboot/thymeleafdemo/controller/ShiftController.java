package com.luv2code.springboot.thymeleafdemo.controller;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.time.format.TextStyle;

import com.luv2code.springboot.thymeleafdemo.entity.Employee;
import com.luv2code.springboot.thymeleafdemo.entity.Shift;
import com.luv2code.springboot.thymeleafdemo.service.EmployeeService;
import com.luv2code.springboot.thymeleafdemo.service.ShiftService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
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


	/*
	@GetMapping("/viewMonthlyShifts")
	public String viewMonthlyShifts(@ModelAttribute("month") @Validated @RequestBody int theMonth, Model theModel) {
		Month m = Month.of(theMonth);

		//theModel.addAttribute("shifts", monthlyShifts);
		return "employees/shifts/list-monthly-shifts";
	}

	*/

	@PostMapping("/generateMonthlyShifts")
	public String generateMonthlyShifts(@ModelAttribute("month") @Validated @RequestBody String theMonth, Model theModel) {
        String[] splitStr = theMonth.split("\\s+");
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
        }
        else {
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
	public String viewMonth(@ModelAttribute("month") @Validated @RequestBody String theMonth, Model theModel) {
		String[] splitStr = theMonth.split("\\s+");
		Month m = Month.valueOf(splitStr[0].toUpperCase());
		int year = Integer.parseInt(splitStr[1]);
		//System.out.println("In viewMonth() and the month chosen is " + m+ " and the year is " + year);

		List<Shift> monthlyShifts = shiftService.getMonthlyShifts(m.getValue(), year);
		theModel.addAttribute("shifts", monthlyShifts);
		return "employees/shifts/list-monthly-shifts";
	}
}
