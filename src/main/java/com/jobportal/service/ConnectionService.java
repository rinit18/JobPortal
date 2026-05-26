package com.jobportal.service;

import java.util.List;

import com.jobportal.dto.ConnectionRequestDTO;
import com.jobportal.dto.ProfileDTO;
import com.jobportal.exception.JobPortalException;

public interface ConnectionService {
    public ConnectionRequestDTO sendConnectionRequest(Long senderId, Long receiverId) throws JobPortalException;
    public ConnectionRequestDTO acceptConnectionRequest(Long requestId) throws JobPortalException;
    public void rejectConnectionRequest(Long requestId) throws JobPortalException;
    public void withdrawConnectionRequest(Long senderId, Long receiverId) throws JobPortalException;
    public void removeConnection(Long userId1, Long userId2) throws JobPortalException;
    public List<ConnectionRequestDTO> getPendingRequests(Long userId) throws JobPortalException;
    public List<ProfileDTO> getConnections(Long userId) throws JobPortalException;
    public List<ProfileDTO> getSuggestions(Long userId) throws JobPortalException;
    public void generateMockData() throws JobPortalException;
    // Returns: "CONNECTED", "PENDING_SENT", "PENDING_RECEIVED", "NONE"
    public String getConnectionStatus(Long currentUserId, Long targetUserId) throws JobPortalException;
}
