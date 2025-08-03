package com.cargopro.repository;

import com.cargopro.entity.Load;
import com.cargopro.enums.LoadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LoadRepository extends JpaRepository<Load, UUID> {

    // Find loads by shipper ID
    Page<Load> findByShipperId(String shipperId, Pageable pageable);

    // Find loads by truck type
    Page<Load> findByTruckType(String truckType, Pageable pageable);

    // Find loads by status
    Page<Load> findByStatus(LoadStatus status, Pageable pageable);

    // Find loads by shipper ID and status
    Page<Load> findByShipperIdAndStatus(String shipperId, LoadStatus status, Pageable pageable);

    // Find loads by truck type and status
    Page<Load> findByTruckTypeAndStatus(String truckType, LoadStatus status, Pageable pageable);

    // Find loads by shipper ID and truck type
    Page<Load> findByShipperIdAndTruckType(String shipperId, String truckType, Pageable pageable);

    // Find loads by shipper ID, truck type, and status
    Page<Load> findByShipperIdAndTruckTypeAndStatus(String shipperId, String truckType, LoadStatus status, Pageable pageable);

    // Custom query for complex filtering
    @Query("SELECT l FROM Load l WHERE " +
           "(:shipperId IS NULL OR l.shipperId = :shipperId) AND " +
           "(:truckType IS NULL OR l.truckType = :truckType) AND " +
           "(:status IS NULL OR l.status = :status)")
    Page<Load> findLoadsWithFilters(
            @Param("shipperId") String shipperId,
            @Param("truckType") String truckType,
            @Param("status") LoadStatus status,
            Pageable pageable);

    // Find loads that have active bookings
    @Query("SELECT DISTINCT l FROM Load l JOIN l.bookings b WHERE b.status IN ('PENDING', 'ACCEPTED')")
    List<Load> findLoadsWithActiveBookings();

    // Find loads that have no active bookings
    @Query("SELECT l FROM Load l WHERE l.id NOT IN " +
           "(SELECT DISTINCT l2.id FROM Load l2 JOIN l2.bookings b WHERE b.status IN ('PENDING', 'ACCEPTED'))")
    List<Load> findLoadsWithoutActiveBookings();
} 