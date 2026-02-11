package com.travelapi.repository;

import com.travelapi.model.Customer;

import java.util.Optional;

public interface CustomerRepository extends CrudRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailExcludingId(String email, long excludeId);
}
