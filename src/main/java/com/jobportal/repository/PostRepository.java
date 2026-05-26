package com.jobportal.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.jobportal.entity.Post;

public interface PostRepository extends MongoRepository<Post, Long> {
}
