package com.cargopro.controller;

import com.cargopro.dto.LoadDto;
import com.cargopro.dto.PagedResponse;
import com.cargopro.enums.LoadStatus;
import com.cargopro.service.LoadService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/load")
public class LoadController {

    @Autowired
    private LoadService loadService;

    // POST /load - Create a new load
    @PostMapping
    public ResponseEntity<LoadDto> createLoad(@Valid @RequestBody LoadDto loadDto) {
        LoadDto createdLoad = loadService.createLoad(loadDto);
        return new ResponseEntity<>(createdLoad, HttpStatus.CREATED);
    }

    // GET /load - Get loads with pagination and filtering
    @GetMapping
    public ResponseEntity<PagedResponse<LoadDto>> getLoads(
            @RequestParam(required = false) String shipperId,
            @RequestParam(required = false) String truckType,
            @RequestParam(required = false) LoadStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        PagedResponse<LoadDto> loads = loadService.getLoads(shipperId, truckType, status, page, size);
        return ResponseEntity.ok(loads);
    }

    // GET /load/{loadId} - Get load details
    @GetMapping("/{loadId}")
    public ResponseEntity<LoadDto> getLoadById(@PathVariable UUID loadId) {
        LoadDto load = loadService.getLoadById(loadId);
        return ResponseEntity.ok(load);
    }

    // PUT /load/{loadId} - Update load details
    @PutMapping("/{loadId}")
    public ResponseEntity<LoadDto> updateLoad(@PathVariable UUID loadId, 
                                            @Valid @RequestBody LoadDto loadDto) {
        LoadDto updatedLoad = loadService.updateLoad(loadId, loadDto);
        return ResponseEntity.ok(updatedLoad);
    }

    // DELETE /load/{loadId} - Delete a load
    @DeleteMapping("/{loadId}")
    public ResponseEntity<Void> deleteLoad(@PathVariable UUID loadId) {
        loadService.deleteLoad(loadId);
        return ResponseEntity.noContent().build();
    }
} 