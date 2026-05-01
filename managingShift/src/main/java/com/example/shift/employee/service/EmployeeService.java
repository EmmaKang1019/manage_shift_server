package com.example.shift.employee.service;

import com.example.shift.employee.domain.Employee;
import com.example.shift.employee.dto.EmployeeCreateRequest;
import com.example.shift.employee.dto.EmployeeResponse;
import com.example.shift.employee.repository.EmployeeRepository;
import com.example.shift.exception.BusinessException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        validateCreateRequest(request);

        Employee employee = new Employee(request.name().trim(), request.position().trim());
        Employee saved = employeeRepository.save(employee);
        return toResponse(saved);
    }

    public List<EmployeeResponse> getActiveEmployees() {
        return employeeRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateCreateRequest(EmployeeCreateRequest request) {
        if (request == null || isBlank(request.name()) || isBlank(request.position())) {
            throw new BusinessException("직원 이름과 포지션은 필수입니다.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getName(),
                employee.getPosition(),
                employee.isActive());
    }
}
