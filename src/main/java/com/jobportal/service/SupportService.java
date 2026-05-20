package com.jobportal.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jobportal.entity.ContactMessage;
import com.jobportal.entity.FAQ;
import com.jobportal.entity.Feedback;
import com.jobportal.repository.ContactMessageRepository;
import com.jobportal.repository.FAQRepository;
import com.jobportal.repository.FeedbackRepository;

@Service
public class SupportService {

    @Autowired
    private FAQRepository faqRepository;

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    public List<FAQ> getAllFAQs() {
        return faqRepository.findAll();
    }

    public void submitContactMessage(ContactMessage message) {
        message.setTimestamp(LocalDateTime.now());
        contactMessageRepository.save(message);
    }

    public void submitFeedback(Feedback feedback) {
        feedback.setTimestamp(LocalDateTime.now());
        feedbackRepository.save(feedback);
    }
}
