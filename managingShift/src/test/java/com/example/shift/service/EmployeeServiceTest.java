package com.example.shift.service;

import com.example.shift.entity.Employee;
import com.example.shift.entity.EmployeeRole;
import com.example.shift.entity.Position;
import com.example.shift.entity.Team;
import com.example.shift.repository.EmployeeRepository;
import com.example.shift.repository.PositionRepository;
import com.example.shift.repository.TeamRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class EmployeeServiceTest {
    @Autowired
    EmployeeRepository employees;

    @Autowired
    PositionRepository positions;

    @Autowired
    TeamRepository teams;

    @Test
    void employeeCanBelongToMultipleTeams(){
        Position server = positions.save(new Position("Server"));
        Position kitchenHelper = positions.save(new Position("kitchenHelper"));
        Team closing = teams.save(new Team("Closing"));
        Team kitchen = teams.save(new Team("Kitchen"));
        Employee saved = employees.saveAndFlush(new Employee(
                "Mina",
                "mina@example.com",
                "778-723-1657",
                EmployeeRole.EMPLOYEE,
                Set.of(server,kitchenHelper),
                Set.of(kitchen, closing),
                true
        ));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTeams())
                .extracting(Team::getName)
                .containsExactlyInAnyOrder("Kitchen","Closing");
        assertThat(saved.getPositions()).extracting(Position::getName).containsExactlyInAnyOrder("Server","kitchenHelper");

    }
    @Test
    void findEligibleSubstitutesReturnsActiveEmployeesSharingTeam(){
        EmployeeService service = new EmployeeService(employees);

        Position server = positions.save(new Position("Server"));
        Position kitchenHelper = positions.save(new Position("kitchenHelper"));
        Team closing = teams.save(new Team("Closing"));
        Team kitchen = teams.save(new Team("Kitchen"));

        Employee requester = employees.save(new Employee(
                "Ben",
                "ben@example.com",
                "889-123-1245",
                EmployeeRole.EMPLOYEE,
                Set.of(server),
                Set.of(closing),
                true
        ));
        Employee cara = employees.saveAndFlush(new Employee(
                        "cara",
                        "cara@example.com",
                        "778-231-1245",
                        EmployeeRole.EMPLOYEE,
                        Set.of(server),
                        Set.of(kitchen),
                        true
                )
        );

        Employee inactive = employees.saveAndFlush(new Employee(
                "inactive",
                "in@example.com",
                "1234-123-0124",
                EmployeeRole.EMPLOYEE,
                Set.of(server),
                Set.of(closing),
                false
        ));
        Employee mina = employees.saveAndFlush(new Employee(
                "Mina",
                "mina@example.com",
                "604-555-0101",
                EmployeeRole.EMPLOYEE,
                Set.of(server),
                Set.of(closing),
                true
        ));

        assertThat(service.findEligibleSubstitutes(requester.getId()))
                .extracting(Employee::getName)
                .containsExactlyInAnyOrder("Mina");
    }

}
