package com.apexinnovators.repository;

import com.apexinnovators.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    void deleteByUserId(Long userId);
}
