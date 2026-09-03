package com.apexinnovators.controller;

import com.apexinnovators.dto.MemberDto;
import com.apexinnovators.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/team")
@Tag(name = "Team", description = "Public team page (ADMIN + CORE_MEMBER roles with profiles)")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    @Operation(summary = "List published team members with their profiles")
    public List<MemberDto> list() {
        return teamService.listPublishedMembers();
    }
}
