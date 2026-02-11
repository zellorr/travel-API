package com.travelapi.model;

public class Customer implements Validatable {

    private long id;
    private String name;
    private String email;
    private String phone;
    private String passportNumber;

    public Customer() {
    }

    public Customer(long id, String name, String email, String phone, String passportNumber) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.passportNumber = passportNumber;
    }

    @Override
    public void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name cannot be empty");
        }
        if (name.length() < 2) {
            throw new IllegalArgumentException("Customer name must be at least 2 characters");
        }
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }
        if (passportNumber == null || passportNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Passport number cannot be empty");
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        if (id <= 0L) {
            throw new IllegalArgumentException("ID must be greater than 0");
        }
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name cannot be empty");
        }
        if (name.length() < 2) {
            throw new IllegalArgumentException("Customer name must be at least 2 characters");
        }
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }
        this.phone = phone;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        if (passportNumber == null || passportNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Passport number cannot be empty");
        }
        this.passportNumber = passportNumber;
    }

    @Override
    public String toString() {
        return String.format("Customer[id=%d, name=%s, email=%s, phone=%s, passport=%s]", id, name, email, phone, passportNumber);
    }
}
