package com.travelapi.controller;

import com.travelapi.dto.FlightBookingRequest;
import com.travelapi.dto.HotelBookingRequest;
import com.travelapi.model.Booking;
import com.travelapi.model.BookingStatus;
import com.travelapi.patterns.BookingBuilder;
import com.travelapi.patterns.LoggingService;
import com.travelapi.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final LoggingService loggingService;

    public BookingController(BookingService bookingService, LoggingService loggingService) {
        this.bookingService = bookingService;
        this.loggingService = loggingService;
    }

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings(HttpServletRequest request) {
        loggingService.logApiRequest("/api/bookings", "GET", request.getRemoteAddr());
        List<Booking> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable long id,
                                                  HttpServletRequest request) {
        loggingService.logApiRequest("/api/bookings/" + id, "GET", request.getRemoteAddr());
        Booking booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(booking);
    }

    @PostMapping("/flight")
    public ResponseEntity<Booking> createFlightBooking(@RequestBody FlightBookingRequest request,
                                                       HttpServletRequest httpRequest) {
        loggingService.logApiRequest("/api/bookings/flight", "POST", httpRequest.getRemoteAddr());
        Booking booking = BookingBuilder.flightBooking()
                .withBookingDate(request.getBookingDate())
                .withTotalPrice(request.getTotalPrice())
                .withStatus(request.getStatus() != null ? request.getStatus() : BookingStatus.PENDING)
                .withCustomerId(request.getCustomerId())
                .withFlightNumber(request.getFlightNumber())
                .withOrigin(request.getOrigin())
                .withDestination(request.getDestination())
                .withSeatClass(request.getSeatClass())
                .buildFlight();
        Booking created = bookingService.createBooking(booking);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/hotel")
    public ResponseEntity<Booking> createHotelBooking(@RequestBody HotelBookingRequest request,
                                                      HttpServletRequest httpRequest) {
        loggingService.logApiRequest("/api/bookings/hotel", "POST", httpRequest.getRemoteAddr());
        Booking booking = BookingBuilder.hotelBooking()
                .withBookingDate(request.getBookingDate())
                .withTotalPrice(request.getTotalPrice())
                .withStatus(request.getStatus() != null ? request.getStatus() : BookingStatus.PENDING)
                .withCustomerId(request.getCustomerId())
                .withHotelName(request.getHotelName())
                .withRoomType(request.getRoomType())
                .withNights(request.getNights())
                .buildHotel();
        Booking created = bookingService.createBooking(booking);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<Map<String, String>> confirmBooking(@PathVariable long id,
                                                              HttpServletRequest request) {
        loggingService.logApiRequest("/api/bookings/" + id + "/confirm", "PUT", request.getRemoteAddr());
        bookingService.confirmBooking(id);
        return ResponseEntity.ok(Map.of(
                "message", "Booking confirmed successfully",
                "id", String.valueOf(id)
        ));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Map<String, String>> cancelBooking(@PathVariable long id,
                                                             HttpServletRequest request) {
        loggingService.logApiRequest("/api/bookings/" + id + "/cancel", "PUT", request.getRemoteAddr());
        bookingService.cancelBooking(id);
        return ResponseEntity.ok(Map.of(
                "message", "Booking cancelled successfully",
                "id", String.valueOf(id)
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable long id,
                                              HttpServletRequest request) {
        loggingService.logApiRequest("/api/bookings/" + id, "DELETE", request.getRemoteAddr());
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Booking>> getBookingsByCustomer(@PathVariable long customerId,
                                                               HttpServletRequest request) {
        loggingService.logApiRequest(
                "/api/bookings/customer/" + customerId,
                "GET",
                request.getRemoteAddr()
        );
        List<Booking> bookings = bookingService.getBookingsByCustomerId(customerId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Booking>> getBookingsByStatus(@PathVariable BookingStatus status,
                                                             HttpServletRequest request) {
        loggingService.logApiRequest(
                "/api/bookings/status/" + status,
                "GET",
                request.getRemoteAddr()
        );
        List<Booking> bookings = bookingService.getBookingsByStatus(status);
        return ResponseEntity.ok(bookings);
    }
}
