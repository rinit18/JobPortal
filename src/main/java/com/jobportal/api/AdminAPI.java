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
import com.jobportal.entity.Job;
import com.jobportal.entity.User;
import com.jobportal.service.AdminService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

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

    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers() {
        return new ResponseEntity<>(adminService.getAllUsers(), HttpStatus.OK);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<Job>> getJobs() {
        return new ResponseEntity<>(adminService.getAllJobs(), HttpStatus.OK);
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        adminService.deleteJob(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/feedbacks/{id}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable String id) {
        adminService.deleteFeedback(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/contacts/{id}")
    public ResponseEntity<Void> deleteContactMessage(@PathVariable String id) {
        adminService.deleteContactMessage(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
