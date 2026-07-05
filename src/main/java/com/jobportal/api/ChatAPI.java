package com.jobportal.api;

import com.jobportal.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.jobportal.entity.ChatRoom;
import com.jobportal.entity.Message;
import com.jobportal.entity.Profile;
import com.jobportal.dto.NotificationDTO;
import com.jobportal.service.ChatService;
import com.jobportal.service.EmailService;
import com.jobportal.service.NotificationService;

@RestController
@CrossOrigin
@RequestMapping("/chats")
@Validated
public class ChatAPI {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @PostMapping("/room-by-user")
    public ResponseEntity<ChatRoom> getOrCreateRoomByUser(@RequestParam Long senderId, @RequestParam Long recipientUserId) throws com.jobportal.exception.JobPortalException {
        com.jobportal.dto.UserDTO currentUser = userService.getCurrentUser();
        Long actualSenderId = currentUser.getProfileId();

        return new ResponseEntity<>(chatService.getOrCreateRoomByUser(actualSenderId, recipientUserId), HttpStatus.OK);
    }

    @PostMapping("/room")
    public ResponseEntity<ChatRoom> getOrCreateRoom(@RequestParam Long senderId, @RequestParam Long recipientId) throws com.jobportal.exception.JobPortalException {
        try {
            com.jobportal.dto.UserDTO currentUser = userService.getCurrentUser();
            Long actualSenderId = currentUser.getProfileId();
            senderId = actualSenderId;
        } catch (com.jobportal.exception.JobPortalException e) {
            throw new RuntimeException("Unauthorized");
        }

        return new ResponseEntity<>(chatService.getOrCreateRoom(senderId, recipientId), HttpStatus.CREATED);
    }

    @GetMapping("/conversations/{profileId}")
    public ResponseEntity<List<ChatRoom>> getConversations(@PathVariable Long profileId) throws com.jobportal.exception.JobPortalException {
        com.jobportal.dto.UserDTO currentUser = userService.getCurrentUser();
        Long actualProfileId = currentUser.getProfileId();
        List<ChatRoom> rooms = chatService.getConversations(actualProfileId);
        return new ResponseEntity<>(rooms, HttpStatus.OK);
    }

    @GetMapping("/messages/{chatRoomId}")
    public ResponseEntity<List<Message>> getMessages(@PathVariable String chatRoomId) throws com.jobportal.exception.JobPortalException {
        com.jobportal.dto.UserDTO currentUser = userService.getCurrentUser();
        Long actualProfileId = currentUser.getProfileId();

        try {
            List<Message> messages = chatService.getMessages(chatRoomId, actualProfileId);
            return new ResponseEntity<>(messages, HttpStatus.OK);
        } catch (com.jobportal.exception.JobPortalException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(@Valid @RequestBody Message message) throws com.jobportal.exception.JobPortalException {
        com.jobportal.dto.UserDTO currentUser = userService.getCurrentUser();
        Long actualProfileId = currentUser.getProfileId();

        try {
            Message savedMessage = chatService.sendMessage(message, actualProfileId);
            return new ResponseEntity<>(savedMessage, HttpStatus.CREATED);
        } catch (com.jobportal.exception.JobPortalException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }
}
