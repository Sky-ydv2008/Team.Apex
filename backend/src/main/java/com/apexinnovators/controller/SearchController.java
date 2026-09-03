package com.apexinnovators.controller;

import com.apexinnovators.dto.SearchResponse;
import com.apexinnovators.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@Tag(name = "Search", description = "Cross-entity search over published content")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    @Operation(summary = "Search projects, hackathons and posts by q (published content only, up to 10 per bucket)")
    public SearchResponse search(@RequestParam String q) {
        return searchService.search(q);
    }
}
