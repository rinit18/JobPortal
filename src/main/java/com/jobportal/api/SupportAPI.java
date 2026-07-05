package com.jobportal.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;

import com.jobportal.entity.ContactMessage;
import com.jobportal.entity.FAQ;
import com.jobportal.entity.Feedback;
import com.jobportal.service.SupportService;

@RestController
@CrossOrigin
@RequestMapping("/support")
@Validated
public class SupportAPI {

    @Autowired
    private SupportService supportService;

    @GetMapping("/faqs")
    public ResponseEntity<List<FAQ>> getAllFAQs() {
        return new ResponseEntity<>(supportService.getAllFAQs(), HttpStatus.OK);
    }

    @PostMapping("/contact")
    public ResponseEntity<String> submitContactMessage(@Valid @RequestBody ContactMessage message) {
        supportService.submitContactMessage(message);
        return new ResponseEntity<>("Message sent successfully", HttpStatus.CREATED);
    }

    @PostMapping("/feedback")
    public ResponseEntity<String> submitFeedback(@Valid @RequestBody Feedback feedback) {
        supportService.submitFeedback(feedback);
        return new ResponseEntity<>("Feedback submitted successfully", HttpStatus.CREATED);
    }
}
