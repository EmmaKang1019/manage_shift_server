package com.example.shift.shift.controller;

import com.example.shift.shift.dto.ShiftCreateRequest;
import com.example.shift.shift.dto.ShiftResponse;
import com.example.shift.shift.service.ShiftService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shifts")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShiftResponse create(@RequestBody ShiftCreateRequest request) {
        return shiftService.createShift(request);
    }

    @GetMapping
    public List<ShiftResponse> list(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long employeeId
    ) {
        return shiftService.getShifts(from, to, employeeId);
    }
}
