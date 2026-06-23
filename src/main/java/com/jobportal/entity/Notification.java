package com.jobportal.entity;


import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import com.jobportal.dto.NotificationDTO;
import com.jobportal.dto.NotificationStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notification")
public class Notification {
	@org.springframework.data.annotation.Id
	private Long id;
	@Indexed
	private Long userId;
	private String message;
	private String action;
	private String route;
	private NotificationStatus status;
	private LocalDateTime timestamp;
	public NotificationDTO toDTO() {
		return new NotificationDTO (this.id, this.userId, this.message, this.action, this.route, this.status, this.timestamp);
	}
}
