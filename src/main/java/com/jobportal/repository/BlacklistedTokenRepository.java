package com.jobportal.repository;

import com.jobportal.entity.BlacklistedToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Date;

public interface BlacklistedTokenRepository extends MongoRepository<BlacklistedToken, String> {
    void deleteByExpiryDateBefore(Date now);
}
