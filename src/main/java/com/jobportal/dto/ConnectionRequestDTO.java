package com.jobportal.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionRequestDTO {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private ConnectionRequestStatus status;
    private LocalDateTime createdAt;
    private ProfileDTO sender; // Optional populated field
    private ProfileDTO receiver; // Optional populated field
}
