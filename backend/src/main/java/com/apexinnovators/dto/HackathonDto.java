package com.apexinnovators.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Hackathon payload (contract field names):
 * {id,name,slug,organizer,date,description,challenge,result,certificateUrl,
 *  presentationUrl,members,projects,createdAt}.
 */
public record HackathonDto(
        Long id,
        String name,
        String slug,
        String organizer,
        LocalDate date,
        String description,
        String challenge,
        String result,
        String certificateUrl,
        String presentationUrl,
        List<HackathonMemberDto> members,
        List<HackathonProjectLinkDto> projects,
        LocalDateTime createdAt) {
}
