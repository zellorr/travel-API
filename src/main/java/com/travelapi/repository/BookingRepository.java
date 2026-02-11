package com.travelapi.repository;

import com.travelapi.model.Booking;
import com.travelapi.model.BookingStatus;

import java.util.List;

public interface BookingRepository extends CrudRepository<Booking, Long> {

    List<Booking> findByCustomerId(long customerId);

    List<Booking> findByStatus(BookingStatus status);

    void updateStatus(long id, BookingStatus status);
}
