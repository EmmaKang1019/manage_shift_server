package com.example.shift.team.dto;

public record PositionCreateRequest(
        Long teamId,
        String name
) {
}
