package com.affycloud.TutorioBackend.services.implementation;

import com.affycloud.TutorioBackend.dtos.address.AddressRequestDTO;
import com.affycloud.TutorioBackend.dtos.address.AddressResponseDTO;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;
import com.affycloud.TutorioBackend.models.Address;
import com.affycloud.TutorioBackend.models.Student;
import com.affycloud.TutorioBackend.repositories.AddressRepository;
import com.affycloud.TutorioBackend.repositories.StudentRepository;
import com.affycloud.TutorioBackend.services.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AddressServiceImplementation implements AddressService {

      @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private StudentRepository customerRepository;

    public AddressResponseDTO addAddress(UUID studentId, AddressRequestDTO addressRequest) {
        Student student = customerRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Address address = new Address();
        address.setStreet(addressRequest.getStreet());
        address.setCity(addressRequest.getCity());
        address.setState(addressRequest.getState());
        address.setPostalCode(addressRequest.getPostalCode());
        address.setCountry(addressRequest.getCountry());
        address.setDefault(addressRequest.isDefault());
        address.setStudent(student);

        Address savedAddress = addressRepository.save(address);
        return toAddressResponseDTO(savedAddress);
    }


    private AddressResponseDTO toAddressResponseDTO(Address address) {
        AddressResponseDTO dto = new AddressResponseDTO();
        dto.setId(address.getId());
        dto.setStreet(address.getStreet());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setPostalCode(address.getPostalCode());
        dto.setCountry(address.getCountry());
        dto.setDefault(address.isDefault());
        return dto;
    }


    @Override
    public ApiResponseWithMeta getAddresses(UUID studentId) {
        List<Address> addresses = addressRepository.findByStudentId(studentId)
                .stream()
                .filter(address -> !address.isDeleted()) 
                .collect(Collectors.toList());
    
        List<AddressResponseDTO> addressResponseList = addresses.stream()
                .map(this::toAddressResponseDTO)
                .collect(Collectors.toList());
    
        return new ApiResponseWithMeta(
                "Addresses fetched successfully",
                false,
                Map.of("addresses", addressResponseList)
        );
    }
    

    @Override
    public boolean softDeleteAddress(UUID addressId) {
        Optional<Address> addressOptional = addressRepository.findById(addressId);
        if (addressOptional.isPresent()) {
            Address address = addressOptional.get();
            address.setDeleted(true); // Set isDeleted = true
            addressRepository.save(address);
            return true;
        }
        return false; // Address not found
    }

    @Override
public ApiResponseWithMeta getAllAddresses() {
    List<Address> addresses = addressRepository.findByIsDeletedFalse(); 

    List<AddressResponseDTO> addressResponseList = addresses.stream()
            .map(this::toAddressResponseDTO)
            .collect(Collectors.toList());

    return new ApiResponseWithMeta(
            "All addresses fetched successfully",
            false,
            Map.of("addresses", addressResponseList)
    );
}


@Override
public ApiResponse updateAddress(UUID addressId, AddressRequestDTO addressRequest) {
    Optional<Address> optionalAddress = addressRepository.findById(addressId);

    if (optionalAddress.isEmpty()) {
        return new ApiResponse("Address not found", true);
    }

    Address address = optionalAddress.get();

    // Check if the address is soft deleted
    if (address.isDeleted()) {
        return new ApiResponse("Cannot update a deleted address", true);
    }

    // Update fields
    address.setStreet(addressRequest.getStreet());
    address.setCity(addressRequest.getCity());
    address.setState(addressRequest.getState());
    address.setPostalCode(addressRequest.getPostalCode());
    address.setCountry(addressRequest.getCountry());
    address.setDefault(addressRequest.isDefault());

    // Save the updated address
    addressRepository.save(address);

    return new ApiResponse("Address updated successfully", false);
}

}
