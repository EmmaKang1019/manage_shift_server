package com.example.shift.team.controller;

import com.example.shift.team.dto.PositionCreateRequest;
import com.example.shift.team.dto.PositionResponse;
import com.example.shift.team.dto.TeamCreateRequest;
import com.example.shift.team.dto.TeamResponse;
import com.example.shift.team.service.TeamService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping("/teams")
    @ResponseStatus(HttpStatus.CREATED)
    public TeamResponse createTeam(@RequestBody TeamCreateRequest request) {
        return teamService.createTeam(request);
    }

    @GetMapping("/teams")
    public List<TeamResponse> listTeams() {
        return teamService.getTeams();
    }

    @PostMapping("/positions")
    @ResponseStatus(HttpStatus.CREATED)
    public PositionResponse createPosition(@RequestBody PositionCreateRequest request) {
        return teamService.createPosition(request);
    }

    @GetMapping("/positions")
    public List<PositionResponse> listPositions(@RequestParam(required = false) Long teamId) {
        return teamService.getPositions(teamId);
    }
}
