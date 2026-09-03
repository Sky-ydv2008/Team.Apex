package com.apexinnovators.repository;

import com.apexinnovators.entity.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    void deleteByUserId(Long userId);
}
