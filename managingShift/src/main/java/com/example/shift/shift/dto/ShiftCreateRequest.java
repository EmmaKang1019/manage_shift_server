package com.example.shift.shift.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ShiftCreateRequest(
        Long employeeId,
        LocalDate workDate,
        LocalTime startTime,
        LocalTime endTime,
        String memo
) {
}
