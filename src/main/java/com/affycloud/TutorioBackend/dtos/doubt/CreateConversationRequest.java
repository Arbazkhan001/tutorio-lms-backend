package com.affycloud.TutorioBackend.dtos.doubt;

import com.affycloud.TutorioBackend.enums.Role;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateConversationRequest {
    private UUID courseTypeId;
    private UUID senderId;
    // This initial message is provided by the sender (typically the student).
    private String messageText;
    private Role senderRole;

}
