package com.affycloud.TutorioBackend.dtos.websocket;

import lombok.Data;

import java.util.UUID;

@Data
public class ConversationRequestDto {
    private UUID studentId;
    private UUID courseTypeId;
}