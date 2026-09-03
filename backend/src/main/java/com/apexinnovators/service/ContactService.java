package com.apexinnovators.service;

import com.apexinnovators.dto.ContactDto;
import com.apexinnovators.dto.ContactRequest;
import com.apexinnovators.entity.ContactMessage;
import com.apexinnovators.entity.MessageStatus;
import com.apexinnovators.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactMessageRepository contactMessageRepository;

    /** Public contact form submission — stored as NEW for the admin inbox. */
    @Transactional
    public ContactDto submit(ContactRequest request) {
        ContactMessage message = new ContactMessage();
        message.setName(request.name().trim());
        message.setEmail(request.email().trim());
        message.setSubject(request.subject() == null ? null : request.subject().trim());
        message.setMessage(request.message());
        message.setStatus(MessageStatus.NEW);
        contactMessageRepository.save(message);
        return new ContactDto(message.getName(), message.getEmail(), message.getSubject(),
                message.getMessage());
    }
}
