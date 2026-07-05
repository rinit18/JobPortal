package com.jobportal.service;

import com.jobportal.entity.ChatRoom;
import com.jobportal.entity.Message;
import com.jobportal.exception.JobPortalException;

import java.util.List;

public interface ChatService {
    ChatRoom getOrCreateRoomByUser(Long senderId, Long recipientUserId) throws JobPortalException;
    ChatRoom getOrCreateRoom(Long senderId, Long recipientId) throws JobPortalException;
    List<ChatRoom> getConversations(Long profileId) throws JobPortalException;
    List<Message> getMessages(String chatRoomId, Long currentProfileId) throws JobPortalException;
    Message sendMessage(Message message, Long currentProfileId) throws JobPortalException;
}
