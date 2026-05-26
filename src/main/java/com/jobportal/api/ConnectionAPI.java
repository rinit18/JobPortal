package com.jobportal.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jobportal.dto.ConnectionRequestDTO;
import com.jobportal.dto.ProfileDTO;
import com.jobportal.exception.JobPortalException;
import com.jobportal.service.ConnectionService;

@RestController
@CrossOrigin
@RequestMapping("/connections")
public class ConnectionAPI {
    
    @Autowired
    private ConnectionService connectionService;

    @PostMapping("/send/{senderId}/{receiverId}")
    public ResponseEntity<ConnectionRequestDTO> sendRequest(@PathVariable Long senderId, @PathVariable Long receiverId) throws JobPortalException {
        return new ResponseEntity<>(connectionService.sendConnectionRequest(senderId, receiverId), HttpStatus.OK);
    }

    @PostMapping("/accept/{requestId}")
    public ResponseEntity<ConnectionRequestDTO> acceptRequest(@PathVariable Long requestId) throws JobPortalException {
        return new ResponseEntity<>(connectionService.acceptConnectionRequest(requestId), HttpStatus.OK);
    }

    @PostMapping("/reject/{requestId}")
    public ResponseEntity<Void> rejectRequest(@PathVariable Long requestId) throws JobPortalException {
        connectionService.rejectConnectionRequest(requestId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/withdraw/{senderId}/{receiverId}")
    public ResponseEntity<Void> withdrawRequest(@PathVariable Long senderId, @PathVariable Long receiverId) throws JobPortalException {
        connectionService.withdrawConnectionRequest(senderId, receiverId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/remove/{userId1}/{userId2}")
    public ResponseEntity<Void> removeConnection(@PathVariable Long userId1, @PathVariable Long userId2) throws JobPortalException {
        connectionService.removeConnection(userId1, userId2);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/requests/{userId}")
    public ResponseEntity<List<ConnectionRequestDTO>> getPendingRequests(@PathVariable Long userId) throws JobPortalException {
        return new ResponseEntity<>(connectionService.getPendingRequests(userId), HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ProfileDTO>> getConnections(@PathVariable Long userId) throws JobPortalException {
        return new ResponseEntity<>(connectionService.getConnections(userId), HttpStatus.OK);
    }

    @GetMapping("/suggestions/{userId}")
    public ResponseEntity<List<ProfileDTO>> getSuggestions(@PathVariable Long userId) throws JobPortalException {
        return new ResponseEntity<>(connectionService.getSuggestions(userId), HttpStatus.OK);
    }

    @GetMapping("/status/{currentUserId}/{targetUserId}")
    public ResponseEntity<String> getConnectionStatus(@PathVariable Long currentUserId, @PathVariable Long targetUserId) throws JobPortalException {
        return new ResponseEntity<>(connectionService.getConnectionStatus(currentUserId, targetUserId), HttpStatus.OK);
    }

    @PostMapping("/mock/{userId}")
    public ResponseEntity<String> generateMockData(@PathVariable Long userId) throws JobPortalException {
        connectionService.generateMockData(userId);
        return new ResponseEntity<>("Mock data generated successfully", HttpStatus.OK);
    }
}
