package com.airlinebookings.document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BookingCount {

    private String travelerId;
    private Integer count;
}
