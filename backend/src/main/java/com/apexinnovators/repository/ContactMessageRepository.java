package com.apexinnovators.repository;

import com.apexinnovators.entity.ContactMessage;
import com.apexinnovators.entity.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    Page<ContactMessage> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<ContactMessage> findByStatusOrderByCreatedAtDesc(MessageStatus status, Pageable pageable);

    long countByStatus(MessageStatus status);
}
