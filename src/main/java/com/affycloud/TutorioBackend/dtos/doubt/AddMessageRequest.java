package com.affycloud.TutorioBackend.dtos.doubt;

import com.affycloud.TutorioBackend.enums.Role;
import lombok.Data;

import java.util.UUID;

@Data
public class AddMessageRequest {
    private UUID senderId;
    private Role senderRole;
    private String messageText;
}
