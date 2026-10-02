package com.affycloud.TutorioBackend.controllers;

import com.affycloud.TutorioBackend.dtos.address.AddressRequestDTO;
import com.affycloud.TutorioBackend.dtos.address.AddressResponseDTO;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;
import com.affycloud.TutorioBackend.services.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Operation(summary = "Create an address with studentId")
    @PostMapping("/{studentId}")
    public ApiResponse addAddress(@PathVariable UUID studentId, @RequestBody AddressRequestDTO addressRequest) {
        try {
            if (addressRequest.getStreet() == null || addressRequest.getStreet().trim().isEmpty()) {
                return new ApiResponse("Street is required", true);
            }
            if (addressRequest.getCity() == null || addressRequest.getCity().trim().isEmpty()) {
                return new ApiResponse("City is required", true);
            }
            if (addressRequest.getState() == null || addressRequest.getState().trim().isEmpty()) {
                return new ApiResponse("State is required", true);
            }
            if (addressRequest.getPostalCode() == null || addressRequest.getPostalCode().trim().isEmpty()) {
                return new ApiResponse("Postal Code is required", true);
            }
            if (addressRequest.getCountry() == null || addressRequest.getCountry().trim().isEmpty()) {
                return new ApiResponse("Country is required", true);
            }

            AddressResponseDTO response = addressService.addAddress(studentId, addressRequest);
            return new ApiResponse("Address added successfully", false);

        } catch (Exception e) {
            return new ApiResponse("Failed to add address: " + e.getMessage(), true);
        }
    }

    @Operation(summary = "Get an address with studentId")
    @GetMapping("/{studentId}")
    public ApiResponseWithMeta getAddresses(@PathVariable UUID studentId) {
    return addressService.getAddresses(studentId);
    }

    @Operation(summary = "Delete an address with addressId (soft delete)")
    @PutMapping("/soft-delete/{addressId}")
    public ApiResponse softDeleteAddress(@PathVariable UUID addressId) {
        try {
            boolean isDeleted = addressService.softDeleteAddress(addressId);
            if (isDeleted) {
                return new ApiResponse("Address deleted successfully", false);
            } else {
                return new ApiResponse("Address not found", true);
            }
        } catch (Exception e) {
            return new ApiResponse("Failed to delete address: " + e.getMessage(), true);
        }
    }

    @Operation(summary = "Get All address ")
    @GetMapping("/all")
    public ApiResponseWithMeta getAllAddresses() {
    return addressService.getAllAddresses();
}

    @Operation(summary = "Update an address with addressId")
    @PutMapping("/{addressId}")
    public ApiResponse updateAddress(@PathVariable UUID addressId, @Valid @RequestBody AddressRequestDTO addressRequest) {
        return addressService.updateAddress(addressId, addressRequest);
    }

}
