package com.travelapi.model;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.time.LocalDate;

@JsonTypeName("HOTEL")
public class HotelBooking extends Booking {

    private String hotelName;
    private RoomType roomType;
    private int nights;

    public HotelBooking() {
        super(0L, LocalDate.now(), 0.0, BookingStatus.PENDING, 0L);
    }

    public HotelBooking(long id, LocalDate bookingDate, double totalPrice, BookingStatus status, long customerId, String hotelName, RoomType roomType, int nights) {
        super(id, bookingDate, totalPrice, status, customerId);
        this.hotelName = hotelName;
        this.roomType = roomType;
        this.nights = nights;
    }

    @Override
    public String getBookingType() {
        return "HOTEL";
    }

    @Override
    public String getBookingDetails() {
        return String.format("Hotel: %s, %s room for %d night(s)", hotelName, roomType, nights);
    }

    @Override
    public double calculatePrice() {
        double pricePerNight = 0;
        switch (roomType) {
            case STANDARD:
                pricePerNight = 100;
                break;
            case DELUXE:
                pricePerNight = 200;
                break;
            case SUITE:
                pricePerNight = 400;
                break;
            case PRESIDENTIAL:
                pricePerNight = 1000;
                break;
        }
        return pricePerNight * nights;
    }

    @Override
    public void validate() {
        super.validate();
        if (hotelName == null || hotelName.trim().isEmpty()) {
            throw new IllegalArgumentException("Hotel name cannot be empty");
        }
        if (roomType == null) {
            throw new IllegalArgumentException("Room type cannot be null");
        }
        if (nights <= 0) {
            throw new IllegalArgumentException("Number of nights must be greater than 0");
        }
        if (nights > 365) {
            throw new IllegalArgumentException("Number of nights cannot exceed 365");
        }
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        if (hotelName == null || hotelName.trim().isEmpty()) {
            throw new IllegalArgumentException("Hotel name cannot be empty");
        }
        this.hotelName = hotelName;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        if (roomType == null) {
            throw new IllegalArgumentException("Room type cannot be null");
        }
        this.roomType = roomType;
    }

    public int getNights() {
        return nights;
    }

    public void setNights(int nights) {
        if (nights <= 0) {
            throw new IllegalArgumentException("Number of nights must be greater than 0");
        }
        if (nights > 365) {
            throw new IllegalArgumentException("Number of nights cannot exceed 365");
        }
        this.nights = nights;
    }

    @Override
    public String toString() {
        return String.format("HotelBooking[id=%d, hotel=%s, room=%s, nights=%d, date=%s, price=%.2f, status=%s]", getId(), hotelName, roomType, nights, getBookingDate(), getTotalPrice(), getStatus());
    }
}
