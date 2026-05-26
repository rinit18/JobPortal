package com.jobportal.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.jobportal.dto.ConnectionRequestDTO;
import com.jobportal.dto.ConnectionRequestStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "connection_requests")
public class ConnectionRequest {
    @Id
    private Long id;
    @Indexed
    private Long senderId;
    @Indexed
    private Long receiverId;
    private ConnectionRequestStatus status;
    private LocalDateTime createdAt;
    
    public ConnectionRequestDTO toDTO() {
        return new ConnectionRequestDTO(this.id, this.senderId, this.receiverId, this.status, this.createdAt, null, null);
    }
}
