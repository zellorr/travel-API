package com.travelapi.patterns;

import com.travelapi.model.*;

import java.time.LocalDate;

public class BookingBuilder {

    private long id = 0L;
    private LocalDate bookingDate;
    private double totalPrice;
    private BookingStatus status = BookingStatus.PENDING;
    private long customerId;
    private String flightNumber;
    private String origin;
    private String destination;
    private SeatClass seatClass = SeatClass.ECONOMY;
    private String hotelName;
    private RoomType roomType = RoomType.STANDARD;
    private int nights = 1;

    private BookingBuilder() {
    }

    public static BookingBuilder flightBooking() {
        return new BookingBuilder();
    }

    public static BookingBuilder hotelBooking() {
        return new BookingBuilder();
    }

    public BookingBuilder withId(long id) {
        this.id = id;
        return this;
    }

    public BookingBuilder withBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
        return this;
    }

    public BookingBuilder withTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
        return this;
    }

    public BookingBuilder withStatus(BookingStatus status) {
        this.status = status;
        return this;
    }

    public BookingBuilder withCustomerId(long customerId) {
        this.customerId = customerId;
        return this;
    }

    public BookingBuilder withFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
        return this;
    }

    public BookingBuilder withOrigin(String origin) {
        this.origin = origin;
        return this;
    }

    public BookingBuilder withDestination(String destination) {
        this.destination = destination;
        return this;
    }

    public BookingBuilder withSeatClass(SeatClass seatClass) {
        this.seatClass = seatClass;
        return this;
    }

    public BookingBuilder withHotelName(String hotelName) {
        this.hotelName = hotelName;
        return this;
    }

    public BookingBuilder withRoomType(RoomType roomType) {
        this.roomType = roomType;
        return this;
    }

    public BookingBuilder withNights(int nights) {
        this.nights = nights;
        return this;
    }

    public FlightBooking buildFlight() {
        validateCommonFields();
        validateFlightFields();
        return new FlightBooking(id, bookingDate, totalPrice, status, customerId, flightNumber, origin, destination, seatClass);
    }

    public HotelBooking buildHotel() {
        validateCommonFields();
        validateHotelFields();
        return new HotelBooking(id, bookingDate, totalPrice, status, customerId, hotelName, roomType, nights);
    }

    private void validateCommonFields() {
        if (bookingDate == null) {
            throw new IllegalStateException("Booking date is required");
        }
        if (customerId <= 0L) {
            throw new IllegalStateException("Valid customer ID is required");
        }
    }

    private void validateFlightFields() {
        if (flightNumber == null || flightNumber.trim().isEmpty()) {
            throw new IllegalStateException("Flight number is required");
        }
        if (origin == null || origin.trim().isEmpty()) {
            throw new IllegalStateException("Origin is required");
        }
        if (destination == null || destination.trim().isEmpty()) {
            throw new IllegalStateException("Destination is required");
        }
    }

    private void validateHotelFields() {
        if (hotelName == null || hotelName.trim().isEmpty()) {
            throw new IllegalStateException("Hotel name is required");
        }
        if (nights <= 0) {
            throw new IllegalStateException("Number of nights must be greater than 0");
        }
    }
}
