package com.restaurantapp.service;

import com.restaurantapp.dto.Location;
import com.restaurantapp.dto.Tables;
import com.restaurantapp.exception.LocationNotFoundException;
import com.restaurantapp.exception.TableNotAvailableException;
import com.restaurantapp.repo.LocationRepo;
import com.restaurantapp.repo.TableRepo;
import com.restaurantapp.util.DateFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TableService {

    private TableRepo tableRepo;
    private LocationRepo locationRepo;

    @Autowired
    public TableService(TableRepo tableRepo, LocationRepo locationRepo) {
        this.tableRepo = tableRepo;
        this.locationRepo = locationRepo;
    }

    public Map<String, Object> getAvailableTables(Map<String, String> queryParams) throws Exception {
        if (queryParams == null || queryParams.get("locationId") == null ||
                queryParams.get("date") == null) {
            throw new IllegalArgumentException("Missing required query parameters: locationId, date.");
        }

        String locationId = queryParams.get("locationId");
        Location location = locationRepo.getLocationById(locationId);
        if (location == null) throw new LocationNotFoundException("Location not found");
        String date = DateFormatter.convertToStandardFormat(queryParams.get("date"));
        String time = queryParams.getOrDefault("time", null);
        String guests = queryParams.getOrDefault("guests", null);
        String zone = locationRepo.getLocationById(locationId).getZone();

        LocalDate currentDate = LocalDate.now(ZoneId.of(zone));
        LocalDate parsedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        if (parsedDate.isBefore(currentDate)) throw new IllegalArgumentException("Invalid date provided.");
        else if (guests != null && Integer.parseInt(guests) <= 0)
            throw new IllegalArgumentException("Invalid no. of guests provided.");

        if (time != null) {
            LocalDateTime currentDateTime = LocalDateTime.now(ZoneId.of(zone));
            LocalDateTime parsedDateTime = LocalDateTime.of(parsedDate, LocalTime.parse(time));
            if (parsedDateTime.isBefore(currentDateTime)) throw new IllegalArgumentException("Invalid time provided.");
        }

        List<Tables> availableTables = tableRepo.getAvailableTables(locationId, date, time, guests, zone);
        if (availableTables.isEmpty()) throw new TableNotAvailableException("Table not available.", "GET");

        List<Map<String, Object>> transformedTables = availableTables.stream()
                .map(table -> {
                    List<String> availableSlots = table.getAvailableSlots().entrySet().stream()
                            .filter(Map.Entry::getValue)
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toList());

                    return Map.of(
                            "tableNumber", table.getTableNumber().split("#")[0],
                            "locationId", table.getLocationId(),
                            "capacity", table.getCapacity(),
                            "date", table.getDate(),
                            "availableSlots", availableSlots
                    );
                })
                .collect(Collectors.toList());

        return Map.of("tables", transformedTables);
    }
}
