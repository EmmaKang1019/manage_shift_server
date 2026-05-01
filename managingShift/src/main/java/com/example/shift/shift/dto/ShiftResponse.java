package com.example.shift.shift.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ShiftResponse(
        Long id,
        Long employeeId,
        String employeeName,
        String employeePosition,
        LocalDate workDate,
        LocalTime startTime,
        LocalTime endTime,
        String memo
) {
}
