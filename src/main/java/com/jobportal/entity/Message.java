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
@Document(collection = "messages")
public class Message {
    @Id
    private String id;
    @Indexed
    private String chatRoomId;
    private Long senderId; // sender profile ID
    private Long recipientId; // recipient profile ID
    private String text;
    private LocalDateTime timestamp;
}
