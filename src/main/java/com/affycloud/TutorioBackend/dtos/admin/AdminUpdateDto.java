package com.affycloud.TutorioBackend.dtos.admin;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AdminUpdateDto {
    private String name;
    private String phoneNumber;
    private String orgarnisationName;
    @Pattern(
            regexp = "^[a-zA-Z0-9-]+$",
            message = "Subdomain can only contain letters, numbers, and hyphens"
    )
    private String subDomain;
}
