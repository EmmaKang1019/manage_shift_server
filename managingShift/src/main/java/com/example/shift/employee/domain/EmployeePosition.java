package com.example.shift.employee.domain;

import com.example.shift.team.domain.Position;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "employee_positions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "position_id"})
)
public class EmployeePosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;

    @Column(nullable = false)
    private boolean primaryPosition;

    @Column(nullable = false)
    private boolean active = true;

    protected EmployeePosition() {
    }

    public EmployeePosition(Employee employee, Position position, boolean primaryPosition) {
        this.employee = employee;
        this.position = position;
        this.primaryPosition = primaryPosition;
    }

    public Long getId() {
        return id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public Position getPosition() {
        return position;
    }

    public boolean isPrimaryPosition() {
        return primaryPosition;
    }

    public boolean isActive() {
        return active;
    }
}
