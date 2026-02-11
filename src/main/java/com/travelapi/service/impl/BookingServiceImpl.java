package com.travelapi.service.impl;

import com.travelapi.exception.InvalidInputException;
import com.travelapi.exception.ResourceNotFoundException;
import com.travelapi.model.Booking;
import com.travelapi.model.BookingStatus;
import com.travelapi.patterns.LoggingService;
import com.travelapi.repository.BookingRepository;
import com.travelapi.repository.CustomerRepository;
import com.travelapi.service.BookingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final LoggingService loggingService;

    public BookingServiceImpl(BookingRepository bookingRepository, CustomerRepository customerRepository, LoggingService loggingService) {
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
        this.loggingService = loggingService;
    }

    @Override
    public Booking createBooking(Booking booking) {
        loggingService.info("BookingService", "createBooking", "Creating booking for customer: " + booking.getCustomerId());
        booking.validate();
        customerRepository.findById(booking.getCustomerId()).orElseThrow(() -> new ResourceNotFoundException("Customer", booking.getCustomerId()));
        Booking created = bookingRepository.create(booking);
        loggingService.info("Booking created successfully with ID: " + created.getId());
        return created;
    }

    @Override
    public List<Booking> getAllBookings() {
        loggingService.info("BookingService", "getAllBookings", "Fetching all bookings");
        return bookingRepository.findAll();
    }

    @Override
    public Booking getBookingById(long id) {
        loggingService.info("BookingService", "getBookingById", "Fetching booking ID: " + id);
        return bookingRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Booking", id));
    }

    @Override
    public void confirmBooking(long id) {
        loggingService.info("BookingService", "confirmBooking", "Confirming booking ID: " + id);
        Booking booking = getBookingById(id);
        booking.confirm();
        bookingRepository.updateStatus(id, BookingStatus.CONFIRMED);
        loggingService.info("Booking confirmed: " + id);
    }

    @Override
    public void cancelBooking(long id) {
        loggingService.info("BookingService", "cancelBooking", "Cancelling booking ID: " + id);
        Booking booking = getBookingById(id);
        booking.cancel();
        bookingRepository.updateStatus(id, BookingStatus.CANCELLED);
        loggingService.info("Booking cancelled: " + id);
    }

    @Override
    public void deleteBooking(long id) {
        loggingService.info("BookingService", "deleteBooking", "Deleting booking ID: " + id);
        Booking booking = getBookingById(id);
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            throw new InvalidInputException("Cannot delete a confirmed booking. Please cancel it first.");
        }
        bookingRepository.delete(id);
        loggingService.info("Booking deleted: " + id);
    }

    @Override
    public List<Booking> getBookingsByCustomerId(long customerId) {
        loggingService.info("BookingService", "getBookingsByCustomerId", "Fetching bookings for customer: " + customerId);
        customerRepository.findById(customerId).orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));
        return bookingRepository.findByCustomerId(customerId);
    }

    @Override
    public List<Booking> getBookingsByStatus(BookingStatus status) {
        loggingService.info("BookingService", "getBookingsByStatus", "Fetching bookings with status: " + status);
        return bookingRepository.findByStatus(status);
    }
}
