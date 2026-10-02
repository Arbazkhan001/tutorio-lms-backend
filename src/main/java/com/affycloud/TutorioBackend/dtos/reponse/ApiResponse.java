package com.affycloud.TutorioBackend.dtos.reponse;

import lombok.Data;

@Data
public class ApiResponse {
    private String message;
    private boolean error;

    // Constructor
    public ApiResponse(String message, boolean error) {
        this.message = message;
        this.error = error;
    }
}

