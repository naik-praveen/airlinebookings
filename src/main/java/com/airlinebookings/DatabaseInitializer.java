package com.airlinebookings;

import com.airlinebookings.document.Airline;
import com.airlinebookings.document.Booking;
import com.airlinebookings.document.Traveler;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DatabaseInitializer {

    @Autowired
    MongoTemplate mongoTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        mongoTemplate.dropCollection(Airline.class);
        mongoTemplate.createCollection(Airline.class);

        mongoTemplate.dropCollection(Booking.class);
        mongoTemplate.createCollection(Booking.class);

        mongoTemplate.dropCollection(Traveler.class);
        mongoTemplate.createCollection(Traveler.class);

        List<Traveler> travelers = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Traveler traveler = new Traveler();
            traveler.setName(RandomStringUtils.randomAlphabetic(5) + " " + RandomStringUtils.randomAlphabetic(10));
            travelers.add(mongoTemplate.insert(traveler));
        }

        List<Airline> airlines = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Airline airline = new Airline();
            airline.setName(RandomStringUtils.randomAlphabetic(5) + " Airlines");
            airlines.add(mongoTemplate.insert(airline));
        }

        List<Booking> bookings = new ArrayList<>();
        for (int i = 0; i < 1000000; i++) {
            Booking booking = new Booking();
            booking.setBookingInstant(Instant.ofEpochSecond(Instant.now().getEpochSecond() - ThreadLocalRandom.current().nextLong(315360000)));
            booking.setAirlineId(airlines.get(ThreadLocalRandom.current().nextInt(5)).getId());
            booking.setTravelerId(travelers.get(ThreadLocalRandom.current().nextInt(100)).getId());
            bookings.add(mongoTemplate.insert(booking));
        }

    }
}
