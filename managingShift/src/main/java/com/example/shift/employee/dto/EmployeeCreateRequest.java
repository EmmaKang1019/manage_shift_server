package com.example.shift.employee.dto;

import java.util.List;

public record EmployeeCreateRequest(
        String name,
        List<Long> positionIds,
        Long primaryPositionId
) {
}
