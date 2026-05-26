package com.jobportal.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jobportal.dto.ConnectionRequestDTO;
import com.jobportal.dto.ConnectionRequestStatus;
import com.jobportal.dto.NotificationDTO;
import com.jobportal.dto.ProfileDTO;
import com.jobportal.entity.ConnectionRequest;
import com.jobportal.entity.Profile;
import com.jobportal.exception.JobPortalException;
import com.jobportal.repository.ConnectionRequestRepository;
import com.jobportal.repository.ProfileRepository;
import com.jobportal.repository.PostRepository;
import com.jobportal.entity.Post;
import com.jobportal.utility.Utilities;

@Service("connectionService")
public class ConnectionServiceImpl implements ConnectionService {

    @Autowired
    private ConnectionRequestRepository connectionRequestRepository;
    
    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    public ConnectionRequestDTO sendConnectionRequest(Long senderId, Long receiverId) throws JobPortalException {
        if (senderId.equals(receiverId)) throw new JobPortalException("Cannot send request to yourself");
        Optional<ConnectionRequest> existing = connectionRequestRepository.findBySenderIdAndReceiverId(senderId, receiverId);
        if (existing.isPresent()) throw new JobPortalException("Connection request already sent");
        
        Optional<ConnectionRequest> reverse = connectionRequestRepository.findBySenderIdAndReceiverId(receiverId, senderId);
        if (reverse.isPresent() && reverse.get().getStatus() == ConnectionRequestStatus.PENDING) {
            return acceptConnectionRequest(reverse.get().getId());
        }

        ConnectionRequest req = new ConnectionRequest(Utilities.getNextSequenceId("connectionRequests"), senderId, receiverId, ConnectionRequestStatus.PENDING, LocalDateTime.now());
        req = connectionRequestRepository.save(req);

        // Send Notification
        Profile senderProfile = profileRepository.findById(senderId).orElse(null);
        if (senderProfile != null) {
            NotificationDTO noti = new NotificationDTO();
            noti.setUserId(receiverId);
            noti.setAction("New Connection Request");
            noti.setMessage(senderProfile.getName() + " wants to connect with you.");
            noti.setRoute("/network");
            notificationService.sendNotification(noti);
        }

        return req.toDTO();
    }

    @Override
    public ConnectionRequestDTO acceptConnectionRequest(Long requestId) throws JobPortalException {
        ConnectionRequest req = connectionRequestRepository.findById(requestId).orElseThrow(() -> new JobPortalException("Request not found"));
        req.setStatus(ConnectionRequestStatus.ACCEPTED);
        req = connectionRequestRepository.save(req);
        
        Profile sender = profileRepository.findById(req.getSenderId()).orElseThrow(() -> new JobPortalException("Sender profile not found"));
        Profile receiver = profileRepository.findById(req.getReceiverId()).orElseThrow(() -> new JobPortalException("Receiver profile not found"));
        
        if (sender.getConnections() == null) sender.setConnections(new ArrayList<>());
        if (receiver.getConnections() == null) receiver.setConnections(new ArrayList<>());
        
        if (!sender.getConnections().contains(receiver.getId())) sender.getConnections().add(receiver.getId());
        if (!receiver.getConnections().contains(sender.getId())) receiver.getConnections().add(sender.getId());
        
        profileRepository.save(sender);
        profileRepository.save(receiver);

        // Send Notification
        NotificationDTO noti = new NotificationDTO();
        noti.setUserId(req.getSenderId());
        noti.setAction("Connection Request Accepted");
        noti.setMessage(receiver.getName() + " accepted your connection request.");
        noti.setRoute("/user/" + receiver.getId());
        notificationService.sendNotification(noti);

        return req.toDTO();
    }

    @Override
    public void rejectConnectionRequest(Long requestId) throws JobPortalException {
        ConnectionRequest req = connectionRequestRepository.findById(requestId).orElseThrow(() -> new JobPortalException("Request not found"));
        req.setStatus(ConnectionRequestStatus.REJECTED);
        connectionRequestRepository.save(req);
    }

    @Override
    public void withdrawConnectionRequest(Long senderId, Long receiverId) throws JobPortalException {
        ConnectionRequest req = connectionRequestRepository.findBySenderIdAndReceiverId(senderId, receiverId)
            .orElseThrow(() -> new JobPortalException("Request not found"));
        if (req.getStatus() == ConnectionRequestStatus.PENDING) {
            connectionRequestRepository.delete(req);
        }
    }

    @Override
    public void removeConnection(Long userId1, Long userId2) throws JobPortalException {
        Profile p1 = profileRepository.findById(userId1).orElseThrow(() -> new JobPortalException("Profile 1 not found"));
        Profile p2 = profileRepository.findById(userId2).orElseThrow(() -> new JobPortalException("Profile 2 not found"));

        if (p1.getConnections() != null) {
            p1.getConnections().removeIf(id -> id.equals(userId2));
            profileRepository.save(p1);
        }
        if (p2.getConnections() != null) {
            p2.getConnections().removeIf(id -> id.equals(userId1));
            profileRepository.save(p2);
        }
        
        // Also remove any existing requests so they can connect again later
        connectionRequestRepository.findBySenderIdAndReceiverId(userId1, userId2).ifPresent(r -> connectionRequestRepository.delete(r));
        connectionRequestRepository.findBySenderIdAndReceiverId(userId2, userId1).ifPresent(r -> connectionRequestRepository.delete(r));
    }

