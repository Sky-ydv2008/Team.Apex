package com.apexinnovators.controller;

import com.apexinnovators.dto.AchievementDto;
import com.apexinnovators.dto.PageResponse;
import com.apexinnovators.service.AchievementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/achievements")
@Tag(name = "Achievements", description = "Public achievements listing with member names")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    @GetMapping
    @Operation(summary = "Page of achievements (page/size), newest first")
    public PageResponse<AchievementDto> page(@RequestParam(required = false) Integer page,
                                             @RequestParam(required = false) Integer size) {
        return achievementService.page(page, size);
    }
}
