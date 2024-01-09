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


    @Column(name="month_set")
    private boolean monthSet;


    @Column(name="shifts_amount")
    private int shiftsAmount;

    @Column(name="days_off")
    private String daysOff;

    @Column(name="weekends")
    private int weekends;

    public String getDaysOff() {
        return daysOff;
    }

    public void setDaysOff(String daysOff) {
        this.daysOff = daysOff;
    }

    public int getWeekends() {
        return weekends;
    }

    public void setWeekends(int weekends) {
        this.weekends = weekends;
    }

    public int getShiftsAmount() {
        return shiftsAmount;
    }

    public void setShiftsAmount(int shiftsAmount) {
        this.shiftsAmount = shiftsAmount;
    }
    public boolean isMonthSet() {
        return monthSet;
    }

    public void setMonthSet(boolean monthSet) {
        this.monthSet = monthSet;
    }

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
                ", daysOff='" + daysOff + '\'' +
                ", shifts total='" + shiftsAmount + '\'' +
                '}';
    }

    public void add(Shift tempShift) {

        if (shifts == null) {
            shifts = new ArrayList<>();
        }

        shifts.add(tempShift);

        tempShift.setEmployee(this);
    }

    public int getShiftsAmount(List<Shift> shifts) {
        int count = 0;
        for (Shift s : shifts) {
            if (this.id == s.getId()){
                count++;
            }
        }
        return count;
    }

    public List<Integer> getDaysOffIntList() {
        List<Integer> list = new ArrayList<>();
        if (daysOff == null)
            return null;
        String[] daysOffString = daysOff.split("\\s+");
        for (String dayStr : daysOffString) {
            if (dayStr == null)
                return list;
            list.add(Integer.parseInt(dayStr));
        }
        return list;
    }

}