    @Override
    public List<ConnectionRequestDTO> getPendingRequests(Long userId) throws JobPortalException {
        List<ConnectionRequest> reqs = connectionRequestRepository.findByReceiverIdAndStatus(userId, ConnectionRequestStatus.PENDING);
        return reqs.stream().map(req -> {
            ConnectionRequestDTO dto = req.toDTO();
            profileRepository.findById(req.getSenderId()).ifPresent(p -> dto.setSender(p.toDTO()));
            return dto;
        }).toList();
    }

    @Override
    public List<ProfileDTO> getConnections(Long userId) throws JobPortalException {
        Profile profile = profileRepository.findById(userId).orElseThrow(() -> new JobPortalException("Profile not found"));
        if (profile.getConnections() == null || profile.getConnections().isEmpty()) return new ArrayList<>();
        List<Profile> connections = (List<Profile>) profileRepository.findAllById(profile.getConnections());
        return connections.stream().map(Profile::toDTO).toList();
    }

    @Override
    public List<ProfileDTO> getSuggestions(Long userId) throws JobPortalException {
        Profile profile = profileRepository.findById(userId).orElseThrow(() -> new JobPortalException("Profile not found"));
        List<Long> excludeIds = new ArrayList<>();
        excludeIds.add(userId);
        if (profile.getConnections() != null) excludeIds.addAll(profile.getConnections());
        
        // Exclude pending sent/received requests
        connectionRequestRepository.findBySenderIdAndStatus(userId, ConnectionRequestStatus.PENDING).forEach(r -> excludeIds.add(r.getReceiverId()));
        connectionRequestRepository.findByReceiverIdAndStatus(userId, ConnectionRequestStatus.PENDING).forEach(r -> excludeIds.add(r.getSenderId()));
        
        // Simple suggestion logic: return profiles not in excludeIds, limit to 10
        List<Profile> all = profileRepository.findAll();
        return all.stream()
            .filter(p -> !excludeIds.contains(p.getId()))
            .limit(10)
            .map(Profile::toDTO)
            .toList();
    }

    @Override
    public void generateMockData() throws JobPortalException {
        long c = profileRepository.count();
        if (c > 3) return; // already have data

        Profile p1 = new Profile(Utilities.getNextSequenceId("profiles"), "Alice Johnson", "alice@test.com", "Frontend Developer", "Google", "California, USA", "Passionate about UI/UX", null, null, 4L, List.of("React", "CSS"), null, null, null, new ArrayList<>());
        Profile p2 = new Profile(Utilities.getNextSequenceId("profiles"), "Bob Smith", "bob@test.com", "Data Scientist", "Amazon", "Seattle, USA", "Love working with data.", null, null, 2L, List.of("Python", "SQL"), null, null, null, new ArrayList<>());
        Profile p3 = new Profile(Utilities.getNextSequenceId("profiles"), "Charlie Davis", "charlie@test.com", "Product Manager", "Microsoft", "Redmond, USA", "Building great products.", null, null, 6L, List.of("Agile", "Jira"), null, null, null, new ArrayList<>());

        profileRepository.saveAll(List.of(p1, p2, p3));

        Post post1 = new Post(Utilities.getNextSequenceId("posts"), p1.getId(), "Just started my new job at Google! So excited for the journey ahead.", null, new ArrayList<>(), new ArrayList<>(), LocalDateTime.now().minusHours(2));
        Post post2 = new Post(Utilities.getNextSequenceId("posts"), p2.getId(), "Does anyone have good recommendations for learning advanced PySpark? Looking to upskill.", null, new ArrayList<>(), new ArrayList<>(), LocalDateTime.now().minusHours(5));
        Post post3 = new Post(Utilities.getNextSequenceId("posts"), p3.getId(), "Product Management is 10% having ideas and 90% convincing others to build them.", null, new ArrayList<>(), new ArrayList<>(), LocalDateTime.now().minusDays(1));

        postRepository.saveAll(List.of(post1, post2, post3));
    }

    @Override
    public String getConnectionStatus(Long currentUserId, Long targetUserId) throws JobPortalException {
        Profile profile = profileRepository.findById(currentUserId).orElseThrow(() -> new JobPortalException("Profile not found"));
        if (profile.getConnections() != null && profile.getConnections().stream().anyMatch(id -> id.equals(targetUserId))) {
            return "CONNECTED";
        }
        Optional<ConnectionRequest> sent = connectionRequestRepository.findBySenderIdAndReceiverId(currentUserId, targetUserId);
        if (sent.isPresent() && sent.get().getStatus() == ConnectionRequestStatus.PENDING) return "PENDING_SENT";
        Optional<ConnectionRequest> received = connectionRequestRepository.findBySenderIdAndReceiverId(targetUserId, currentUserId);
        if (received.isPresent() && received.get().getStatus() == ConnectionRequestStatus.PENDING) return "PENDING_RECEIVED";
        return "NONE";
    }
}
