package com.example.shift.team.service;

import com.example.shift.exception.BusinessException;
import com.example.shift.team.domain.Position;
import com.example.shift.team.domain.Team;
import com.example.shift.team.dto.PositionCreateRequest;
import com.example.shift.team.dto.PositionResponse;
import com.example.shift.team.dto.TeamCreateRequest;
import com.example.shift.team.dto.TeamResponse;
import com.example.shift.team.repository.PositionRepository;
import com.example.shift.team.repository.TeamRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TeamService {

    private final TeamRepository teamRepository;
    private final PositionRepository positionRepository;

    public TeamService(TeamRepository teamRepository, PositionRepository positionRepository) {
        this.teamRepository = teamRepository;
        this.positionRepository = positionRepository;
    }

    @Transactional
    public TeamResponse createTeam(TeamCreateRequest request) {
        if (request == null || isBlank(request.name())) {
            throw new BusinessException("팀 이름은 필수입니다.");
        }

        Team saved = teamRepository.save(new Team(request.name().trim()));
        return toTeamResponse(saved);
    }

    public List<TeamResponse> getTeams() {
        return teamRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::toTeamResponse)
                .toList();
    }

    @Transactional
    public PositionResponse createPosition(PositionCreateRequest request) {
        if (request == null || request.teamId() == null || isBlank(request.name())) {
            throw new BusinessException("팀 ID와 포지션 이름은 필수입니다.");
        }

        Team team = teamRepository.findById(request.teamId())
                .orElseThrow(() -> new BusinessException("팀을 찾을 수 없습니다."));

        Position saved = positionRepository.save(new Position(team, request.name().trim()));
        return toPositionResponse(saved);
    }

    public List<PositionResponse> getPositions(Long teamId) {
        List<Position> positions = teamId == null
                ? positionRepository.findByActiveTrueOrderByTeamNameAscNameAsc()
                : positionRepository.findByTeamIdAndActiveTrueOrderByNameAsc(teamId);

        return positions.stream()
                .map(this::toPositionResponse)
                .toList();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private TeamResponse toTeamResponse(Team team) {
        return new TeamResponse(team.getId(), team.getName(), team.isActive());
    }

    private PositionResponse toPositionResponse(Position position) {
        return new PositionResponse(
                position.getId(),
                position.getTeam().getId(),
                position.getTeam().getName(),
                position.getName(),
                position.isActive());
    }
}
