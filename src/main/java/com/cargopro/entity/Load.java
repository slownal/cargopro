package com.cargopro.entity;

import com.cargopro.enums.LoadStatus;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "loads")
public class Load {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = "Shipper ID is required")
    @Column(name = "shipper_id", nullable = false)
    private String shipperId;

    @Valid
    @Embedded
    private Facility facility;

    @NotBlank(message = "Product type is required")
    @Column(name = "product_type", nullable = false)
    private String productType;

    @NotBlank(message = "Truck type is required")
    @Column(name = "truck_type", nullable = false)
    private String truckType;

    @Min(value = 1, message = "Number of trucks must be at least 1")
    @Column(name = "no_of_trucks", nullable = false)
    private Integer noOfTrucks;

    @Positive(message = "Weight must be positive")
    @Column(name = "weight", nullable = false)
    private Double weight;

    @Column(name = "comment", length = 1000)
    private String comment;

    @Column(name = "date_posted", nullable = false)
    private LocalDateTime datePosted;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LoadStatus status = LoadStatus.POSTED;

    @OneToMany(mappedBy = "load", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();

    // Default constructor
    public Load() {
        this.datePosted = LocalDateTime.now();
    }

    // Constructor with required fields
    public Load(String shipperId, Facility facility, String productType, String truckType, 
                Integer noOfTrucks, Double weight, String comment) {
        this();
        this.shipperId = shipperId;
        this.facility = facility;
        this.productType = productType;
        this.truckType = truckType;
        this.noOfTrucks = noOfTrucks;
        this.weight = weight;
        this.comment = comment;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getShipperId() {
        return shipperId;
    }

    public void setShipperId(String shipperId) {
        this.shipperId = shipperId;
    }

    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getTruckType() {
        return truckType;
    }

    public void setTruckType(String truckType) {
        this.truckType = truckType;
    }

    public Integer getNoOfTrucks() {
        return noOfTrucks;
    }

    public void setNoOfTrucks(Integer noOfTrucks) {
        this.noOfTrucks = noOfTrucks;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getDatePosted() {
        return datePosted;
    }

    public void setDatePosted(LocalDateTime datePosted) {
        this.datePosted = datePosted;
    }

    public LoadStatus getStatus() {
        return status;
    }

    public void setStatus(LoadStatus status) {
        this.status = status;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }

    // Business methods
    public void addBooking(Booking booking) {
        bookings.add(booking);
        booking.setLoad(this);
    }

    public void removeBooking(Booking booking) {
        bookings.remove(booking);
        booking.setLoad(null);
    }

    public boolean hasAcceptedBooking() {
        return bookings.stream()
                .anyMatch(booking -> booking.getStatus() == com.cargopro.enums.BookingStatus.ACCEPTED);
    }

    public boolean hasActiveBookings() {
        return bookings.stream()
                .anyMatch(booking -> booking.getStatus() == com.cargopro.enums.BookingStatus.PENDING ||
                                   booking.getStatus() == com.cargopro.enums.BookingStatus.ACCEPTED);
    }

    @Override
    public String toString() {
        return "Load{" +
                "id=" + id +
                ", shipperId='" + shipperId + '\'' +
                ", facility=" + facility +
                ", productType='" + productType + '\'' +
                ", truckType='" + truckType + '\'' +
                ", noOfTrucks=" + noOfTrucks +
                ", weight=" + weight +
                ", comment='" + comment + '\'' +
                ", datePosted=" + datePosted +
                ", status=" + status +
                '}';
    }
} 