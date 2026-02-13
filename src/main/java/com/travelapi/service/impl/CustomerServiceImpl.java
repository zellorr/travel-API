package com.travelapi.service.impl;

import com.travelapi.exception.DuplicateResourceException;
import com.travelapi.exception.ResourceNotFoundException;
import com.travelapi.model.Customer;
import com.travelapi.patterns.CacheManager;
import com.travelapi.patterns.LoggingService;
import com.travelapi.repository.CustomerRepository;
import com.travelapi.service.CustomerService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CacheManager cacheManager;
    private final LoggingService loggingService;

    private static final String CACHE_ALL_CUSTOMERS = "customers:all";
    private static final String CACHE_CUSTOMER_PREFIX = "customer:";
    private static final String CACHE_CUSTOMER_EMAIL_PREFIX = "customer:email:";

    public CustomerServiceImpl(CustomerRepository customerRepository,
                               CacheManager cacheManager,
                               LoggingService loggingService) {
        this.customerRepository = customerRepository;
        this.cacheManager = cacheManager;
        this.loggingService = loggingService;
    }

    @Override
    public Customer createCustomer(Customer customer) {
        customer.validate();

        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new DuplicateResourceException("Customer", "email: " + customer.getEmail());
        }

        Customer created = customerRepository.create(customer);

        invalidateAllCustomersCache();

        loggingService.logDatabaseOperation("CREATE", "Customer", true);
        loggingService.info("Customer created with ID: " + created.getId() + ", cache invalidated");

        return created;
    }

    @Override
    public List<Customer> getAllCustomers() {
        List<Customer> cachedCustomers = cacheManager.get(CACHE_ALL_CUSTOMERS);

        if (cachedCustomers != null) {
            loggingService.info("getAllCustomers() - Cache HIT");
            return cachedCustomers;
        }

        loggingService.info("getAllCustomers() - Cache MISS, querying database");
        List<Customer> customers = customerRepository.findAll();

        cacheManager.put(CACHE_ALL_CUSTOMERS, customers);
        loggingService.info("getAllCustomers() - Cached " + customers.size() + " customers");

        return customers;
    }

    @Override
    public Customer getCustomerById(long id) {
        String cacheKey = CACHE_CUSTOMER_PREFIX + id;

        Customer cachedCustomer = cacheManager.get(cacheKey);

        if (cachedCustomer != null) {
            loggingService.info("getCustomerById(" + id + ") - Cache HIT");
            return cachedCustomer;
        }

        loggingService.info("getCustomerById(" + id + ") - Cache MISS");
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));

        cacheManager.put(cacheKey, customer);

        return customer;
    }

    @Override
    public Customer updateCustomer(long id, Customer customer) {
        Customer existing = getCustomerById(id);

        customer.validate();

        if (!existing.getEmail().equals(customer.getEmail()) &&
                customerRepository.existsByEmailExcludingId(customer.getEmail(), id)) {
            throw new DuplicateResourceException("Customer", "email: " + customer.getEmail());
        }

        Customer updated = customerRepository.update(id, customer);

        invalidateCustomerCache(id);
        invalidateCustomerEmailCache(existing.getEmail());
        invalidateAllCustomersCache();

        loggingService.logDatabaseOperation("UPDATE", "Customer", true);
        loggingService.info("Customer " + id + " updated, cache invalidated");

        return updated;
    }

    @Override
    public void deleteCustomer(long id) {
        Customer customer = getCustomerById(id);

        customerRepository.delete(id);

        invalidateCustomerCache(id);
        invalidateCustomerEmailCache(customer.getEmail());
        invalidateAllCustomersCache();

        loggingService.logDatabaseOperation("DELETE", "Customer", true);
        loggingService.info("Customer " + id + " deleted, cache invalidated");
    }

    @Override
    public Customer getCustomerByEmail(String email) {
        String cacheKey = CACHE_CUSTOMER_EMAIL_PREFIX + email;

        Customer cachedCustomer = cacheManager.get(cacheKey);

        if (cachedCustomer != null) {
            loggingService.info("getCustomerByEmail(" + email + ") - Cache HIT");
            return cachedCustomer;
        }

        loggingService.info("getCustomerByEmail(" + email + ") - Cache MISS");
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer with email " + email + " not found"));

        cacheManager.put(cacheKey, customer);

        return customer;
    }

    private void invalidateCustomerCache(long id) {
        String cacheKey = CACHE_CUSTOMER_PREFIX + id;
        cacheManager.evict(cacheKey);
        loggingService.debug("Invalidated cache for customer: " + id);
    }

    private void invalidateCustomerEmailCache(String email) {
        String cacheKey = CACHE_CUSTOMER_EMAIL_PREFIX + email;
        cacheManager.evict(cacheKey);
        loggingService.debug("Invalidated cache for customer email: " + email);
    }

    private void invalidateAllCustomersCache() {
        cacheManager.evict(CACHE_ALL_CUSTOMERS);
        cacheManager.evictByPattern(CACHE_CUSTOMER_PREFIX + "*");
        cacheManager.evictByPattern(CACHE_CUSTOMER_EMAIL_PREFIX + "*");
        loggingService.debug("Invalidated all customers cache");
    }
}
