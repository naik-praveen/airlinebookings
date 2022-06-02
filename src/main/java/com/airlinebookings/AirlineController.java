package com.airlinebookings;

import com.airlinebookings.document.Airline;
import com.airlinebookings.document.Booking;
import com.airlinebookings.document.BookingCount;
import com.airlinebookings.document.Traveler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@RestController
@RequestMapping("/api")
public class AirlineController {

    @Autowired
    MongoTemplate mongoTemplate;

    @GetMapping("/airlines")
    private List<Airline> getAirlines() {
        return mongoTemplate.findAll(Airline.class);
    }

    @GetMapping("/travelers")
    private List<Traveler> getTravelers() {
        return mongoTemplate.findAll(Traveler.class);
    }

    @GetMapping("/bookings")
    private List<Booking> getBookings() {
        return mongoTemplate.findAll(Booking.class);
    }

    @GetMapping("/top-travelers")
    private List<BookingCount> getTopTravelers(@RequestParam(required = false) String airline,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
                                               @RequestParam(required = false) Integer top) {
        List<AggregationOperation> aggregationOperations = new ArrayList<>();
        List<Criteria> criteriaList = new ArrayList<>();

        if (StringUtils.isNotEmpty(airline)) {
            criteriaList.add(Criteria.where("airlineId").is(airline));
        }

        if (startDate != null) {
            criteriaList.add(Criteria.where("bookingInstant").gte(startDate.toInstant(ZoneOffset.UTC)));
        }

        if (endDate != null) {
            criteriaList.add(Criteria.where("bookingInstant").lte(endDate.toInstant(ZoneOffset.UTC)));
        }

        if (!criteriaList.isEmpty()) {
            Criteria criteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[criteriaList.size()]));
            MatchOperation matchOperation = match(criteria);
            aggregationOperations.add(matchOperation);
        }

        GroupOperation groupOperation = group("travelerId").count().as("bookingCount");
        SortOperation sortByCount = sort(Sort.Direction.DESC, "bookingCount")
                .and(Sort.Direction.ASC, "_id");
        ProjectionOperation projectToMatchModel = project().andExpression("_id").as("travelerId")
                .andExpression("bookingCount").as("count");
        aggregationOperations.add(groupOperation);
        aggregationOperations.add(sortByCount);
        aggregationOperations.add(projectToMatchModel);

        if (top != null) {
            aggregationOperations.add(limit(top));
        }

        Aggregation aggregation = Aggregation.newAggregation(aggregationOperations);

        AggregationResults<BookingCount> result = mongoTemplate.aggregate(
                aggregation, "booking", BookingCount.class);
        List<BookingCount> bookingCounts = result.getMappedResults();

        return bookingCounts;
    }
}
