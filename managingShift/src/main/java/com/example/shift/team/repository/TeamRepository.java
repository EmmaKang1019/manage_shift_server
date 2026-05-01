package com.example.shift.team.repository;

import com.example.shift.team.domain.Team;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByActiveTrueOrderByNameAsc();
}
