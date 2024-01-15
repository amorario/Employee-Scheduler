package com.luv2code.springboot.thymeleafdemo.entity;

import com.luv2code.springboot.thymeleafdemo.entity.Employee;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.ArrayList;
import java.util.List;

public class EmployeeListWrapper {

    private List<Employee> employeeList = new ArrayList<>();

    public List<Employee> getEmployeeList() {
        return employeeList;
    }

    public void setEmployeeList(List<Employee> employeeList) {
        this.employeeList = employeeList;
    }
}
