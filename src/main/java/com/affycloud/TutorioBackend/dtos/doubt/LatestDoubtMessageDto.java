package com.affycloud.TutorioBackend.dtos.doubt;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class LatestDoubtMessageDto {
    private UUID conversationId;
    private DoubtCourseDetailDto courseType;
    private SenderStudentDetailDto student;
    private DoubtMessageDto message;
    private Date conversationCreatedAt;
    private Date conversationUpdatedAt;
}

