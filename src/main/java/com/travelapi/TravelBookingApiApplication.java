package com.travelapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TravelBookingApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TravelBookingApiApplication.class, args);
        System.out.println("\n=================================================");
        System.out.println("   Travel Booking API Started Successfully!");
        System.out.println("   Access: http://localhost:8081/index.html");
        System.out.println("=================================================\n");
    }
}