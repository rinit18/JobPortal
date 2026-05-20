package com.jobportal.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jobportal.entity.ContactMessage;
import com.jobportal.entity.Feedback;
import com.jobportal.repository.ContactMessageRepository;
import com.jobportal.repository.FeedbackRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    public Map<String, Long> getPlatformStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalJobs", jobRepository.count());
        stats.put("totalFeedback", feedbackRepository.count());
        stats.put("totalContactMessages", contactMessageRepository.count());
        return stats;
    }

    public List<Feedback> getAllFeedbacks() {
        return feedbackRepository.findAll();
    }

    public List<ContactMessage> getAllContactMessages() {
        return contactMessageRepository.findAll();
    }
}
