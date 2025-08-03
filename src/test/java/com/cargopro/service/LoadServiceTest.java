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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoadServiceTest {

    @Mock
    private LoadRepository loadRepository;

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private LoadService loadService;

    private LoadDto testLoadDto;
    private Load testLoad;
    private UUID testLoadId;

    @BeforeEach
    void setUp() {
        testLoadId = UUID.randomUUID();
        
        // Create test FacilityDto
        FacilityDto facilityDto = new FacilityDto(
                "Mumbai",
                "Delhi",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        // Create test LoadDto
        testLoadDto = new LoadDto();
        testLoadDto.setShipperId("SHIPPER001");
        testLoadDto.setFacility(facilityDto);
        testLoadDto.setProductType("Electronics");
        testLoadDto.setTruckType("Container");
        testLoadDto.setNoOfTrucks(2);
        testLoadDto.setWeight(5000.0);
        testLoadDto.setComment("Test load");

        // Create test Load entity
        Facility facility = new Facility(
                "Mumbai",
                "Delhi",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        testLoad = new Load();
        testLoad.setId(testLoadId);
        testLoad.setShipperId("SHIPPER001");
        testLoad.setFacility(facility);
        testLoad.setProductType("Electronics");
        testLoad.setTruckType("Container");
        testLoad.setNoOfTrucks(2);
        testLoad.setWeight(5000.0);
        testLoad.setComment("Test load");
        testLoad.setStatus(LoadStatus.POSTED);
        testLoad.setDatePosted(LocalDateTime.now());
    }

    @Test
    void createLoad_Success() {
        // Arrange
        when(loadRepository.save(any(Load.class))).thenReturn(testLoad);

        // Act
        LoadDto result = loadService.createLoad(testLoadDto);

        // Assert
        assertNotNull(result);
        assertEquals(testLoadId, result.getId());
        assertEquals("SHIPPER001", result.getShipperId());
        assertEquals(LoadStatus.POSTED, result.getStatus());
        verify(loadRepository).save(any(Load.class));
    }

    @Test
    void getLoads_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Load> loads = Arrays.asList(testLoad);
        Page<Load> loadPage = new PageImpl<>(loads, pageable, 1);
        
        when(loadRepository.findLoadsWithFilters(any(), any(), any(), any())).thenReturn(loadPage);

        // Act
        PagedResponse<LoadDto> result = loadService.getLoads("SHIPPER001", "Container", LoadStatus.POSTED, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalElements());
        verify(loadRepository).findLoadsWithFilters("SHIPPER001", "Container", LoadStatus.POSTED, pageable);
    }

    @Test
    void getLoadById_Success() {
        // Arrange
        when(loadRepository.findById(testLoadId)).thenReturn(Optional.of(testLoad));

        // Act
        LoadDto result = loadService.getLoadById(testLoadId);

        // Assert
        assertNotNull(result);
        assertEquals(testLoadId, result.getId());
        assertEquals("SHIPPER001", result.getShipperId());
        verify(loadRepository).findById(testLoadId);
    }

    @Test
    void getLoadById_NotFound() {
        // Arrange
        when(loadRepository.findById(testLoadId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> loadService.getLoadById(testLoadId));
        verify(loadRepository).findById(testLoadId);
    }

    @Test
    void updateLoad_Success() {
        // Arrange
        LoadDto updateDto = new LoadDto();
        updateDto.setShipperId("SHIPPER002");
        updateDto.setFacility(testLoadDto.getFacility());
        updateDto.setProductType("Furniture");
        updateDto.setTruckType("Flatbed");
        updateDto.setNoOfTrucks(3);
        updateDto.setWeight(3000.0);
        updateDto.setComment("Updated load");

        when(loadRepository.findById(testLoadId)).thenReturn(Optional.of(testLoad));
        when(loadRepository.save(any(Load.class))).thenReturn(testLoad);

        // Act
        LoadDto result = loadService.updateLoad(testLoadId, updateDto);

        // Assert
        assertNotNull(result);
        verify(loadRepository).findById(testLoadId);
        verify(loadRepository).save(any(Load.class));
    }

    @Test
    void updateLoad_NotFound() {
        // Arrange
        when(loadRepository.findById(testLoadId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> loadService.updateLoad(testLoadId, testLoadDto));
        verify(loadRepository).findById(testLoadId);
        verify(loadRepository, never()).save(any(Load.class));
    }

    @Test
    void updateLoad_CancelledLoad() {
        // Arrange
        testLoad.setStatus(LoadStatus.CANCELLED);
        when(loadRepository.findById(testLoadId)).thenReturn(Optional.of(testLoad));

        // Act & Assert
        assertThrows(BusinessException.class, () -> loadService.updateLoad(testLoadId, testLoadDto));
        verify(loadRepository).findById(testLoadId);
        verify(loadRepository, never()).save(any(Load.class));
    }

    @Test
    void deleteLoad_Success() {
        // Arrange
        when(loadRepository.findById(testLoadId)).thenReturn(Optional.of(testLoad));
        doNothing().when(loadRepository).delete(testLoad);

        // Act
        loadService.deleteLoad(testLoadId);

        // Assert
        verify(loadRepository).findById(testLoadId);
        verify(loadRepository).delete(testLoad);
    }

    @Test
    void deleteLoad_NotFound() {
        // Arrange
        when(loadRepository.findById(testLoadId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> loadService.deleteLoad(testLoadId));
        verify(loadRepository).findById(testLoadId);
        verify(loadRepository, never()).delete(any(Load.class));
    }

    @Test
    void deleteLoad_WithActiveBookings() {
        // Arrange
        testLoad.setStatus(LoadStatus.BOOKED); // Simulate active bookings
        when(loadRepository.findById(testLoadId)).thenReturn(Optional.of(testLoad));

        // Act & Assert
        assertThrows(BusinessException.class, () -> loadService.deleteLoad(testLoadId));
        verify(loadRepository).findById(testLoadId);
        verify(loadRepository, never()).delete(any(Load.class));
    }

    @Test
    void updateLoadStatusToBooked_Success() {
        // Arrange
        when(loadRepository.findById(testLoadId)).thenReturn(Optional.of(testLoad));
        when(loadRepository.save(any(Load.class))).thenReturn(testLoad);

        // Act
        loadService.updateLoadStatusToBooked(testLoadId);

        // Assert
        verify(loadRepository).findById(testLoadId);
        verify(loadRepository).save(any(Load.class));
    }

    @Test
    void updateLoadStatusToCancelled_Success() {
        // Arrange
        when(loadRepository.findById(testLoadId)).thenReturn(Optional.of(testLoad));
        when(loadRepository.save(any(Load.class))).thenReturn(testLoad);

        // Act
        loadService.updateLoadStatusToCancelled(testLoadId);

        // Assert
        verify(loadRepository).findById(testLoadId);
        verify(loadRepository).save(any(Load.class));
    }

    @Test
    void revertLoadStatusToPosted_Success() {
        // Arrange
        when(loadRepository.findById(testLoadId)).thenReturn(Optional.of(testLoad));
        when(loadRepository.save(any(Load.class))).thenReturn(testLoad);

        // Act
        loadService.revertLoadStatusToPosted(testLoadId);

        // Assert
        verify(loadRepository).findById(testLoadId);
        verify(loadRepository).save(any(Load.class));
    }

    @Test
    void canAcceptBookings_Available() {
        // Arrange
        when(loadRepository.findById(testLoadId)).thenReturn(Optional.of(testLoad));

        // Act
        boolean result = loadService.canAcceptBookings(testLoadId);

        // Assert
        assertTrue(result);
        verify(loadRepository).findById(testLoadId);
    }

    @Test
    void canAcceptBookings_Cancelled() {
        // Arrange
        testLoad.setStatus(LoadStatus.CANCELLED);
        when(loadRepository.findById(testLoadId)).thenReturn(Optional.of(testLoad));

        // Act
        boolean result = loadService.canAcceptBookings(testLoadId);

        // Assert
        assertFalse(result);
        verify(loadRepository).findById(testLoadId);
    }

    @Test
    void canAcceptBookings_Booked() {
        // Arrange
        testLoad.setStatus(LoadStatus.BOOKED);
        when(loadRepository.findById(testLoadId)).thenReturn(Optional.of(testLoad));

        // Act
        boolean result = loadService.canAcceptBookings(testLoadId);

        // Assert
        assertFalse(result);
        verify(loadRepository).findById(testLoadId);
    }
} 