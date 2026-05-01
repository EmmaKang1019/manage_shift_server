package com.example.shift.employee.dto;

import java.util.List;

public record EmployeeResponse(
        Long id,
        String name,
        List<EmployeePositionResponse> positions,
        boolean active
) {
}
