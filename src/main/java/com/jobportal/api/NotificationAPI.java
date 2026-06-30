package com.jobportal.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jobportal.dto.ResponseDTO;
import com.jobportal.entity.Notification;
import com.jobportal.exception.JobPortalException;
import com.jobportal.service.NotificationService;

@RestController
@CrossOrigin
@RequestMapping("/notification")
@Validated
public class NotificationAPI {
	@Autowired
	private NotificationService notificationService;

	@Autowired
	private com.jobportal.service.UserService userService;
	
	@Autowired
	private com.jobportal.repository.NotificationRepository notificationRepository;
	
	@GetMapping("/get/{userId}")
	public ResponseEntity<List<Notification>>getNotifications(@PathVariable Long userId) throws JobPortalException {
		com.jobportal.dto.UserDTO currentUser = userService.getCurrentUser();
		if (!currentUser.getId().equals(userId)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		return new ResponseEntity<>(notificationService.getNotifications(userId), HttpStatus.OK);
	}
	@PutMapping("/read/{id}")
	public ResponseEntity<ResponseDTO>readNotification(@PathVariable Long id) throws JobPortalException{
		com.jobportal.dto.UserDTO currentUser = userService.getCurrentUser();
		Notification noti = notificationRepository.findById(id).orElseThrow(() -> new JobPortalException("No Notification found"));
		if (!noti.getUserId().equals(currentUser.getId())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		notificationService.readNotification(id);
		return new ResponseEntity<>(new ResponseDTO("Success"), HttpStatus.OK);
	}
}
