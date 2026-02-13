package com.travelapi.service.impl;

import com.travelapi.exception.ResourceNotFoundException;
import com.travelapi.model.Booking;
import com.travelapi.model.BookingStatus;
import com.travelapi.patterns.CacheManager;
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
    private final CacheManager cacheManager;
    private final LoggingService loggingService;

    private static final String CACHE_ALL_BOOKINGS = "bookings:all";
    private static final String CACHE_BOOKING_PREFIX = "booking:";
    private static final String CACHE_CUSTOMER_BOOKINGS_PREFIX = "bookings:customer:";
    private static final String CACHE_STATUS_BOOKINGS_PREFIX = "bookings:status:";

    public BookingServiceImpl(BookingRepository bookingRepository,
                              CustomerRepository customerRepository,
                              CacheManager cacheManager,
                              LoggingService loggingService) {
        this.bookingRepository = bookingRepository;
        this.customerRepository = customerRepository;
        this.cacheManager = cacheManager;
        this.loggingService = loggingService;
    }

    @Override
    public Booking createBooking(Booking booking) {
        booking.validate();

        customerRepository.findById(booking.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", booking.getCustomerId()));

        Booking created = bookingRepository.create(booking);

        invalidateAllBookingsCache();

        loggingService.logDatabaseOperation("CREATE", "Booking", true);
        loggingService.info("Booking created with ID: " + created.getId() + ", cache invalidated");

        return created;
    }

    @Override
    public List<Booking> getAllBookings() {
        List<Booking> cachedBookings = cacheManager.get(CACHE_ALL_BOOKINGS);

        if (cachedBookings != null) {
            loggingService.info("getAllBookings() - Cache HIT");
            return cachedBookings;
        }

        loggingService.info("getAllBookings() - Cache MISS, querying database");
        List<Booking> bookings = bookingRepository.findAll();

        cacheManager.put(CACHE_ALL_BOOKINGS, bookings);
        loggingService.info("getAllBookings() - Cached " + bookings.size() + " bookings");

        return bookings;
    }

    @Override
    public Booking getBookingById(long id) {
        String cacheKey = CACHE_BOOKING_PREFIX + id;

        Booking cachedBooking = cacheManager.get(cacheKey);

        if (cachedBooking != null) {
            loggingService.info("getBookingById(" + id + ") - Cache HIT");
            return cachedBooking;
        }

        loggingService.info("getBookingById(" + id + ") - Cache MISS");
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));

        cacheManager.put(cacheKey, booking);

        return booking;
    }

    @Override
    public void confirmBooking(long id) {
        Booking booking = getBookingById(id);
        booking.confirm();

        bookingRepository.updateStatus(id, BookingStatus.CONFIRMED);

        invalidateBookingCache(id);
        invalidateAllBookingsCache();

        loggingService.info("Booking " + id + " confirmed, cache invalidated");
    }

    @Override
    public void cancelBooking(long id) {
        Booking booking = getBookingById(id);
        booking.cancel();

        bookingRepository.updateStatus(id, BookingStatus.CANCELLED);

        invalidateBookingCache(id);
        invalidateAllBookingsCache();

        loggingService.info("Booking " + id + " cancelled, cache invalidated");
    }

    @Override
    public void deleteBooking(long id) {
        getBookingById(id);

        bookingRepository.delete(id);

        invalidateBookingCache(id);
        invalidateAllBookingsCache();

        loggingService.logDatabaseOperation("DELETE", "Booking", true);
        loggingService.info("Booking " + id + " deleted, cache invalidated");
    }

    @Override
    public List<Booking> getBookingsByCustomerId(long customerId) {
        String cacheKey = CACHE_CUSTOMER_BOOKINGS_PREFIX + customerId;

        List<Booking> cachedBookings = cacheManager.get(cacheKey);

        if (cachedBookings != null) {
            loggingService.info("getBookingsByCustomerId(" + customerId + ") - Cache HIT");
            return cachedBookings;
        }

        loggingService.info("getBookingsByCustomerId(" + customerId + ") - Cache MISS");
        List<Booking> bookings = bookingRepository.findByCustomerId(customerId);

        cacheManager.put(cacheKey, bookings);

        return bookings;
    }

    @Override
    public List<Booking> getBookingsByStatus(BookingStatus status) {
        String cacheKey = CACHE_STATUS_BOOKINGS_PREFIX + status.name();

        List<Booking> cachedBookings = cacheManager.get(cacheKey);

        if (cachedBookings != null) {
            loggingService.info("getBookingsByStatus(" + status + ") - Cache HIT");
            return cachedBookings;
        }

        loggingService.info("getBookingsByStatus(" + status + ") - Cache MISS");
        List<Booking> bookings = bookingRepository.findByStatus(status);

        cacheManager.put(cacheKey, bookings);

        return bookings;
    }

    private void invalidateBookingCache(long id) {
        String cacheKey = CACHE_BOOKING_PREFIX + id;
        cacheManager.evict(cacheKey);
        loggingService.debug("Invalidated cache for booking: " + id);
    }

    private void invalidateAllBookingsCache() {
        cacheManager.evict(CACHE_ALL_BOOKINGS);
        cacheManager.evictByPattern(CACHE_CUSTOMER_BOOKINGS_PREFIX + "*");
        cacheManager.evictByPattern(CACHE_STATUS_BOOKINGS_PREFIX + "*");
        loggingService.debug("Invalidated all bookings cache");
    }
}
