package com.example.shift.team.repository;

import com.example.shift.team.domain.Position;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, Long> {

    List<Position> findByActiveTrueOrderByTeamNameAscNameAsc();

    List<Position> findByTeamIdAndActiveTrueOrderByNameAsc(Long teamId);
}
