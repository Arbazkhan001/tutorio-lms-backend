package com.affycloud.TutorioBackend.dtos.doubt;

import lombok.Data;

import java.util.UUID;

@Data
public class DoubtConversationStudentsListDto {
    private UUID conversationId;
    private SenderStudentDetailDto student;
}
