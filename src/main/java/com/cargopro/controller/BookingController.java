package com.cargopro.controller;

import com.cargopro.dto.BookingDto;
import com.cargopro.dto.PagedResponse;
import com.cargopro.enums.BookingStatus;
import com.cargopro.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // POST /booking - Create a new booking
    @PostMapping
    public ResponseEntity<BookingDto> createBooking(@Valid @RequestBody BookingDto bookingDto) {
        BookingDto createdBooking = bookingService.createBooking(bookingDto);
        return new ResponseEntity<>(createdBooking, HttpStatus.CREATED);
    }

    // GET /booking - Get bookings with pagination and filtering
    @GetMapping
    public ResponseEntity<PagedResponse<BookingDto>> getBookings(
            @RequestParam(required = false) UUID loadId,
            @RequestParam(required = false) String transporterId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        PagedResponse<BookingDto> bookings = bookingService.getBookings(loadId, transporterId, status, page, size);
        return ResponseEntity.ok(bookings);
    }

    // GET /booking/{bookingId} - Get booking details
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDto> getBookingById(@PathVariable UUID bookingId) {
        BookingDto booking = bookingService.getBookingById(bookingId);
        return ResponseEntity.ok(booking);
    }

    // PUT /booking/{bookingId} - Update booking details
    @PutMapping("/{bookingId}")
    public ResponseEntity<BookingDto> updateBooking(@PathVariable UUID bookingId, 
                                                   @Valid @RequestBody BookingDto bookingDto) {
        BookingDto updatedBooking = bookingService.updateBooking(bookingId, bookingDto);
        return ResponseEntity.ok(updatedBooking);
    }

    // DELETE /booking/{bookingId} - Delete a booking
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> deleteBooking(@PathVariable UUID bookingId) {
        bookingService.deleteBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    // POST /booking/{bookingId}/accept - Accept a booking
    @PostMapping("/{bookingId}/accept")
    public ResponseEntity<BookingDto> acceptBooking(@PathVariable UUID bookingId) {
        BookingDto acceptedBooking = bookingService.acceptBooking(bookingId);
        return ResponseEntity.ok(acceptedBooking);
    }

    // POST /booking/{bookingId}/reject - Reject a booking
    @PostMapping("/{bookingId}/reject")
    public ResponseEntity<BookingDto> rejectBooking(@PathVariable UUID bookingId) {
        BookingDto rejectedBooking = bookingService.rejectBooking(bookingId);
        return ResponseEntity.ok(rejectedBooking);
    }

    // GET /booking/load/{loadId} - Get all bookings for a specific load
    @GetMapping("/load/{loadId}")
    public ResponseEntity<List<BookingDto>> getBookingsByLoadId(@PathVariable UUID loadId) {
        List<BookingDto> bookings = bookingService.getBookingsByLoadId(loadId);
        return ResponseEntity.ok(bookings);
    }

    // GET /booking/load/{loadId}/active - Get active bookings for a specific load
    @GetMapping("/load/{loadId}/active")
    public ResponseEntity<List<BookingDto>> getActiveBookingsByLoadId(@PathVariable UUID loadId) {
        List<BookingDto> bookings = bookingService.getActiveBookingsByLoadId(loadId);
        return ResponseEntity.ok(bookings);
    }
} 