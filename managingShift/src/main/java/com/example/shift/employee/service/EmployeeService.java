package com.example.shift.employee.service;

import com.example.shift.employee.domain.Employee;
import com.example.shift.employee.domain.EmployeePosition;
import com.example.shift.employee.dto.EmployeeCreateRequest;
import com.example.shift.employee.dto.EmployeePositionResponse;
import com.example.shift.employee.dto.EmployeeResponse;
import com.example.shift.employee.repository.EmployeePositionRepository;
import com.example.shift.employee.repository.EmployeeRepository;
import com.example.shift.exception.BusinessException;
import com.example.shift.team.domain.Position;
import com.example.shift.team.repository.PositionRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;
    private final EmployeePositionRepository employeePositionRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           PositionRepository positionRepository,
                           EmployeePositionRepository employeePositionRepository) {
        this.employeeRepository = employeeRepository;
        this.positionRepository = positionRepository;
        this.employeePositionRepository = employeePositionRepository;
    }

    @Transactional
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        validateCreateRequest(request);

        Set<Long> positionIds = new LinkedHashSet<>(request.positionIds());
        Long primaryPositionId = request.primaryPositionId() == null
                ? positionIds.iterator().next()
                : request.primaryPositionId();

        if (!positionIds.contains(primaryPositionId)) {
            throw new BusinessException("대표 포지션은 가능한 포지션 목록에 포함되어야 합니다.");
        }

        List<Position> positions = positionRepository.findAllById(positionIds);
        if (positions.size() != positionIds.size()) {
            throw new BusinessException("존재하지 않는 포지션이 포함되어 있습니다.");
        }

        Employee employee = employeeRepository.save(new Employee(request.name().trim()));
        List<EmployeePosition> employeePositions = positions.stream()
                .map(position -> new EmployeePosition(employee, position, position.getId().equals(primaryPositionId)))
                .map(employeePositionRepository::save)
                .toList();

        return toResponse(employee, employeePositions);
    }

    public List<EmployeeResponse> getActiveEmployees() {
        return employeeRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateCreateRequest(EmployeeCreateRequest request) {
        if (request == null || isBlank(request.name()) || request.positionIds() == null || request.positionIds().isEmpty()) {
            throw new BusinessException("직원 이름과 가능한 포지션은 필수입니다.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private EmployeeResponse toResponse(Employee employee) {
        List<EmployeePosition> positions = employeePositionRepository
                .findByEmployeeIdAndActiveTrueOrderByPrimaryPositionDescPositionTeamNameAscPositionNameAsc(employee.getId());
        return toResponse(employee, positions);
    }

    private EmployeeResponse toResponse(Employee employee, List<EmployeePosition> positions) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getName(),
                positions.stream().map(this::toPositionResponse).toList(),
                employee.isActive());
    }

    private EmployeePositionResponse toPositionResponse(EmployeePosition employeePosition) {
        Position position = employeePosition.getPosition();
        return new EmployeePositionResponse(
                position.getTeam().getId(),
                position.getTeam().getName(),
                position.getId(),
                position.getName(),
                employeePosition.isPrimaryPosition());
    }
}
