package com.travelapi.service;

import com.travelapi.model.Customer;

import java.util.List;

public interface CustomerService {

    Customer createCustomer(Customer customer);

    List<Customer> getAllCustomers();

    Customer getCustomerById(long id);

    Customer updateCustomer(long id, Customer customer);

    void deleteCustomer(long id);

    Customer getCustomerByEmail(String email);
}
