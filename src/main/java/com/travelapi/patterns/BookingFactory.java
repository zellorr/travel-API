package com.travelapi.patterns;

import com.travelapi.model.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class BookingFactory {

    public enum BookingType {
        FLIGHT, HOTEL
    }

    public Booking createBooking(BookingType type, long id, LocalDate bookingDate, double totalPrice, BookingStatus status, long customerId) {
        switch (type) {
            case FLIGHT:
                return createDefaultFlightBooking(id, bookingDate, totalPrice, status, customerId);
            case HOTEL:
                return createDefaultHotelBooking(id, bookingDate, totalPrice, status, customerId);
            default:
                throw new IllegalArgumentException("Unknown booking type: " + type);
        }
    }

    public FlightBooking createFlightBooking(long id, LocalDate bookingDate, double totalPrice, BookingStatus status, long customerId, String flightNumber, String origin, String destination, SeatClass seatClass) {
        FlightBooking booking = new FlightBooking(id, bookingDate, totalPrice, status, customerId, flightNumber, origin, destination, seatClass);
        booking.validate();
        return booking;
    }

    public HotelBooking createHotelBooking(long id, LocalDate bookingDate, double totalPrice, BookingStatus status, long customerId, String hotelName, RoomType roomType, int nights) {
        HotelBooking booking = new HotelBooking(id, bookingDate, totalPrice, status, customerId, hotelName, roomType, nights);
        booking.validate();
        return booking;
    }

    public Booking createBookingFromType(String typeString) {
        BookingType type = BookingType.valueOf(typeString.toUpperCase());
        return createBooking(type, 0L, LocalDate.now().plusDays(1), 0.0, BookingStatus.PENDING, 0L);
    }

    private FlightBooking createDefaultFlightBooking(long id, LocalDate bookingDate, double totalPrice, BookingStatus status, long customerId) {
        return new FlightBooking(id, bookingDate, totalPrice, status, customerId, "DEFAULT", "ORIGIN", "DESTINATION", SeatClass.ECONOMY);
    }

    private HotelBooking createDefaultHotelBooking(long id, LocalDate bookingDate, double totalPrice, BookingStatus status, long customerId) {
        return new HotelBooking(id, bookingDate, totalPrice, status, customerId, "DEFAULT HOTEL", RoomType.STANDARD, 1);
    }
}
