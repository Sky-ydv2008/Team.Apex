package com.apexinnovators.controller;

import com.apexinnovators.dto.TechDto;
import com.apexinnovators.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public technology catalog: drives filter vocabularies and the home-page
 * "technology ecosystem" section. Read-only; management lives under /api/admin.
 */
@RestController
@RequestMapping("/api/technologies")
@Tag(name = "Technologies", description = "Public read-only technology catalog")
@RequiredArgsConstructor
public class PublicTechnologyController {

    private final AdminService adminService;

    @GetMapping
    @Operation(summary = "List all technologies ordered by name")
    public List<TechDto> list() {
        return adminService.listTechnologies();
    }
}
