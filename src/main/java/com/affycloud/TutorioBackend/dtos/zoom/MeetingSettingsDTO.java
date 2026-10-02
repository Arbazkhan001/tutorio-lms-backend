package com.affycloud.TutorioBackend.dtos.zoom;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MeetingSettingsDTO {

    @NotNull
    private Boolean hostVideo;

    @NotNull
    private Boolean participantVideo;

    @NotNull
    private Boolean joinBeforeHost;

    @NotNull
    private Boolean muteUponEntry;

    @NotNull
    private Boolean waitingRoom;

}
