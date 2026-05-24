package com.jobportal.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.jobportal.entity.Message;

public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findByChatRoomIdOrderByTimestampAsc(String chatRoomId);
}
