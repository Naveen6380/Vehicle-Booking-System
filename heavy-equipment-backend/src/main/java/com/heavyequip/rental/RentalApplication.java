package com.heavyequip.rental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Heavy Equipment Rental & Booking System backend.
 *
 * Run with: mvn spring-boot:run
 * Default active profile: dev (see application.yml -> spring.profiles.active)
 */
@SpringBootApplication
public class RentalApplication {

    public static void main(String[] args) {
        SpringApplication.run(RentalApplication.class, args);
    }
}
