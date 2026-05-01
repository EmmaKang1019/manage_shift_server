package com.example.shift.team.dto;

public record PositionResponse(
        Long id,
        Long teamId,
        String teamName,
        String name,
        boolean active
) {
}
