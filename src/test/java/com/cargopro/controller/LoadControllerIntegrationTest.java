package com.cargopro.controller;

import com.cargopro.dto.FacilityDto;
import com.cargopro.dto.LoadDto;
import com.cargopro.enums.LoadStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class LoadControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @Test
    void createLoad_Success() throws Exception {
        // Arrange
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        FacilityDto facility = new FacilityDto(
                "Mumbai",
                "Delhi",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        LoadDto loadDto = new LoadDto();
        loadDto.setShipperId("SHIPPER001");
        loadDto.setFacility(facility);
        loadDto.setProductType("Electronics");
        loadDto.setTruckType("Container");
        loadDto.setNoOfTrucks(2);
        loadDto.setWeight(5000.0);
        loadDto.setComment("Test load");

        // Act & Assert
        mockMvc.perform(post("/api/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loadDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shipperId").value("SHIPPER001"))
                .andExpect(jsonPath("$.productType").value("Electronics"))
                .andExpect(jsonPath("$.status").value("POSTED"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void createLoad_ValidationError() throws Exception {
        // Arrange
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        LoadDto loadDto = new LoadDto();
        // Missing required fields

        // Act & Assert
        mockMvc.perform(post("/api/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loadDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    void getLoads_Success() throws Exception {
        // Arrange
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Act & Assert
        mockMvc.perform(get("/api/load")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    void getLoads_WithFilters() throws Exception {
        // Arrange
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Act & Assert
        mockMvc.perform(get("/api/load")
                        .param("shipperId", "SHIPPER001")
                        .param("truckType", "Container")
                        .param("status", "POSTED")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
    }

    @Test
    void getLoadById_Success() throws Exception {
        // Arrange
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // First create a load
        FacilityDto facility = new FacilityDto(
                "Mumbai",
                "Delhi",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        LoadDto loadDto = new LoadDto();
        loadDto.setShipperId("SHIPPER001");
        loadDto.setFacility(facility);
        loadDto.setProductType("Electronics");
        loadDto.setTruckType("Container");
        loadDto.setNoOfTrucks(2);
        loadDto.setWeight(5000.0);
        loadDto.setComment("Test load");

        String response = mockMvc.perform(post("/api/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loadDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LoadDto createdLoad = objectMapper.readValue(response, LoadDto.class);

        // Act & Assert
        mockMvc.perform(get("/api/load/{loadId}", createdLoad.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdLoad.getId().toString()))
                .andExpect(jsonPath("$.shipperId").value("SHIPPER001"));
    }

    @Test
    void getLoadById_NotFound() throws Exception {
        // Arrange
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        UUID nonExistentId = UUID.randomUUID();

        // Act & Assert
        mockMvc.perform(get("/api/load/{loadId}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void updateLoad_Success() throws Exception {
        // Arrange
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // First create a load
        FacilityDto facility = new FacilityDto(
                "Mumbai",
                "Delhi",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        LoadDto loadDto = new LoadDto();
        loadDto.setShipperId("SHIPPER001");
        loadDto.setFacility(facility);
        loadDto.setProductType("Electronics");
        loadDto.setTruckType("Container");
        loadDto.setNoOfTrucks(2);
        loadDto.setWeight(5000.0);
        loadDto.setComment("Test load");

        String response = mockMvc.perform(post("/api/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loadDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LoadDto createdLoad = objectMapper.readValue(response, LoadDto.class);

        // Update the load
        LoadDto updateDto = new LoadDto();
        updateDto.setShipperId("SHIPPER002");
        updateDto.setFacility(facility);
        updateDto.setProductType("Furniture");
        updateDto.setTruckType("Flatbed");
        updateDto.setNoOfTrucks(3);
        updateDto.setWeight(3000.0);
        updateDto.setComment("Updated load");

        // Act & Assert
        mockMvc.perform(put("/api/load/{loadId}", createdLoad.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shipperId").value("SHIPPER002"))
                .andExpect(jsonPath("$.productType").value("Furniture"));
    }

    @Test
    void deleteLoad_Success() throws Exception {
        // Arrange
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // First create a load
        FacilityDto facility = new FacilityDto(
                "Mumbai",
                "Delhi",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        LoadDto loadDto = new LoadDto();
        loadDto.setShipperId("SHIPPER001");
        loadDto.setFacility(facility);
        loadDto.setProductType("Electronics");
        loadDto.setTruckType("Container");
        loadDto.setNoOfTrucks(2);
        loadDto.setWeight(5000.0);
        loadDto.setComment("Test load");

        String response = mockMvc.perform(post("/api/load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loadDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LoadDto createdLoad = objectMapper.readValue(response, LoadDto.class);

        // Act & Assert
        mockMvc.perform(delete("/api/load/{loadId}", createdLoad.getId()))
                .andExpect(status().isNoContent());
    }
} 