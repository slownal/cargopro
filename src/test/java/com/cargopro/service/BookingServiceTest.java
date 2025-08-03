package com.cargopro.service;

import com.cargopro.dto.BookingDto;
import com.cargopro.dto.PagedResponse;
import com.cargopro.entity.Booking;
import com.cargopro.enums.BookingStatus;
import com.cargopro.enums.LoadStatus;
import com.cargopro.exception.BusinessException;
import com.cargopro.exception.ResourceNotFoundException;
import com.cargopro.repository.BookingRepository;
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
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private LoadService loadService;

    @InjectMocks
    private BookingService bookingService;

    private BookingDto testBookingDto;
    private Booking testBooking;
    private UUID testBookingId;
    private UUID testLoadId;

    @BeforeEach
    void setUp() {
        testBookingId = UUID.randomUUID();
        testLoadId = UUID.randomUUID();

        // Create test BookingDto
        testBookingDto = new BookingDto();
        testBookingDto.setLoadId(testLoadId);
        testBookingDto.setTransporterId("TRANSPORTER001");
        testBookingDto.setProposedRate(5000.0);
        testBookingDto.setComment("Test booking");

        // Create test Booking entity
        testBooking = new Booking();
        testBooking.setId(testBookingId);
        testBooking.setLoadId(testLoadId);
        testBooking.setTransporterId("TRANSPORTER001");
        testBooking.setProposedRate(5000.0);
        testBooking.setComment("Test booking");
        testBooking.setStatus(BookingStatus.PENDING);
        testBooking.setRequestedAt(LocalDateTime.now());
    }

    @Test
    void createBooking_Success() {
        // Arrange
        when(loadService.canAcceptBookings(testLoadId)).thenReturn(true);
        when(bookingRepository.existsByLoadIdAndTransporterId(testLoadId, "TRANSPORTER001")).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        // Act
        BookingDto result = bookingService.createBooking(testBookingDto);

        // Assert
        assertNotNull(result);
        assertEquals(testBookingId, result.getId());
        assertEquals(BookingStatus.PENDING, result.getStatus());
        verify(loadService).canAcceptBookings(testLoadId);
        verify(bookingRepository).existsByLoadIdAndTransporterId(testLoadId, "TRANSPORTER001");
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_LoadNotAvailable() {
        // Arrange
        when(loadService.canAcceptBookings(testLoadId)).thenReturn(false);

        // Act & Assert
        assertThrows(BusinessException.class, () -> bookingService.createBooking(testBookingDto));
        verify(loadService).canAcceptBookings(testLoadId);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_TransporterAlreadyBooked() {
        // Arrange
        when(loadService.canAcceptBookings(testLoadId)).thenReturn(true);
        when(bookingRepository.existsByLoadIdAndTransporterId(testLoadId, "TRANSPORTER001")).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessException.class, () -> bookingService.createBooking(testBookingDto));
        verify(loadService).canAcceptBookings(testLoadId);
        verify(bookingRepository).existsByLoadIdAndTransporterId(testLoadId, "TRANSPORTER001");
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void getBookings_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Booking> bookings = Arrays.asList(testBooking);
        Page<Booking> bookingPage = new PageImpl<>(bookings, pageable, 1);
        
        when(bookingRepository.findBookingsWithFilters(any(), any(), any(), any())).thenReturn(bookingPage);

        // Act
        PagedResponse<BookingDto> result = bookingService.getBookings(testLoadId, "TRANSPORTER001", BookingStatus.PENDING, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalElements());
        verify(bookingRepository).findBookingsWithFilters(testLoadId, "TRANSPORTER001", BookingStatus.PENDING, pageable);
    }

    @Test
    void getBookingById_Success() {
        // Arrange
        when(bookingRepository.findById(testBookingId)).thenReturn(Optional.of(testBooking));

        // Act
        BookingDto result = bookingService.getBookingById(testBookingId);

        // Assert
        assertNotNull(result);
        assertEquals(testBookingId, result.getId());
        assertEquals("TRANSPORTER001", result.getTransporterId());
        verify(bookingRepository).findById(testBookingId);
    }

    @Test
    void getBookingById_NotFound() {
        // Arrange
        when(bookingRepository.findById(testBookingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> bookingService.getBookingById(testBookingId));
        verify(bookingRepository).findById(testBookingId);
    }

    @Test
    void updateBooking_Success() {
        // Arrange
        BookingDto updateDto = new BookingDto();
        updateDto.setTransporterId("TRANSPORTER002");
        updateDto.setProposedRate(6000.0);
        updateDto.setComment("Updated booking");

        when(bookingRepository.findById(testBookingId)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        // Act
        BookingDto result = bookingService.updateBooking(testBookingId, updateDto);

        // Assert
        assertNotNull(result);
        verify(bookingRepository).findById(testBookingId);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void updateBooking_NotFound() {
        // Arrange
        when(bookingRepository.findById(testBookingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> bookingService.updateBooking(testBookingId, testBookingDto));
        verify(bookingRepository).findById(testBookingId);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void updateBooking_RejectedBooking() {
        // Arrange
        testBooking.setStatus(BookingStatus.REJECTED);
        when(bookingRepository.findById(testBookingId)).thenReturn(Optional.of(testBooking));

        // Act & Assert
        assertThrows(BusinessException.class, () -> bookingService.updateBooking(testBookingId, testBookingDto));
        verify(bookingRepository).findById(testBookingId);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void deleteBooking_Success() {
        // Arrange
        when(bookingRepository.findById(testBookingId)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.countActiveBookingsByLoadId(testLoadId)).thenReturn(0L);
        doNothing().when(bookingRepository).delete(testBooking);

        // Act
        bookingService.deleteBooking(testBookingId);

        // Assert
        verify(bookingRepository).findById(testBookingId);
        verify(bookingRepository).delete(testBooking);
        verify(loadService).revertLoadStatusToPosted(testLoadId);
    }

    @Test
    void deleteBooking_NotFound() {
        // Arrange
        when(bookingRepository.findById(testBookingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> bookingService.deleteBooking(testBookingId));
        verify(bookingRepository).findById(testBookingId);
        verify(bookingRepository, never()).delete(any(Booking.class));
    }

    @Test
    void acceptBooking_Success() {
        // Arrange
        when(bookingRepository.findById(testBookingId)).thenReturn(Optional.of(testBooking));
        when(loadService.canAcceptBookings(testLoadId)).thenReturn(true);
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(bookingRepository.findByLoadIdAndStatus(testLoadId, BookingStatus.PENDING)).thenReturn(Arrays.asList(testBooking));

        // Act
        BookingDto result = bookingService.acceptBooking(testBookingId);

        // Assert
        assertNotNull(result);
        assertEquals(BookingStatus.ACCEPTED, result.getStatus());
        verify(bookingRepository).findById(testBookingId);
        verify(loadService).canAcceptBookings(testLoadId);
        verify(bookingRepository).save(any(Booking.class));
        verify(loadService).updateLoadStatusToBooked(testLoadId);
    }

    @Test
    void acceptBooking_NotPending() {
        // Arrange
        testBooking.setStatus(BookingStatus.ACCEPTED);
        when(bookingRepository.findById(testBookingId)).thenReturn(Optional.of(testBooking));

        // Act & Assert
        assertThrows(BusinessException.class, () -> bookingService.acceptBooking(testBookingId));
        verify(bookingRepository).findById(testBookingId);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void acceptBooking_LoadNotAvailable() {
        // Arrange
        when(bookingRepository.findById(testBookingId)).thenReturn(Optional.of(testBooking));
        when(loadService.canAcceptBookings(testLoadId)).thenReturn(false);

        // Act & Assert
        assertThrows(BusinessException.class, () -> bookingService.acceptBooking(testBookingId));
        verify(bookingRepository).findById(testBookingId);
        verify(loadService).canAcceptBookings(testLoadId);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void rejectBooking_Success() {
        // Arrange
        when(bookingRepository.findById(testBookingId)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(bookingRepository.countActiveBookingsByLoadId(testLoadId)).thenReturn(0L);

        // Act
        BookingDto result = bookingService.rejectBooking(testBookingId);

        // Assert
        assertNotNull(result);
        assertEquals(BookingStatus.REJECTED, result.getStatus());
        verify(bookingRepository).findById(testBookingId);
        verify(bookingRepository).save(any(Booking.class));
        verify(loadService).revertLoadStatusToPosted(testLoadId);
    }

    @Test
    void rejectBooking_NotPending() {
        // Arrange
        testBooking.setStatus(BookingStatus.ACCEPTED);
        when(bookingRepository.findById(testBookingId)).thenReturn(Optional.of(testBooking));

        // Act & Assert
        assertThrows(BusinessException.class, () -> bookingService.rejectBooking(testBookingId));
        verify(bookingRepository).findById(testBookingId);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void getBookingsByLoadId_Success() {
        // Arrange
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingRepository.findByLoadId(testLoadId)).thenReturn(bookings);

        // Act
        List<BookingDto> result = bookingService.getBookingsByLoadId(testLoadId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testBookingId, result.get(0).getId());
        verify(bookingRepository).findByLoadId(testLoadId);
    }

    @Test
    void getActiveBookingsByLoadId_Success() {
        // Arrange
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingRepository.findActiveBookingsByLoadId(testLoadId)).thenReturn(bookings);

        // Act
        List<BookingDto> result = bookingService.getActiveBookingsByLoadId(testLoadId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testBookingId, result.get(0).getId());
        verify(bookingRepository).findActiveBookingsByLoadId(testLoadId);
    }
} 