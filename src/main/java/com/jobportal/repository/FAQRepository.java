package com.jobportal.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.jobportal.entity.FAQ;

public interface FAQRepository extends MongoRepository<FAQ, String> {
}
