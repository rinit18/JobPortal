package com.jobportal.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jobportal.dto.ConnectionRequestDTO;
import com.jobportal.dto.ConnectionRequestStatus;
import com.jobportal.dto.ProfileDTO;
import com.jobportal.entity.ConnectionRequest;
import com.jobportal.entity.Profile;
import com.jobportal.exception.JobPortalException;
import com.jobportal.repository.ConnectionRequestRepository;
import com.jobportal.repository.ProfileRepository;
import com.jobportal.utility.Utilities;

@Service("connectionService")
public class ConnectionServiceImpl implements ConnectionService {

    @Autowired
    private ConnectionRequestRepository connectionRequestRepository;
    
    @Autowired
    private ProfileRepository profileRepository;

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
        return req.toDTO();
    }

    @Override
    public void rejectConnectionRequest(Long requestId) throws JobPortalException {
        ConnectionRequest req = connectionRequestRepository.findById(requestId).orElseThrow(() -> new JobPortalException("Request not found"));
        req.setStatus(ConnectionRequestStatus.REJECTED);
        connectionRequestRepository.save(req);
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
}
