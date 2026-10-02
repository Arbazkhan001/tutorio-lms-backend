package com.affycloud.TutorioBackend.services;

import com.affycloud.TutorioBackend.dtos.address.AddressRequestDTO;
import com.affycloud.TutorioBackend.dtos.address.AddressResponseDTO;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;

import java.util.UUID;

public interface AddressService {
    AddressResponseDTO addAddress(UUID studentId, AddressRequestDTO addressRequest);
    ApiResponseWithMeta getAddresses(UUID studentId);
    public boolean softDeleteAddress(UUID addressId);
    ApiResponseWithMeta getAllAddresses();
    ApiResponse updateAddress(UUID addressId, AddressRequestDTO addressRequest);


}
