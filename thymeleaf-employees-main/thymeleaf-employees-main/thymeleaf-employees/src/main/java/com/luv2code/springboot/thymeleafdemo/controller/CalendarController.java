
package org.daypilot.demo.html5Shiftcalendarspring.controller;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import org.daypilot.demo.html5Shiftcalendarspring.domain.Shift;
import org.daypilot.demo.html5Shiftcalendarspring.repository.ShiftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.*;

import jakarta.transaction.Transactional;
import java.time.LocalDate;

@RestController
public class CalendarController {

    @Autowired
    ShiftRepository shiftRepository;

    @RequestMapping("/api")
    @ResponseBody
    String home() {
        return "Welcome!";
    }

    @GetMapping("/api/Shifts")
    @JsonSerialize(using = LocalDateSerializer.class)
    Iterable<Shift> Shifts(@RequestParam("start") @DateTimeFormat(iso = ISO.DATE_TIME) LocalDate start, @RequestParam("end") @DateTimeFormat(iso = ISO.DATE_TIME) LocalDate end) {
        return shiftRepository.findBetween(start, end);
    }

    @PostMapping("/api/Shifts/create")
    @JsonSerialize(using = LocalDateSerializer.class)
    @Transactional
    Shift createShift(@RequestBody ShiftCreateParams params) {

        Shift s = new Shift();
        s.setDate(params.date);
        s.setCall(params.call);
        shiftRepository.save(s);
        return s;
    }

    @PostMapping("/api/Shifts/move")
    @JsonSerialize(using = LocalDateSerializer.class)
    @Transactional
    Shift moveShift(@RequestBody ShiftMoveParams params) {

        Shift s = shiftRepository.findById(params.id).get();
        s.setStart(params.date);
        shiftRepository.save(s);

        return s;
    }

    @PostMapping("/api/Shifts/setColor")
    @JsonSerialize(using = LocalDateSerializer.class)
    @Transactional
    Shift setColor(@RequestBody SetColorParams params) {

        Shift s = shiftRepository.findById(params.id).get();
        s.setColor(params.color);
        shiftRepository.save(s);

        return s;
    }

    @PostMapping("/api/Shifts/delete")
    @JsonSerialize(using = LocalDateSerializer.class)
    @Transactional
    ShiftDeleteResponse deleteShift(@RequestBody ShiftDeleteParams params) {

        shiftRepository.deleteById(params.id);

        return new ShiftDeleteResponse() {{
            message = "Deleted";
        }};
    }

    public static class ShiftDeleteParams {
        public int id;
    }

    public static class ShiftDeleteResponse {
        public String message;
    }

    public static class ShiftCreateParams {
        public LocalDate date;
        public String call;
    }

    public static class ShiftMoveParams {
        public int id;
        public LocalDate date;
    }

    public static class SetColorParams {
        public int id;
        public String color;
    }


}