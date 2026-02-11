package com.travelapi.model;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.time.LocalDate;

@JsonTypeName("FLIGHT")
public class FlightBooking extends Booking {

    private String flightNumber;
    private String origin;
    private String destination;
    private SeatClass seatClass;

    public FlightBooking() {
        super(0L, LocalDate.now(), 0.0, BookingStatus.PENDING, 0L);
    }

    public FlightBooking(long id, LocalDate bookingDate, double totalPrice, BookingStatus status, long customerId, String flightNumber, String origin, String destination, SeatClass seatClass) {
        super(id, bookingDate, totalPrice, status, customerId);
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.seatClass = seatClass;
    }

    @Override
    public String getBookingType() {
        return "FLIGHT";
    }

    @Override
    public String getBookingDetails() {
        return String.format("Flight %s from %s to %s (%s class)", flightNumber, origin, destination, seatClass);
    }

    @Override
    public double calculatePrice() {
        double basePrice = getTotalPrice();
        switch (seatClass) {
            case ECONOMY:
                return basePrice;
            case BUSINESS:
                return basePrice * 2.5;
            case FIRST_CLASS:
                return basePrice * 4.0;
            default:
                return basePrice;
        }
    }

    @Override
    public void validate() {
        super.validate();
        if (flightNumber == null || flightNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Flight number cannot be empty");
        }
        if (origin == null || origin.trim().isEmpty()) {
            throw new IllegalArgumentException("Origin cannot be empty");
        }
        if (destination == null || destination.trim().isEmpty()) {
            throw new IllegalArgumentException("Destination cannot be empty");
        }
        if (origin.equalsIgnoreCase(destination)) {
            throw new IllegalArgumentException("Origin and destination cannot be the same");
        }
        if (seatClass == null) {
            throw new IllegalArgumentException("Seat class cannot be null");
        }
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        if (flightNumber == null || flightNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Flight number cannot be empty");
        }
        this.flightNumber = flightNumber;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        if (origin == null || origin.trim().isEmpty()) {
            throw new IllegalArgumentException("Origin cannot be empty");
        }
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        if (destination == null || destination.trim().isEmpty()) {
            throw new IllegalArgumentException("Destination cannot be empty");
        }
        this.destination = destination;
    }

    public SeatClass getSeatClass() {
        return seatClass;
    }

    public void setSeatClass(SeatClass seatClass) {
        if (seatClass == null) {
            throw new IllegalArgumentException("Seat class cannot be null");
        }
        this.seatClass = seatClass;
    }

    @Override
    public String toString() {
        return String.format("FlightBooking[id=%d, flight=%s, %s→%s, class=%s, date=%s, price=%.2f, status=%s]", getId(), flightNumber, origin, destination, seatClass, getBookingDate(), getTotalPrice(), getStatus());
    }
}
