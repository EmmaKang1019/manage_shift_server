package com.example.shift.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private EmployeeRole role;

    @ManyToMany
    @JoinTable(
            name = "employee_position",
            joinColumns = @JoinColumn(name = "employee_id"),
            inverseJoinColumns = @JoinColumn(name = "position_id")
    )
    private Set<Position> positions = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "employee_team",
            joinColumns = @JoinColumn(name="employee_id"),
            inverseJoinColumns = @JoinColumn(name="team_id")
    )
    private Set<Team> teams = new HashSet<>();

    private boolean active;
    protected Employee(){}
    public Employee(
            String name,
            String email,
            String phoneNumber,
            EmployeeRole role,
            Set<Position> positions,
            Set<Team> teams,
            boolean active
    ){
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.positions = new HashSet<>(positions);
        this.teams = new HashSet<>(teams);
        this.active = active;
    }

    public Long getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public Set<Team> getTeams(){
        return teams;
    }

    public Set<Position> getPositions(){
        return positions;
    }
    public boolean sharesTeamWith(Employee other){
        return teams.stream().anyMatch(other.teams::contains);
    }
}
