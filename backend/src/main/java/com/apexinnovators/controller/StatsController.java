package com.apexinnovators.controller;

import com.apexinnovators.dto.StatsDto;
import com.apexinnovators.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/stats")
@Tag(name = "Public Stats", description = "Aggregate counters for the landing page")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping
    @Operation(summary = "Published project count, hackathons, active members, technologies and achievements")
    public StatsDto stats() {
        return statsService.stats();
    }
}
