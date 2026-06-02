package com.example.shift.controller;

import com.example.shift.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/schedule")
public class ScheduleController {

    @GetMapping
    public ResponseEntity shiftCoverage(@RequestParam String startDate, @RequestParam String endDate){

        return ResponseEntity.ok(List.of());
    }
}
