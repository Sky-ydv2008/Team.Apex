package com.apexinnovators.controller;

import com.apexinnovators.dto.EventDto;
import com.apexinnovators.dto.PageResponse;
import com.apexinnovators.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Events", description = "Public upcoming events listing")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    @Operation(summary = "Page of upcoming events (page/size), soonest first")
    public PageResponse<EventDto> page(@RequestParam(required = false) Integer page,
                                       @RequestParam(required = false) Integer size) {
        return eventService.pageUpcoming(page, size);
    }
}
