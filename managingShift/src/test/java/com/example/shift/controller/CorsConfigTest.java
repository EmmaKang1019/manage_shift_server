package com.example.shift.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:5173")
public class CorsConfigTest {

    @Autowired
    MockMvc mock;

    @Test
    public void apiAllowFront() throws Exception{
      mock.perform(options("/api/v1/schedule")
              .header("Origin", "http://localhost:5173")
              .header("Access-Control-Request-Method","GET"))
              .andExpect(status().isOk())
              .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));


    }

}
