package com.travelapi.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDate;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = FlightBooking.class, name = "FLIGHT"),
        @JsonSubTypes.Type(value = HotelBooking.class, name = "HOTEL")
})
public abstract class Booking implements Validatable, Billable {

    private long id;
    private LocalDate bookingDate;
    private double totalPrice;
    private BookingStatus status;
    private long customerId;

    public Booking(long id, LocalDate bookingDate, double totalPrice, BookingStatus status, long customerId) {
        this.id = id;
        this.bookingDate = bookingDate;
        this.totalPrice = totalPrice;
        this.status = status;
        this.customerId = customerId;
    }

    public abstract String getBookingType();
    public abstract String getBookingDetails();

    public void confirm() {
        if (this.status == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot confirm a cancelled booking");
        }
        this.status = BookingStatus.CONFIRMED;
    }

    public void cancel() {
        if (this.status == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }
        this.status = BookingStatus.CANCELLED;
    }

    @Override
    public void validate() {
        if (bookingDate == null) {
            throw new IllegalArgumentException("Booking date cannot be null");
        }
        if (bookingDate.isBefore(LocalDate.now())) {
            throw new IllegalStateException("Booking date cannot be in the past");
        }
        if (totalPrice <= 0) {
            throw new IllegalStateException("Total price must be greater than 0");
        }
        if (customerId <= 0L) {
            throw new IllegalStateException("Customer ID must be valid");
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        if (id <= 0L) {
            throw new IllegalStateException("ID must be greater than 0");
        }
        this.id = id;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        if (bookingDate == null) {
            throw new IllegalStateException("Booking date cannot be null");
        }
        this.bookingDate = bookingDate;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        if (totalPrice <= 0) {
            throw new IllegalStateException("Total price must be greater than 0");
        }
        this.totalPrice = totalPrice;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        this.status = status;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        if (customerId <= 0L) {
            throw new IllegalStateException("Customer ID must be greater than 0");
        }
        this.customerId = customerId;
    }

    @Override
    public String toString() {
        return String.format("Booking[id=%d, type=%s, date=%s, price=%.2f, status=%s]", id, getBookingType(), bookingDate, totalPrice, status);
    }
}
