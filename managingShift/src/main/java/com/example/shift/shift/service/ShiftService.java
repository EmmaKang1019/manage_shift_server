package com.example.shift.shift.service;

import com.example.shift.employee.domain.Employee;
import com.example.shift.employee.repository.EmployeePositionRepository;
import com.example.shift.employee.repository.EmployeeRepository;
import com.example.shift.exception.BusinessException;
import com.example.shift.shift.domain.Shift;
import com.example.shift.shift.dto.ShiftCreateRequest;
import com.example.shift.shift.dto.ShiftResponse;
import com.example.shift.shift.repository.ShiftRepository;
import com.example.shift.team.domain.Position;
import com.example.shift.team.repository.PositionRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;
    private final EmployeePositionRepository employeePositionRepository;

    public ShiftService(ShiftRepository shiftRepository,
                        EmployeeRepository employeeRepository,
                        PositionRepository positionRepository,
                        EmployeePositionRepository employeePositionRepository) {
        this.shiftRepository = shiftRepository;
        this.employeeRepository = employeeRepository;
        this.positionRepository = positionRepository;
        this.employeePositionRepository = employeePositionRepository;
    }

    @Transactional
    public ShiftResponse createShift(ShiftCreateRequest request) {
        validateCreateRequest(request);

        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new BusinessException("직원을 찾을 수 없습니다."));
        Position position = positionRepository.findById(request.positionId())
                .orElseThrow(() -> new BusinessException("포지션을 찾을 수 없습니다."));

        boolean canWorkPosition = employeePositionRepository.existsByEmployeeIdAndPositionIdAndActiveTrue(
                request.employeeId(), request.positionId());
        if (!canWorkPosition) {
            throw new BusinessException("해당 직원이 수행할 수 없는 포지션입니다.");
        }

        boolean overlaps = !shiftRepository.findOverlaps(
                request.employeeId(), request.workDate(), request.startTime(), request.endTime()).isEmpty();

        if (overlaps) {
            throw new BusinessException("같은 날짜와 시간에 겹치는 시프트가 이미 존재합니다.");
        }

        Shift shift = new Shift(employee, position, request.workDate(), request.startTime(), request.endTime(), request.memo());
        Shift saved = shiftRepository.save(shift);
        return toResponse(saved);
    }

    public List<ShiftResponse> getShifts(LocalDate from, LocalDate to, Long employeeId) {
        List<Shift> shifts = employeeId == null
                ? shiftRepository.findByWorkDateBetweenOrderByWorkDateAscStartTimeAsc(from, to)
                : shiftRepository.findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAscStartTimeAsc(employeeId, from, to);

        return shifts.stream().map(this::toResponse).toList();
    }

    private void validateCreateRequest(ShiftCreateRequest request) {
        if (request == null
                || request.employeeId() == null
                || request.positionId() == null
                || request.workDate() == null
                || request.startTime() == null
                || request.endTime() == null) {
            throw new BusinessException("요청값이 올바르지 않습니다.");
        }

        if (!request.startTime().isBefore(request.endTime())) {
            throw new BusinessException("시작 시간은 종료 시간보다 빨라야 합니다.");
        }
    }

    private ShiftResponse toResponse(Shift shift) {
        Position position = shift.getPosition();
        return new ShiftResponse(
                shift.getId(),
                shift.getEmployee().getId(),
                shift.getEmployee().getName(),
                position.getTeam().getId(),
                position.getTeam().getName(),
                position.getId(),
                position.getName(),
                shift.getWorkDate(),
                shift.getStartTime(),
                shift.getEndTime(),
                shift.getMemo());
    }
}
