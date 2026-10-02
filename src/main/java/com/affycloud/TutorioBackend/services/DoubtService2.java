package com.affycloud.TutorioBackend.services;


import com.affycloud.TutorioBackend.dtos.doubt.DoubtConversationDto;
import com.affycloud.TutorioBackend.dtos.doubt.DoubtMessageDto;
import com.affycloud.TutorioBackend.enums.Role;

import java.util.UUID;

public interface DoubtService2 {
    DoubtConversationDto createConversation(UUID courseTypeId, UUID studentId,
                                         String initialMessage,
                                         UUID senderId, Role senderRole);
    DoubtMessageDto addMessage(UUID conversationId, UUID senderId,
                               Role senderRole, String messageText);
    DoubtConversationDto getConversation(UUID conversationId);

    DoubtConversationDto getConversationByStudentAndCourse(UUID studentId, UUID courseTypeId);

}
