package com.example.shift.employee.dto;

public record EmployeePositionResponse(
        Long teamId,
        String teamName,
        Long positionId,
        String positionName,
        boolean primaryPosition
) {
}
