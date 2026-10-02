package com.affycloud.TutorioBackend.dtos.doubt;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
public class DoubtConversationDto {
    private UUID conversationId;
    private UUID courseTypeId;
    private SenderStudentDetailDto student;
    private List<DoubtMessageDto> messages;
    private Date createdAt;
    private Date updatedAt;
}
