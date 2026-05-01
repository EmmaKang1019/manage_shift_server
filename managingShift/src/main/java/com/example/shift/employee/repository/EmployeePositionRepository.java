package com.example.shift.employee.repository;

import com.example.shift.employee.domain.EmployeePosition;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeePositionRepository extends JpaRepository<EmployeePosition, Long> {

    boolean existsByEmployeeIdAndPositionIdAndActiveTrue(Long employeeId, Long positionId);

    List<EmployeePosition> findByEmployeeIdAndActiveTrueOrderByPrimaryPositionDescPositionTeamNameAscPositionNameAsc(Long employeeId);
}
