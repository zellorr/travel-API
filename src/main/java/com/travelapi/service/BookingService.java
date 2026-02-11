package com.travelapi.service;

import com.travelapi.model.Booking;
import com.travelapi.model.BookingStatus;

import java.util.List;

public interface BookingService {

    Booking createBooking(Booking booking);

    List<Booking> getAllBookings();

    Booking getBookingById(long id);

    void confirmBooking(long id);

    void cancelBooking(long id);

    void deleteBooking(long id);

    List<Booking> getBookingsByCustomerId(long customerId);

    List<Booking> getBookingsByStatus(BookingStatus status);
}
