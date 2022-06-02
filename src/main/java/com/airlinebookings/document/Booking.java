package com.airlinebookings.document;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document
@Data
@NoArgsConstructor
public class Booking {
    private String travelerId;
    private String airlineId;
    private Instant bookingInstant;
}
