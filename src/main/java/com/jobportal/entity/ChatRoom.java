package com.jobportal.entity;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_rooms")
public class ChatRoom {
    @Id
    private String id; // format: "minProfileId-maxProfileId" to prevent duplicate rooms
    @Indexed
    private Long user1Id; // applicant profile ID or recruiter profile ID
    @Indexed
    private Long user2Id; // opposite profile ID
    private String user1Name;
    private String user2Name;
    private String user1Role;
    private String user2Role;
    private String lastMessage;
    private LocalDateTime lastActive;
}
