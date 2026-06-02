package com.example.shift.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureMockMvc
public class ShiftCoverageApiTest {
    @Autowired
    MockMvc mvc;

    @Test
    void scheduleEndpointTest() throws Exception{
        mvc.perform(get("/api/v1/schedule")
                .param("startDate","2026-06-01")
                .param("endDate","2026-06-07"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));
    }
    @Test
    void elegibleSubtitutesTest() throws Exception{
        mvc.perform(get("/api/v1/employees/{id}/eligible-substitutes",1))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));
    }

}
