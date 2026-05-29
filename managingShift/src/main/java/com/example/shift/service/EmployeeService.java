package com.example.shift.service;

import com.example.shift.entity.Employee;
import com.example.shift.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {
    private final EmployeeRepository repo;

    public EmployeeService(EmployeeRepository repo){
        this.repo = repo;
    }
    public List<Employee> findEligibleSubstitutes(Long requestId){
        Employee requester = repo.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found : "+requestId));
        return repo.findByActiveTrue().stream()
                .filter(candidate -> !candidate.getId().equals(requester.getId()))
                .filter((candidate -> requester.sharesTeamWith(candidate)))
                .toList();
    }
}
