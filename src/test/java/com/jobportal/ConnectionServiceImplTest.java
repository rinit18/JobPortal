package com.jobportal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jobportal.dto.ConnectionRequestDTO;
import com.jobportal.dto.ConnectionRequestStatus;
import com.jobportal.entity.ConnectionRequest;
import com.jobportal.entity.Profile;
import com.jobportal.exception.JobPortalException;
import com.jobportal.repository.ConnectionRequestRepository;
import com.jobportal.repository.ProfileRepository;
import com.jobportal.service.ConnectionServiceImpl;
import com.jobportal.service.NotificationService;
import com.jobportal.utility.Utilities;

@ExtendWith(MockitoExtension.class)
public class ConnectionServiceImplTest {

    @Mock
    private ConnectionRequestRepository connectionRequestRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ConnectionServiceImpl connectionService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSendConnectionRequest_Success() throws Exception {
        try (MockedStatic<Utilities> utilities = mockStatic(Utilities.class)) {
            utilities.when(() -> Utilities.getNextSequenceId("connectionRequests")).thenReturn(50L);

            when(connectionRequestRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(Optional.empty());
            when(connectionRequestRepository.findBySenderIdAndReceiverId(2L, 1L)).thenReturn(Optional.empty());

            ConnectionRequest savedReq = new ConnectionRequest();
            savedReq.setId(50L);
            savedReq.setSenderId(1L);
            savedReq.setReceiverId(2L);
            savedReq.setStatus(ConnectionRequestStatus.PENDING);
            
            when(connectionRequestRepository.save(any(ConnectionRequest.class))).thenReturn(savedReq);
            
            Profile sender = new Profile();
            sender.setId(1L);
            sender.setName("Alice");
            when(profileRepository.findById(1L)).thenReturn(Optional.of(sender));

            ConnectionRequestDTO result = connectionService.sendConnectionRequest(1L, 2L);

            assertNotNull(result);
            assertEquals(50L, result.getId());
            assertEquals(ConnectionRequestStatus.PENDING, result.getStatus());
            verify(notificationService, times(1)).sendNotification(any());
        }
    }

    @Test
    public void testSendConnectionRequest_AlreadySent_ThrowsException() {
        ConnectionRequest existing = new ConnectionRequest();
        when(connectionRequestRepository.findBySenderIdAndReceiverId(1L, 2L)).thenReturn(Optional.of(existing));

        JobPortalException exception = assertThrows(JobPortalException.class, () -> {
            connectionService.sendConnectionRequest(1L, 2L);
        });

        assertEquals("Connection request already sent", exception.getMessage());
    }

    @Test
    public void testAcceptConnectionRequest_Success() throws Exception {
        ConnectionRequest req = new ConnectionRequest();
        req.setId(50L);
        req.setSenderId(1L);
        req.setReceiverId(2L);
        req.setStatus(ConnectionRequestStatus.PENDING);

        when(connectionRequestRepository.findById(50L)).thenReturn(Optional.of(req));
        when(connectionRequestRepository.save(any(ConnectionRequest.class))).thenReturn(req);

        Profile sender = new Profile();
        sender.setId(1L);
        
        Profile receiver = new Profile();
        receiver.setId(2L);
        receiver.setName("Bob");

        when(profileRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(profileRepository.findById(2L)).thenReturn(Optional.of(receiver));

        ConnectionRequestDTO result = connectionService.acceptConnectionRequest(50L);

        assertEquals(ConnectionRequestStatus.ACCEPTED, result.getStatus());
        assertTrue(sender.getConnections().contains(2L), "Sender should have receiver in connections");
        assertTrue(receiver.getConnections().contains(1L), "Receiver should have sender in connections");
        verify(profileRepository, times(2)).save(any(Profile.class));
        verify(notificationService, times(1)).sendNotification(any());
    }
}
