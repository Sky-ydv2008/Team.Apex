package com.apexinnovators.service;

import com.apexinnovators.dto.EventDto;
import com.apexinnovators.dto.PageResponse;
import com.apexinnovators.entity.Event;
import com.apexinnovators.repository.EventRepository;
import com.apexinnovators.util.PageUtil;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    /** Public page of upcoming events (date >= now), soonest first. */
    @Transactional(readOnly = true)
    public PageResponse<EventDto> pageUpcoming(Integer page, Integer size) {
        Pageable pageable = PageUtil.of(page, size);
        Page<Event> events = eventRepository.findByDateGreaterThanEqualOrderByDateAsc(
                LocalDateTime.now(), pageable);
        List<EventDto> content = events.getContent().stream()
                .map(e -> new EventDto(e.getId(), e.getTitle(), e.getDate(), e.getMode(),
                        e.getLocation(), e.getDescription()))
                .toList();
        return new PageResponse<>(content, events.getNumber(), events.getSize(),
                events.getTotalElements(), events.getTotalPages());
    }
}
