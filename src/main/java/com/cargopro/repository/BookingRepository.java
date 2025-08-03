package com.cargopro.repository;

import com.cargopro.entity.Booking;
import com.cargopro.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    // Find bookings by load ID
    Page<Booking> findByLoadId(UUID loadId, Pageable pageable);

    // Find bookings by transporter ID
    Page<Booking> findByTransporterId(String transporterId, Pageable pageable);

    // Find bookings by status
    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    // Find bookings by load ID and status
    Page<Booking> findByLoadIdAndStatus(UUID loadId, BookingStatus status, Pageable pageable);

    // Find bookings by transporter ID and status
    Page<Booking> findByTransporterIdAndStatus(String transporterId, BookingStatus status, Pageable pageable);

    // Find bookings by load ID and transporter ID
    Page<Booking> findByLoadIdAndTransporterId(UUID loadId, String transporterId, Pageable pageable);

    // Find bookings by load ID, transporter ID, and status
    Page<Booking> findByLoadIdAndTransporterIdAndStatus(UUID loadId, String transporterId, BookingStatus status, Pageable pageable);

    // Custom query for complex filtering
    @Query("SELECT b FROM Booking b WHERE " +
           "(:loadId IS NULL OR b.loadId = :loadId) AND " +
           "(:transporterId IS NULL OR b.transporterId = :transporterId) AND " +
           "(:status IS NULL OR b.status = :status)")
    Page<Booking> findBookingsWithFilters(
            @Param("loadId") UUID loadId,
            @Param("transporterId") String transporterId,
            @Param("status") BookingStatus status,
            Pageable pageable);

    // Find all bookings for a specific load
    List<Booking> findByLoadId(UUID loadId);

    // Find active bookings for a specific load
    @Query("SELECT b FROM Booking b WHERE b.loadId = :loadId AND b.status IN ('PENDING', 'ACCEPTED')")
    List<Booking> findActiveBookingsByLoadId(@Param("loadId") UUID loadId);

    // Find accepted bookings for a specific load
    List<Booking> findByLoadIdAndStatus(UUID loadId, BookingStatus status);

    // Count active bookings for a specific load
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.loadId = :loadId AND b.status IN ('PENDING', 'ACCEPTED')")
    long countActiveBookingsByLoadId(@Param("loadId") UUID loadId);

    // Check if a transporter has already booked a specific load
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.loadId = :loadId AND b.transporterId = :transporterId")
    boolean existsByLoadIdAndTransporterId(@Param("loadId") UUID loadId, @Param("transporterId") String transporterId);
} 