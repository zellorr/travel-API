package com.travelapi.repository.impl;

import com.travelapi.exception.DatabaseOperationException;
import com.travelapi.exception.DuplicateResourceException;
import com.travelapi.exception.ResourceNotFoundException;
import com.travelapi.model.Customer;
import com.travelapi.patterns.LoggingService;
import com.travelapi.repository.CustomerRepository;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class CustomerRepositoryImpl implements CustomerRepository {

    private final DataSource dataSource;
    private final LoggingService loggingService;

    public CustomerRepositoryImpl(DataSource dataSource, LoggingService loggingService) {
        this.dataSource = dataSource;
        this.loggingService = loggingService;
    }

    @Override
    public Customer create(Customer customer) {
        if (existsByEmail(customer.getEmail())) {
            throw new DuplicateResourceException("Customer", "email: " + customer.getEmail());
        }
        String sql = "INSERT INTO customers (name, email, phone, passport_number) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPhone());
            ps.setString(4, customer.getPassportNumber());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    customer.setId(keys.getLong(1));
                    loggingService.logDatabaseOperation("CREATE", "Customer", true);
                    return customer;
                } else {
                    throw new DatabaseOperationException("Creating customer failed, no ID obtained");
                }
            }
        } catch (SQLException e) {
            loggingService.logDatabaseOperation("CREATE", "Customer", false);
            throw new DatabaseOperationException("create", "Customer", e);
        }
    }

    @Override
    public List<Customer> findAll() {
        String sql = "SELECT * FROM customers ORDER BY id";
        List<Customer> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapCustomer(rs));
            }
            loggingService.logDatabaseOperation("FIND_ALL", "Customer", true);
            return list;
        } catch (SQLException e) {
            loggingService.logDatabaseOperation("FIND_ALL", "Customer", false);
            throw new DatabaseOperationException("findAll", "Customer", e);
        }
    }

    @Override
    public Optional<Customer> findById(Long id) {
        String sql = "SELECT * FROM customers WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    loggingService.logDatabaseOperation("FIND_BY_ID", "Customer", true);
                    return Optional.of(mapCustomer(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            loggingService.logDatabaseOperation("FIND_BY_ID", "Customer", false);
            throw new DatabaseOperationException("findById", "Customer", e);
        }
    }

    @Override
    public Customer update(Long id, Customer customer) {
        findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        if (existsByEmailExcludingId(customer.getEmail(), id)) {
            throw new DuplicateResourceException("Customer", "email: " + customer.getEmail());
        }
        String sql = "UPDATE customers SET name = ?, email = ?, phone = ?, passport_number = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPhone());
            ps.setString(4, customer.getPassportNumber());
            ps.setLong(5, id);
            ps.executeUpdate();
            customer.setId(id);
            loggingService.logDatabaseOperation("UPDATE", "Customer", true);
            return customer;
        } catch (SQLException e) {
            loggingService.logDatabaseOperation("UPDATE", "Customer", false);
            throw new DatabaseOperationException("update", "Customer", e);
        }
    }

    @Override
    public void delete(Long id) {
        findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer", id));
        String sql = "DELETE FROM customers WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
            loggingService.logDatabaseOperation("DELETE", "Customer", true);
        } catch (SQLException e) {
            loggingService.logDatabaseOperation("DELETE", "Customer", false);
            throw new DatabaseOperationException("delete", "Customer", e);
        }
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        String sql = "SELECT * FROM customers WHERE email = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapCustomer(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("findByEmail", "Customer", e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM customers WHERE email = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("existsByEmail", "Customer", e);
        }
        return false;
    }

    @Override
    public boolean existsByEmailExcludingId(String email, long excludeId) {
        String sql = "SELECT COUNT(*) FROM customers WHERE email = ? AND id != ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setLong(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("existsByEmailExcludingId", "Customer", e);
        }
        return false;
    }

    private Customer mapCustomer(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("passport_number")
        );
    }
}
