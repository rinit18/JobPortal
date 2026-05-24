package com.jobportal.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.jobportal.entity.ChatRoom;

public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {
    List<ChatRoom> findByUser1IdOrUser2IdOrderByLastActiveDesc(Long user1Id, Long user2Id);
}
