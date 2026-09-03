package com.apexinnovators.repository;

import com.apexinnovators.entity.Event;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {

    /** Upcoming events (date >= now), soonest first. Events without a date are excluded. */
    Page<Event> findByDateGreaterThanEqualOrderByDateAsc(LocalDateTime date, Pageable pageable);
}
