package com.jobportal.api;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jobportal.entity.ContactMessage;
import com.jobportal.entity.Feedback;
import com.jobportal.service.AdminService;

@RestController
@CrossOrigin
@RequestMapping("/admin")
public class AdminAPI {

    @Autowired
    private AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        return new ResponseEntity<>(adminService.getPlatformStats(), HttpStatus.OK);
    }

    @GetMapping("/feedbacks")
    public ResponseEntity<List<Feedback>> getFeedbacks() {
        return new ResponseEntity<>(adminService.getAllFeedbacks(), HttpStatus.OK);
    }

    @GetMapping("/contacts")
    public ResponseEntity<List<ContactMessage>> getContacts() {
        return new ResponseEntity<>(adminService.getAllContactMessages(), HttpStatus.OK);
    }
}
