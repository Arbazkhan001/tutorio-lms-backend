package com.affycloud.TutorioBackend.dtos.doubt;

import com.affycloud.TutorioBackend.enums.Role;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class DoubtMessageDto {
    private UUID id;
    private Role senderRole;
    private SenderStudentDetailDto studentSender;
    private SenderUserDetailDto userSender;
    private String messageText;
    private Date createdAt;
}
