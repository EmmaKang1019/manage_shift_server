package com.example.shift.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {
    @GetMapping("/{id}/eligible-substitutes")
    public ResponseEntity checkEligibleSubstitutes(@PathVariable Long id){
        return ResponseEntity.ok(List.of());
    }
}
