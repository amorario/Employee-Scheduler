package com.luv2code.springboot.thymeleafdemo.entity;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import jakarta.persistence.*;

@Entity
@Table(name="shift")
public class Shift {
  
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      @Column(name="id")
      private int id;
  
      @Column(name = "call")
      private String call;

      @Column(name = "date")
      private String date;
      
      @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE,
                          CascadeType.DETACH, CascadeType.REFRESH})
      @JoinColumn(name="employee_id")
      private Employee employee;

      //DateFormatSymbols syms = new DateFormatSymbols(new Locale ("en","US"));
      //DateFormat output = new SimpleDateFormat("MM-dd-yyyy", syms);
      //DateFormat output = new SimpleDateFormat("EEE MMMMM dd, YYYY 'at' hh:mm aaa", syms);
  
      public Shift () {
      }

      public Shift (String call, String date) {
	this.call = call;
	this.date = date;
      }
      
      public int getId() {
	return id;
      }
      public void setId(int id) {
	this.id = id;
      }
      public String getCall() {
	return call;
      }
      public void setCall(String call) {
	this.call = call;
      }
      public String getDate() {
	return date;
      }
      public void setDate(String date) {
	this.date = date;
      }
      public Employee getEmployee() {
	return employee;
      }
      public void setEmployee (Employee employee) {
	this.employee = employee;
      }
  
      @Override
      public String toString () {
	return "Shift{" +
                "id=" + id +
                ", call= " + call +
            //", date= " + output.format(date) +
            ", date= " + date +
                '}';
      }
}