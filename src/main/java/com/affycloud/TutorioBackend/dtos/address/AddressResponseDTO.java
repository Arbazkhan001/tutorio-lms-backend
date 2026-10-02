package com.affycloud.TutorioBackend.dtos.address;

import lombok.Data;

import java.util.UUID;

@Data
public class AddressResponseDTO {
    private UUID id;
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private boolean isDefault;
}
