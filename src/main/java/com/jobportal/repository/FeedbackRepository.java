package com.jobportal.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.jobportal.entity.Feedback;

public interface FeedbackRepository extends MongoRepository<Feedback, String> {
}
