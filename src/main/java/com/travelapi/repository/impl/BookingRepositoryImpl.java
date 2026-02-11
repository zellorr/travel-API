package com.travelapi.repository.impl;

import com.travelapi.exception.DatabaseOperationException;
import com.travelapi.exception.ResourceNotFoundException;
import com.travelapi.model.*;
import com.travelapi.patterns.LoggingService;
import com.travelapi.repository.BookingRepository;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class BookingRepositoryImpl implements BookingRepository {

    private final DataSource dataSource;
    private final LoggingService loggingService;

    public BookingRepositoryImpl(DataSource dataSource, LoggingService loggingService) {
        this.dataSource = dataSource;
        this.loggingService = loggingService;
    }

    @Override
    public Booking create(Booking booking) {
        if (booking instanceof FlightBooking) {
            return createFlightBooking((FlightBooking) booking);
        } else if (booking instanceof HotelBooking) {
            return createHotelBooking((HotelBooking) booking);
        } else {
            throw new IllegalArgumentException("Unknown booking type");
        }
    }

    private FlightBooking createFlightBooking(FlightBooking booking) {
        String bookingSql = "INSERT INTO bookings (booking_date, total_price, status, customer_id, type) VALUES (?, ?, ?, ?, ?)";
        String flightSql = "INSERT INTO flight_bookings (booking_id, flight_number, origin, destination, seat_class) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement bookingStmt = conn.prepareStatement(bookingSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement flightStmt = conn.prepareStatement(flightSql)) {
                bookingStmt.setDate(1, Date.valueOf(booking.getBookingDate()));
                bookingStmt.setDouble(2, booking.getTotalPrice());
                bookingStmt.setString(3, booking.getStatus().name());
                bookingStmt.setLong(4, booking.getCustomerId());
                bookingStmt.setString(5, "FLIGHT");
                bookingStmt.executeUpdate();
                try (ResultSet keys = bookingStmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        booking.setId(keys.getLong(1));
                    } else {
                        throw new DatabaseOperationException("Creating booking failed, no ID obtained");
                    }
                }
                flightStmt.setLong(1, booking.getId());
                flightStmt.setString(2, booking.getFlightNumber());
                flightStmt.setString(3, booking.getOrigin());
                flightStmt.setString(4, booking.getDestination());
                flightStmt.setString(5, booking.getSeatClass().name());
                flightStmt.executeUpdate();
                conn.commit();
                loggingService.logDatabaseOperation("CREATE", "FlightBooking", true);
                return booking;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            loggingService.logDatabaseOperation("CREATE", "FlightBooking", false);
            throw new DatabaseOperationException("create", "FlightBooking", e);
        }
    }

    private HotelBooking createHotelBooking(HotelBooking booking) {
        String bookingSql = "INSERT INTO bookings (booking_date, total_price, status, customer_id, type) VALUES (?, ?, ?, ?, ?)";
        String hotelSql = "INSERT INTO hotel_bookings (booking_id, hotel_name, room_type, nights) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement bookingStmt = conn.prepareStatement(bookingSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement hotelStmt = conn.prepareStatement(hotelSql)) {
                bookingStmt.setDate(1, Date.valueOf(booking.getBookingDate()));
                bookingStmt.setDouble(2, booking.getTotalPrice());
                bookingStmt.setString(3, booking.getStatus().name());
                bookingStmt.setLong(4, booking.getCustomerId());
                bookingStmt.setString(5, "HOTEL");
                bookingStmt.executeUpdate();
                try (ResultSet keys = bookingStmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        booking.setId(keys.getLong(1));
                    } else {
                        throw new DatabaseOperationException("Creating booking failed, no ID obtained");
                    }
                }
                hotelStmt.setLong(1, booking.getId());
                hotelStmt.setString(2, booking.getHotelName());
                hotelStmt.setString(3, booking.getRoomType().name());
                hotelStmt.setInt(4, booking.getNights());
                hotelStmt.executeUpdate();
                conn.commit();
                loggingService.logDatabaseOperation("CREATE", "HotelBooking", true);
                return booking;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            loggingService.logDatabaseOperation("CREATE", "HotelBooking", false);
            throw new DatabaseOperationException("create", "HotelBooking", e);
        }
    }

    @Override
    public List<Booking> findAll() {
        List<Booking> result = new ArrayList<>();
        result.addAll(findAllFlightBookings());
        result.addAll(findAllHotelBookings());
        loggingService.logDatabaseOperation("FIND_ALL", "Booking", true);
        return result;
    }

    private List<FlightBooking> findAllFlightBookings() {
        String sql = "SELECT b.*, fb.flight_number, fb.origin, fb.destination, fb.seat_class FROM bookings b JOIN flight_bookings fb ON b.id = fb.booking_id";
        List<FlightBooking> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapFlight(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("findAll", "FlightBooking", e);
        }
        return list;
    }

    private List<HotelBooking> findAllHotelBookings() {
        String sql = "SELECT b.*, hb.hotel_name, hb.room_type, hb.nights FROM bookings b JOIN hotel_bookings hb ON b.id = hb.booking_id";
        List<HotelBooking> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapHotel(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("findAll", "HotelBooking", e);
        }
        return list;
    }

    @Override
    public Optional<Booking> findById(Long id) {
        try {
            return Optional.of(findFlightById(id));
        } catch (ResourceNotFoundException e) {
            try {
                return Optional.of(findHotelById(id));
            } catch (ResourceNotFoundException e2) {
                return Optional.empty();
            }
        }
    }

    private FlightBooking findFlightById(long id) {
        String sql = "SELECT b.*, fb.flight_number, fb.origin, fb.destination, fb.seat_class FROM bookings b JOIN flight_bookings fb ON b.id = fb.booking_id WHERE b.id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ResourceNotFoundException("FlightBooking", id);
                }
                return mapFlight(rs);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("findById", "FlightBooking", e);
        }
    }

    private HotelBooking findHotelById(long id) {
        String sql = "SELECT b.*, hb.hotel_name, hb.room_type, hb.nights FROM bookings b JOIN hotel_bookings hb ON b.id = hb.booking_id WHERE b.id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ResourceNotFoundException("HotelBooking", id);
                }
                return mapHotel(rs);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("findById", "HotelBooking", e);
        }
    }

    @Override
    public Booking update(Long id, Booking booking) {
        throw new UnsupportedOperationException("Use updateStatus for booking updates");
    }

    @Override
    public void delete(Long id) {
        findById(id).orElseThrow(() -> new ResourceNotFoundException("Booking", id));
        String sql = "DELETE FROM bookings WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
            loggingService.logDatabaseOperation("DELETE", "Booking", true);
        } catch (SQLException e) {
            loggingService.logDatabaseOperation("DELETE", "Booking", false);
            throw new DatabaseOperationException("delete", "Booking", e);
        }
    }

    @Override
    public List<Booking> findByCustomerId(long customerId) {
        List<Booking> result = new ArrayList<>();
        result.addAll(findFlightBookingsByCustomerId(customerId));
        result.addAll(findHotelBookingsByCustomerId(customerId));
        return result;
    }

    private List<FlightBooking> findFlightBookingsByCustomerId(long customerId) {
        String sql = "SELECT b.*, fb.flight_number, fb.origin, fb.destination, fb.seat_class FROM bookings b JOIN flight_bookings fb ON b.id = fb.booking_id WHERE b.customer_id = ?";
        List<FlightBooking> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapFlight(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("findByCustomerId", "FlightBooking", e);
        }
        return list;
    }

    private List<HotelBooking> findHotelBookingsByCustomerId(long customerId) {
        String sql = "SELECT b.*, hb.hotel_name, hb.room_type, hb.nights FROM bookings b JOIN hotel_bookings hb ON b.id = hb.booking_id WHERE b.customer_id = ?";
        List<HotelBooking> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapHotel(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("findByCustomerId", "HotelBooking", e);
        }
        return list;
    }

    @Override
    public List<Booking> findByStatus(BookingStatus status) {
        List<Booking> result = new ArrayList<>();
        String sql = "SELECT b.*, fb.flight_number, fb.origin, fb.destination, fb.seat_class FROM bookings b JOIN flight_bookings fb ON b.id = fb.booking_id WHERE b.status = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapFlight(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("findByStatus", "FlightBooking", e);
        }
        sql = "SELECT b.*, hb.hotel_name, hb.room_type, hb.nights FROM bookings b JOIN hotel_bookings hb ON b.id = hb.booking_id WHERE b.status = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapHotel(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("findByStatus", "HotelBooking", e);
        }
        return result;
    }

    @Override
    public void updateStatus(long id, BookingStatus status) {
        findById(id).orElseThrow(() -> new ResourceNotFoundException("Booking", id));
        String sql = "UPDATE bookings SET status = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, id);
            ps.executeUpdate();
            loggingService.logDatabaseOperation("UPDATE_STATUS", "Booking", true);
        } catch (SQLException e) {
            loggingService.logDatabaseOperation("UPDATE_STATUS", "Booking", false);
            throw new DatabaseOperationException("updateStatus", "Booking", e);
        }
    }

    private FlightBooking mapFlight(ResultSet rs) throws SQLException {
        return new FlightBooking(
                rs.getLong("id"),
                rs.getDate("booking_date").toLocalDate(),
                rs.getDouble("total_price"),
                BookingStatus.valueOf(rs.getString("status")),
                rs.getLong("customer_id"),
                rs.getString("flight_number"),
                rs.getString("origin"),
                rs.getString("destination"),
                SeatClass.valueOf(rs.getString("seat_class"))
        );
    }

    private HotelBooking mapHotel(ResultSet rs) throws SQLException {
        return new HotelBooking(
                rs.getLong("id"),
                rs.getDate("booking_date").toLocalDate(),
                rs.getDouble("total_price"),
                BookingStatus.valueOf(rs.getString("status")),
                rs.getLong("customer_id"),
                rs.getString("hotel_name"),
                RoomType.valueOf(rs.getString("room_type")),
                rs.getInt("nights")
        );
    }
}
