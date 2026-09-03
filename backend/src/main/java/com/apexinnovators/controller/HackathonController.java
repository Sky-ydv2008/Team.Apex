package com.apexinnovators.controller;

import com.apexinnovators.dto.HackathonDto;
import com.apexinnovators.dto.PageResponse;
import com.apexinnovators.service.HackathonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hackathons")
@Tag(name = "Hackathons", description = "Public hackathon archive listing and detail")
@RequiredArgsConstructor
public class HackathonController {

    private final HackathonService hackathonService;

    @GetMapping
    @Operation(summary = "List hackathons (page/size), newest first")
    public PageResponse<HackathonDto> list(@RequestParam(required = false) Integer page,
                                           @RequestParam(required = false) Integer size) {
        return hackathonService.list(page, size);
    }

    @GetMapping("/{identifier}")
    @Operation(summary = "Get a hackathon by numeric id or slug")
    public HackathonDto get(@PathVariable String identifier) {
        return hackathonService.getByIdOrSlug(identifier);
    }
}
