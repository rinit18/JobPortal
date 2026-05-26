package com.jobportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.jobportal.entity.ConnectionRequest;
import com.jobportal.dto.ConnectionRequestStatus;

public interface ConnectionRequestRepository extends MongoRepository<ConnectionRequest, Long> {
    List<ConnectionRequest> findByReceiverIdAndStatus(Long receiverId, ConnectionRequestStatus status);
    List<ConnectionRequest> findBySenderIdAndStatus(Long senderId, ConnectionRequestStatus status);
    Optional<ConnectionRequest> findBySenderIdAndReceiverId(Long senderId, Long receiverId);
}
