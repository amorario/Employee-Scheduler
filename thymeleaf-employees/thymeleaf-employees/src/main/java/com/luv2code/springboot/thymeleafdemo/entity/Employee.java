package com.luv2code.springboot.thymeleafdemo.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name="employee")
public class Employee {
  
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      @Column(name="id")
      private int id;
  
      @Column(name="first_name")
      private String firstName;

      @Column(name="last_name")
      private String lastName;

      @Column(name="email")
      private String email;
  
      @OneToMany(mappedBy = "employee",
			  fetch = FetchType.LAZY,
			  cascade = {CascadeType.PERSIST, CascadeType.MERGE,
					  CascadeType.DETACH, CascadeType.REFRESH})
      private List<Shift> shifts;

      /*DateFormatSymbols syms = new DateFormatSymbols(new Locale ("en","US"));
      DateFormat output = new SimpleDateFormat("MM-dd-yyyy", syms);
      //DateFormat output = new SimpleDateFormat("EEE MMMMM dd, YYYY 'at' hh:mm aaa", syms);*/
  
      public Employee () {
      }

      public Employee (String firstName, String lastName, String email) {
	  this.firstName = firstName;
	  this.lastName = lastName;
	  this.email = email;
      }
      
      public int getId() {
	  return id;
      }
      public void setId(int id) {
	  this.id = id;
      }
      public String getFirstName() {
	  return firstName;
      }
      public void setFirstName(String firstName) {
	  this.firstName = firstName;
      }
      public String getLastName() {
	  return lastName;
      }
      public void setLastName(String lastName) {
	  this.lastName = lastName;
      }
      public String getEmail() {
	  return email;
      }
      public void setEmail(String email) {
	  this.email = email;
      }
      
      public List<Shift> getShifts() {
	  return shifts;
      }
      public void setShifts(List<Shift> shifts) {
	  this.shifts = shifts;
      }
  
      @Override
      public String toString() {
	  return "Employee{" +
		  "id=" + id +
		  ", firstName='" + firstName + '\'' +
		  ", lastName='" + lastName + '\'' +
		  ", email='" + email + '\'' +
		  '}';
      }
      
      public void add(Shift tempShift) {

        if (shifts == null) {
            shifts = new ArrayList<>();
        }

        shifts.add(tempShift);

        tempShift.setEmployee(this);
    }
}