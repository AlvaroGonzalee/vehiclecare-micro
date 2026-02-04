package com.vehiclecare.vehiclecaremicro.infrastructure.rest.controller;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/years")
public class YearsController {

    private static final int START_YEAR = 1980;

    @GetMapping
    public ResponseEntity<List<Integer>> listYears() {
        int currentYear = Year.now().getValue();
        List<Integer> years = new ArrayList<>();
        for (int year = currentYear; year >= START_YEAR; year--) {
            years.add(year);
        }
        return ResponseEntity.ok(years);
    }
}
