package com.jobportal.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.jobportal.entity.ContactMessage;

public interface ContactMessageRepository extends MongoRepository<ContactMessage, String> {
}
