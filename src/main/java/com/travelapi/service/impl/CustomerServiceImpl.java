package com.travelapi.service.impl;

import com.travelapi.exception.ResourceNotFoundException;
import com.travelapi.model.Customer;
import com.travelapi.patterns.LoggingService;
import com.travelapi.repository.CustomerRepository;
import com.travelapi.service.CustomerService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final LoggingService loggingService;

    public CustomerServiceImpl(CustomerRepository customerRepository, LoggingService loggingService) {
        this.customerRepository = customerRepository;
        this.loggingService = loggingService;
    }

    @Override
    public Customer createCustomer(Customer customer) {
        loggingService.info("CustomerService", "createCustomer", "Creating customer: " + customer.getEmail());
        customer.validate();
        Customer created = customerRepository.create(customer);
        loggingService.info("Customer created successfully with ID: " + created.getId());
        return created;
    }

    @Override
    public List<Customer> getAllCustomers() {
        loggingService.info("CustomerService", "getAllCustomers", "Fetching all customers");
        return customerRepository.findAll();
    }

    @Override
    public Customer getCustomerById(long id) {
        loggingService.info("CustomerService", "getCustomerById", "Fetching customer with ID: " + id);
        return customerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    @Override
    public Customer updateCustomer(long id, Customer customer) {
        loggingService.info("CustomerService", "updateCustomer", "Updating customer ID: " + id);
        customer.validate();
        Customer updated = customerRepository.update(id, customer);
        loggingService.info("Customer updated successfully: " + id);
        return updated;
    }

    @Override
    public void deleteCustomer(long id) {
        loggingService.info("CustomerService", "deleteCustomer", "Deleting customer ID: " + id);
        customerRepository.delete(id);
        loggingService.info("Customer deleted successfully: " + id);
    }

    @Override
    public Customer getCustomerByEmail(String email) {
        loggingService.info("CustomerService", "getCustomerByEmail", "Fetching customer with email: " + email);
        return customerRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Customer with email " + email + " not found"));
    }
}
