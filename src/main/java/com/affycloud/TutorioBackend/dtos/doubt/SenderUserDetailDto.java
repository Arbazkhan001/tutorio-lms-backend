package com.affycloud.TutorioBackend.dtos.doubt;

import lombok.Data;

import java.util.UUID;

@Data
public class SenderUserDetailDto {

    private UUID userId;
    private String name;
    private String email;
    private String phoneNumber;
}
