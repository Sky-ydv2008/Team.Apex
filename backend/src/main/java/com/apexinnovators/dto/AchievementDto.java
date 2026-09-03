package com.apexinnovators.dto;

import com.apexinnovators.entity.AchievementType;
import java.time.LocalDate;

/**
 * Achievement payload (contract field names):
 * {id,userId,userName,title,type,issuer,awardDate,description,verifyUrl}.
 */
public record AchievementDto(
        Long id,
        Long userId,
        String userName,
        String title,
        AchievementType type,
        String issuer,
        LocalDate awardDate,
        String description,
        String verifyUrl) {
}
