package com.cargopro.service;

import com.cargopro.dto.FacilityDto;
import com.cargopro.dto.LoadDto;
import com.cargopro.dto.PagedResponse;
import com.cargopro.entity.Facility;
import com.cargopro.entity.Load;
import com.cargopro.enums.LoadStatus;
import com.cargopro.exception.BusinessException;
import com.cargopro.exception.ResourceNotFoundException;
import com.cargopro.repository.LoadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class LoadService {

    @Autowired
    private LoadRepository loadRepository;

    @Autowired
    private BookingService bookingService;

    // Create a new load
    public LoadDto createLoad(LoadDto loadDto) {
        Load load = convertToEntity(loadDto);
        load.setStatus(LoadStatus.POSTED); // Default status
        Load savedLoad = loadRepository.save(load);
        return convertToDto(savedLoad);
    }

    // Get loads with pagination and filtering
    public PagedResponse<LoadDto> getLoads(String shipperId, String truckType, LoadStatus status, 
                                          int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Load> loadPage = loadRepository.findLoadsWithFilters(shipperId, truckType, status, pageable);
        
        List<LoadDto> loadDtos = loadPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                loadDtos,
                page,
                size,
                loadPage.getTotalElements(),
                loadPage.getTotalPages(),
                loadPage.hasNext(),
                loadPage.hasPrevious()
        );
    }

    // Get load by ID
    public LoadDto getLoadById(UUID loadId) {
        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load", "id", loadId));
        return convertToDto(load);
    }

    // Update load
    public LoadDto updateLoad(UUID loadId, LoadDto loadDto) {
        Load existingLoad = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load", "id", loadId));

        // Check if load can be updated (not cancelled)
        if (existingLoad.getStatus() == LoadStatus.CANCELLED) {
            throw new BusinessException("Cannot update a cancelled load");
        }

        // Update fields
        existingLoad.setShipperId(loadDto.getShipperId());
        existingLoad.setFacility(convertToFacilityEntity(loadDto.getFacility()));
        existingLoad.setProductType(loadDto.getProductType());
        existingLoad.setTruckType(loadDto.getTruckType());
        existingLoad.setNoOfTrucks(loadDto.getNoOfTrucks());
        existingLoad.setWeight(loadDto.getWeight());
        existingLoad.setComment(loadDto.getComment());

        Load updatedLoad = loadRepository.save(existingLoad);
        return convertToDto(updatedLoad);
    }

    // Delete load
    public void deleteLoad(UUID loadId) {
        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load", "id", loadId));

        // Check if load has active bookings
        if (load.hasActiveBookings()) {
            throw new BusinessException("Cannot delete load with active bookings");
        }

        loadRepository.delete(load);
    }

    // Update load status when booking is accepted
    public void updateLoadStatusToBooked(UUID loadId) {
        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load", "id", loadId));
        
        load.setStatus(LoadStatus.BOOKED);
        loadRepository.save(load);
    }

    // Update load status when booking is cancelled
    public void updateLoadStatusToCancelled(UUID loadId) {
        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load", "id", loadId));
        
        load.setStatus(LoadStatus.CANCELLED);
        loadRepository.save(load);
    }

    // Revert load status to POSTED when all bookings are deleted/rejected
    public void revertLoadStatusToPosted(UUID loadId) {
        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load", "id", loadId));
        
        if (!load.hasActiveBookings()) {
            load.setStatus(LoadStatus.POSTED);
            loadRepository.save(load);
        }
    }

    // Check if load can accept bookings
    public boolean canAcceptBookings(UUID loadId) {
        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load", "id", loadId));
        
        return load.getStatus() != LoadStatus.CANCELLED && load.getStatus() != LoadStatus.BOOKED;
    }

    // Convert DTO to Entity
    private Load convertToEntity(LoadDto loadDto) {
        Load load = new Load();
        load.setShipperId(loadDto.getShipperId());
        load.setFacility(convertToFacilityEntity(loadDto.getFacility()));
        load.setProductType(loadDto.getProductType());
        load.setTruckType(loadDto.getTruckType());
        load.setNoOfTrucks(loadDto.getNoOfTrucks());
        load.setWeight(loadDto.getWeight());
        load.setComment(loadDto.getComment());
        return load;
    }

    // Convert Entity to DTO
    private LoadDto convertToDto(Load load) {
        LoadDto loadDto = new LoadDto();
        loadDto.setId(load.getId());
        loadDto.setShipperId(load.getShipperId());
        loadDto.setFacility(convertToFacilityDto(load.getFacility()));
        loadDto.setProductType(load.getProductType());
        loadDto.setTruckType(load.getTruckType());
        loadDto.setNoOfTrucks(load.getNoOfTrucks());
        loadDto.setWeight(load.getWeight());
        loadDto.setComment(load.getComment());
        loadDto.setDatePosted(load.getDatePosted());
        loadDto.setStatus(load.getStatus());
        return loadDto;
    }

    // Convert Facility DTO to Entity
    private Facility convertToFacilityEntity(FacilityDto facilityDto) {
        return new Facility(
                facilityDto.getLoadingPoint(),
                facilityDto.getUnloadingPoint(),
                facilityDto.getLoadingDate(),
                facilityDto.getUnloadingDate()
        );
    }

    // Convert Facility Entity to DTO
    private FacilityDto convertToFacilityDto(Facility facility) {
        return new FacilityDto(
                facility.getLoadingPoint(),
                facility.getUnloadingPoint(),
                facility.getLoadingDate(),
                facility.getUnloadingDate()
        );
    }
} 