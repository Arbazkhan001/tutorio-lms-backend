package com.affycloud.TutorioBackend.dtos.zoom;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data; 

@Data
public class CreateMeetingDTO {

    @NotBlank
    private String topic;

    @NotNull
    private Integer type;

    @NotNull
    private String startTime;

    @NotNull
    private Integer duration;

    @NotBlank
    private String timezone;

    private String agenda;

    @NotNull
    @Valid  
    private MeetingSettingsDTO settings;
}

