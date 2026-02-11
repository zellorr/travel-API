package com.travelapi.dto;

public class CustomerRequest {

    private String name;
    private String email;
    private String phone;
    private String passportNumber;

    public CustomerRequest() {
    }

    public CustomerRequest(String name, String email, String phone, String passportNumber) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.passportNumber = passportNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }
}
