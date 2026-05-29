package com.example.shift.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    protected Position(){

    }
    public Position(String name){
        this.name = name;
    }
    public Long getId(){
        return id;
    }

    public String getName(){
        return name;
    }
}
