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

import com.jobportal.entity.ChatRoom;
import com.jobportal.entity.Message;
import com.jobportal.entity.Profile;
import com.jobportal.repository.ChatRoomRepository;
import com.jobportal.repository.MessageRepository;
import com.jobportal.repository.ProfileRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.jobportal.dto.NotificationDTO;
import com.jobportal.service.EmailService;
import com.jobportal.service.NotificationService;

@RestController
@CrossOrigin
@RequestMapping("/chats")
@Validated
public class ChatAPI {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private com.jobportal.repository.UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/room-by-user")
    public ResponseEntity<ChatRoom> getOrCreateRoomByUser(@RequestParam Long senderId, @RequestParam Long recipientUserId) throws com.jobportal.exception.JobPortalException {
        com.jobportal.dto.UserDTO currentUser = userService.getCurrentUser();
        Long actualSenderId = currentUser.getProfileId();

        Optional<com.jobportal.entity.User> userOpt = userRepository.findById(recipientUserId);
        Long recipientProfileId = recipientUserId; // Fallback
        if (userOpt.isPresent() && userOpt.get().getProfileId() != null) {
            recipientProfileId = userOpt.get().getProfileId();
        }
        return getOrCreateRoom(actualSenderId, recipientProfileId);
    }

    @PostMapping("/room")
    public ResponseEntity<ChatRoom> getOrCreateRoom(@RequestParam Long senderId, @RequestParam Long recipientId) {
        try {
            com.jobportal.dto.UserDTO currentUser = userService.getCurrentUser();
            Long actualSenderId = currentUser.getProfileId();
            senderId = actualSenderId;
        } catch (com.jobportal.exception.JobPortalException e) {
            throw new RuntimeException("Unauthorized");
        }

        Long minId = Math.min(senderId, recipientId);
        Long maxId = Math.max(senderId, recipientId);
        String roomId = minId + "-" + maxId;

        Optional<ChatRoom> existingRoom = chatRoomRepository.findById(roomId);
        if (existingRoom.isPresent()) {
            return new ResponseEntity<>(existingRoom.get(), HttpStatus.OK);
        }

        // Validate Connection
        Optional<Profile> senderProfile = profileRepository.findById(senderId);
        if (senderProfile.isEmpty() || senderProfile.get().getConnections() == null || !senderProfile.get().getConnections().contains(recipientId)) {
            throw new RuntimeException("You can only message your connections.");
        }
        ChatRoom room = new ChatRoom();
        room.setId(roomId);
        room.setUser1Id(senderId);
        room.setUser2Id(recipientId);
        room.setLastActive(LocalDateTime.now());
        room.setLastMessage("Started conversation");

        Optional<Profile> p1 = profileRepository.findById(senderId);
        if (p1.isPresent()) {
            room.setUser1Name(p1.get().getName());
            String role = p1.get().getJobTitle();
            if (p1.get().getCompany() != null && !p1.get().getCompany().isEmpty()) {
                role += " at " + p1.get().getCompany();
            }
            room.setUser1Role(role != null ? role : "Member");
        } else {
            room.setUser1Name("User " + senderId);
            room.setUser1Role("Member");
        }

        Optional<Profile> p2 = profileRepository.findById(recipientId);
        if (p2.isPresent()) {
            room.setUser2Name(p2.get().getName());
            String role = p2.get().getJobTitle();
            if (p2.get().getCompany() != null && !p2.get().getCompany().isEmpty()) {
                role += " at " + p2.get().getCompany();
            }
            room.setUser2Role(role != null ? role : "Member");
        } else {
            room.setUser2Name("User " + recipientId);
            room.setUser2Role("Member");
        }

        ChatRoom saved = chatRoomRepository.save(room);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/conversations/{profileId}")
    public ResponseEntity<List<ChatRoom>> getConversations(@PathVariable Long profileId) throws com.jobportal.exception.JobPortalException {
        com.jobportal.dto.UserDTO currentUser = userService.getCurrentUser();
        Long actualProfileId = currentUser.getProfileId();
        List<ChatRoom> rooms = chatRoomRepository.findByUser1IdOrUser2IdOrderByLastActiveDesc(actualProfileId, actualProfileId);
        return new ResponseEntity<>(rooms, HttpStatus.OK);
    }

    @GetMapping("/messages/{chatRoomId}")
    public ResponseEntity<List<Message>> getMessages(@PathVariable String chatRoomId) throws com.jobportal.exception.JobPortalException {
        com.jobportal.dto.UserDTO currentUser = userService.getCurrentUser();
        Long actualProfileId = currentUser.getProfileId();

        Optional<ChatRoom> room = chatRoomRepository.findById(chatRoomId);
        if (room.isEmpty() || (!room.get().getUser1Id().equals(actualProfileId) && !room.get().getUser2Id().equals(actualProfileId))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<Message> messages = messageRepository.findByChatRoomIdOrderByTimestampAsc(chatRoomId);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(@RequestBody Message message) throws com.jobportal.exception.JobPortalException {
        com.jobportal.dto.UserDTO currentUser = userService.getCurrentUser();
        Long actualProfileId = currentUser.getProfileId();

        Optional<ChatRoom> roomOpt = chatRoomRepository.findById(message.getChatRoomId());
        if (roomOpt.isEmpty() || (!roomOpt.get().getUser1Id().equals(actualProfileId) && !roomOpt.get().getUser2Id().equals(actualProfileId))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        message.setSenderId(actualProfileId);
        message.setTimestamp(LocalDateTime.now());
        Message savedMessage = messageRepository.save(message);

        // Update last message and active time in parent chat room
        roomOpt = chatRoomRepository.findById(message.getChatRoomId());
        if (roomOpt.isPresent()) {
            ChatRoom room = roomOpt.get();
            room.setLastMessage(message.getText());
            room.setLastActive(LocalDateTime.now());
            chatRoomRepository.save(room);
            
            try {
                NotificationDTO notiDto = new NotificationDTO();
                notiDto.setAction("New Message");
                
                String senderName = "Someone";
                if (message.getSenderId().equals(room.getUser1Id())) {
                    senderName = room.getUser1Name();
                } else if (message.getSenderId().equals(room.getUser2Id())) {
                    senderName = room.getUser2Name();
                }
                
                notiDto.setMessage("You received a new message from " + senderName);
                notiDto.setUserId(message.getRecipientId());
                notiDto.setRoute("/messages?roomId=" + room.getId());
                notificationService.sendNotification(notiDto);

                // Send Gmail to recipient
                final String finalSenderName = senderName;
                final String msgPreview = message.getText() != null && message.getText().length() > 120
                    ? message.getText().substring(0, 120) + "..."
                    : message.getText();
                userRepository.findById(message.getRecipientId()).ifPresent(recipient -> {
                    String recipientName = recipient.getName() != null ? recipient.getName() : "there";
                    emailService.sendNewMessageEmail(recipient.getEmail(), recipientName, finalSenderName, msgPreview);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Broadcast to receiver via WebSockets topic
        messagingTemplate.convertAndSend(
            "/topic/messages/" + message.getRecipientId(), 
            savedMessage
        );

        return new ResponseEntity<>(savedMessage, HttpStatus.CREATED);
    }
}
