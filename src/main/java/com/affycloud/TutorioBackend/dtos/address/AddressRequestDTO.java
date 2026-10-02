package com.affycloud.TutorioBackend.dtos.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressRequestDTO {
    @NotBlank(message = "Street is required")
    @Size(max = 128)
    private String street;

    @NotBlank(message = "City is required")
    @Size(max = 128)
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 128)
    private String state;

    @NotBlank(message = "Postal Code is required")
    @Size(max = 10)
    private String postalCode;

    @NotBlank(message = "Country is required")
    @Size(max = 128)
    private String country;
    private boolean isDefault;
}
