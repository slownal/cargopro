package com.cargopro.service;

import com.cargopro.dto.BookingDto;
import com.cargopro.dto.PagedResponse;
import com.cargopro.entity.Booking;
import com.cargopro.enums.BookingStatus;
import com.cargopro.exception.BusinessException;
import com.cargopro.exception.ResourceNotFoundException;
import com.cargopro.repository.BookingRepository;
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
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private LoadService loadService;

    // Create a new booking
    public BookingDto createBooking(BookingDto bookingDto) {
        // Check if load exists and can accept bookings
        if (!loadService.canAcceptBookings(bookingDto.getLoadId())) {
            throw new BusinessException("Load is not available for booking");
        }

        // Check if transporter has already booked this load
        if (bookingRepository.existsByLoadIdAndTransporterId(bookingDto.getLoadId(), bookingDto.getTransporterId())) {
            throw new BusinessException("Transporter has already booked this load");
        }

        Booking booking = convertToEntity(bookingDto);
        booking.setStatus(BookingStatus.PENDING); // Default status
        Booking savedBooking = bookingRepository.save(booking);
        return convertToDto(savedBooking);
    }

    // Get bookings with pagination and filtering
    public PagedResponse<BookingDto> getBookings(UUID loadId, String transporterId, BookingStatus status, 
                                                 int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> bookingPage = bookingRepository.findBookingsWithFilters(loadId, transporterId, status, pageable);
        
        List<BookingDto> bookingDtos = bookingPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                bookingDtos,
                page,
                size,
                bookingPage.getTotalElements(),
                bookingPage.getTotalPages(),
                bookingPage.hasNext(),
                bookingPage.hasPrevious()
        );
    }

    // Get booking by ID
    public BookingDto getBookingById(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));
        return convertToDto(booking);
    }

    // Update booking
    public BookingDto updateBooking(UUID bookingId, BookingDto bookingDto) {
        Booking existingBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        // Check if booking can be updated (not rejected)
        if (existingBooking.getStatus() == BookingStatus.REJECTED) {
            throw new BusinessException("Cannot update a rejected booking");
        }

        // Update fields
        existingBooking.setTransporterId(bookingDto.getTransporterId());
        existingBooking.setProposedRate(bookingDto.getProposedRate());
        existingBooking.setComment(bookingDto.getComment());

        Booking updatedBooking = bookingRepository.save(existingBooking);
        return convertToDto(updatedBooking);
    }

    // Delete booking
    public void deleteBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        UUID loadId = booking.getLoadId();
        bookingRepository.delete(booking);

        // Update load status if needed
        updateLoadStatusAfterBookingDeletion(loadId);
    }

    // Accept booking
    public BookingDto acceptBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        // Check if booking is in PENDING status
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessException("Only pending bookings can be accepted");
        }

        // Check if load is still available
        if (!loadService.canAcceptBookings(booking.getLoadId())) {
            throw new BusinessException("Load is no longer available for booking");
        }

        // Accept the booking
        booking.setStatus(BookingStatus.ACCEPTED);
        Booking savedBooking = bookingRepository.save(booking);

        // Update load status to BOOKED
        loadService.updateLoadStatusToBooked(booking.getLoadId());

        // Reject all other pending bookings for the same load
        rejectOtherPendingBookings(booking.getLoadId(), bookingId);

        return convertToDto(savedBooking);
    }

    // Reject booking
    public BookingDto rejectBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        // Check if booking is in PENDING status
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessException("Only pending bookings can be rejected");
        }

        booking.setStatus(BookingStatus.REJECTED);
        Booking savedBooking = bookingRepository.save(booking);

        // Update load status if needed
        updateLoadStatusAfterBookingDeletion(booking.getLoadId());

        return convertToDto(savedBooking);
    }

    // Get all bookings for a specific load
    public List<BookingDto> getBookingsByLoadId(UUID loadId) {
        List<Booking> bookings = bookingRepository.findByLoadId(loadId);
        return bookings.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Get active bookings for a specific load
    public List<BookingDto> getActiveBookingsByLoadId(UUID loadId) {
        List<Booking> bookings = bookingRepository.findActiveBookingsByLoadId(loadId);
        return bookings.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Private helper methods
    private void rejectOtherPendingBookings(UUID loadId, UUID acceptedBookingId) {
        List<Booking> pendingBookings = bookingRepository.findByLoadIdAndStatus(loadId, BookingStatus.PENDING);
        for (Booking booking : pendingBookings) {
            if (!booking.getId().equals(acceptedBookingId)) {
                booking.setStatus(BookingStatus.REJECTED);
                bookingRepository.save(booking);
            }
        }
    }

    private void updateLoadStatusAfterBookingDeletion(UUID loadId) {
        long activeBookingsCount = bookingRepository.countActiveBookingsByLoadId(loadId);
        if (activeBookingsCount == 0) {
            loadService.revertLoadStatusToPosted(loadId);
        }
    }

    // Convert DTO to Entity
    private Booking convertToEntity(BookingDto bookingDto) {
        Booking booking = new Booking();
        booking.setLoadId(bookingDto.getLoadId());
        booking.setTransporterId(bookingDto.getTransporterId());
        booking.setProposedRate(bookingDto.getProposedRate());
        booking.setComment(bookingDto.getComment());
        return booking;
    }

    // Convert Entity to DTO
    private BookingDto convertToDto(Booking booking) {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(booking.getId());
        bookingDto.setLoadId(booking.getLoadId());
        bookingDto.setTransporterId(booking.getTransporterId());
        bookingDto.setProposedRate(booking.getProposedRate());
        bookingDto.setComment(booking.getComment());
        bookingDto.setStatus(booking.getStatus());
        bookingDto.setRequestedAt(booking.getRequestedAt());
        return bookingDto;
    }
} 