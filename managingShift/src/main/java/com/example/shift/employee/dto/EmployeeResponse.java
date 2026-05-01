package com.example.shift.employee.dto;

public record EmployeeResponse(
        Long id,
        String name,
        String position,
        boolean active
) {
}
