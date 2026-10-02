package com.affycloud.TutorioBackend.dtos.reponse;

import lombok.Data;

import java.util.Map;

@Data
public class ApiResponseWithMeta {
    private String message;
    private boolean error;
    private Map<String, Object> meta;

    public ApiResponseWithMeta(String message, boolean error,Map<String, Object> meta) {
        this.message = message;
        this.error = error;
        this.meta = meta;
    }
}
