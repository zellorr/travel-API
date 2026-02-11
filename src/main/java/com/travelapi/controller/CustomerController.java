package com.travelapi.controller;

import com.travelapi.dto.CustomerRequest;
import com.travelapi.model.Customer;
import com.travelapi.patterns.LoggingService;
import com.travelapi.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final LoggingService loggingService;

    public CustomerController(CustomerService customerService, LoggingService loggingService) {
        this.customerService = customerService;
        this.loggingService = loggingService;
    }

    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers(HttpServletRequest request) {
        loggingService.logApiRequest("/api/customers", "GET", request.getRemoteAddr());
        List<Customer> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable long id, HttpServletRequest request) {
        loggingService.logApiRequest("/api/customers/" + id, "GET", request.getRemoteAddr());
        Customer customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(customer);
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody CustomerRequest customerRequest,
                                                   HttpServletRequest request) {
        loggingService.logApiRequest("/api/customers", "POST", request.getRemoteAddr());
        Customer customer = new Customer(
                0L,
                customerRequest.getName(),
                customerRequest.getEmail(),
                customerRequest.getPhone(),
                customerRequest.getPassportNumber()
        );
        Customer created = customerService.createCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable long id,
                                                   @RequestBody CustomerRequest customerRequest,
                                                   HttpServletRequest request) {
        loggingService.logApiRequest("/api/customers/" + id, "PUT", request.getRemoteAddr());
        Customer customer = new Customer(
                0L,
                customerRequest.getName(),
                customerRequest.getEmail(),
                customerRequest.getPhone(),
                customerRequest.getPassportNumber()
        );
        Customer updated = customerService.updateCustomer(id, customer);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable long id, HttpServletRequest request) {
        loggingService.logApiRequest("/api/customers/" + id, "DELETE", request.getRemoteAddr());
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Customer> getCustomerByEmail(@PathVariable String email,
                                                       HttpServletRequest request) {
        loggingService.logApiRequest("/api/customers/email/" + email, "GET", request.getRemoteAddr());
        Customer customer = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(customer);
    }
}
