package com.affycloud.TutorioBackend.services.implementation;


import com.affycloud.TutorioBackend.dtos.variants.VariantNameDetailDto;
import com.affycloud.TutorioBackend.dtos.variants.VariantNameDto;
import com.affycloud.TutorioBackend.models.VariantName;
import com.affycloud.TutorioBackend.models.VariantValue;
import com.affycloud.TutorioBackend.repositories.VariantNameRepository;
import com.affycloud.TutorioBackend.repositories.VariantValueRepository;
import com.affycloud.TutorioBackend.services.VariantNameService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class VariantNameServiceImplementation implements VariantNameService {


    @Autowired
    private VariantNameRepository variantNameRepository;

    @Autowired
    private VariantValueRepository variantValueRepository;

    @Autowired
    private ModelMapper modelMapper;


    @Override
    public Map<String, Object> createVariantName(VariantNameDto variantNameDto) {
        Map<String, Object> response = new HashMap<>();

        /* Check if the email is already in use */
        if (variantNameRepository.findByVariantType(variantNameDto.getVariantType()).isPresent()) {
            response.put("response", "This Variant-Tpe already exists");
            response.put("error", true);
            return response;
        }

        VariantName variantName = modelMapper.map(variantNameDto, VariantName.class);
        variantName = variantNameRepository.save(variantName);
        response.put("message", "VariantName created successfully.");
        response.put("error", false);
        return response;

    }


    public List<VariantNameDetailDto> getAllVariantNames() {
        List<VariantName> variantNames = variantNameRepository.findAll();
        if (variantNames.isEmpty()) {
            /*throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No variant-Names found");*/
            return Collections.emptyList();
        }
        return variantNames.stream()
                .map(variantName -> modelMapper.map(variantName, VariantNameDetailDto.class))
                .collect(Collectors.toList());
    }

    // New method to fetch VariantName by variantType

    @Override
    public VariantNameDetailDto getVariantNameByType(String variantType, UUID tenantId) {
        VariantName variantName = variantNameRepository.findByVariantType(variantType)
                .orElseThrow(() -> new RuntimeException("VariantName with type " + variantType + " not found"));

        // Tenant-specific values fetch karna
        List<VariantValue> values = variantValueRepository.findByVariantName_VariantTypeAndTenantId(variantType, tenantId);

        // DTO me map karna
        VariantNameDetailDto dto = new VariantNameDetailDto();
        dto.setVariantType(variantName.getVariantType());
        dto.setVariantNames(values.stream().map(VariantValue::getName).toList());

        return dto;
    }

//    public VariantNameDetailDto getVariantNameByType(String variantType) {
//        VariantName variantName = variantNameRepository.findByVariantType(variantType)
//                .orElseThrow(() -> new RuntimeException("VariantNames with type " + variantType + " not found"));
//
//        return modelMapper.map(variantName, VariantNameDetailDto.class);
//    }


    // Add variant names from an existing VariantName

    @Override
    public Map<String, Object> addVariantNamesToExistingType(String variantType, List<String> variantNamesToAdd, UUID tenantId) {
        Map<String, Object> response = new HashMap<>();

        VariantName variantName = variantNameRepository.findByVariantType(variantType)
                .orElseThrow(() -> new RuntimeException("VariantType not found"));

        List<String> duplicates = variantNamesToAdd.stream()
                .filter(name -> variantValueRepository.existsByVariantName_VariantTypeAndNameAndTenantId(variantType, name, tenantId))
                .collect(Collectors.toList());

        if (!duplicates.isEmpty()) {
            response.put("message", "Some variant names already exist for this tenant: " + String.join(", ", duplicates));
            response.put("error", true);
            return response;
        }

        List<VariantValue> newValues = variantNamesToAdd.stream()
                .map(name -> {
                    VariantValue v = new VariantValue();
                    v.setName(name);
                    v.setTenantId(tenantId);
                    v.setVariantName(variantName);
                    return v;
                }).toList();

        variantValueRepository.saveAll(newValues);

        response.put("message", "Variant names added successfully for tenant.");
        response.put("error", false);
        return response;
    }

    // Remove variant names from an existing VariantName by variantType
    @Override
    public Map<String, Object> removeVariantNamesFromExistingType(String variantType, List<String> variantNamesToRemove, UUID tenantId) {
        Map<String, Object> response = new HashMap<>();

        // Fetch the VariantName entity by variantType
        VariantName variantName = variantNameRepository.findByVariantType(variantType)
                .orElseThrow(() -> new RuntimeException("VariantName with type " + variantType + " not found"));

        // Fetch tenant-specific variant values
        List<VariantValue> tenantValues = variantValueRepository.findByVariantName_VariantTypeAndTenantId(variantType, tenantId);

        // Extract existing names
        List<String> currentVariantNames = tenantValues.stream().map(VariantValue::getName).toList();

        // Check for non-existent names
        List<String> nonExistentNames = variantNamesToRemove.stream()
                .filter(name -> !currentVariantNames.contains(name))
                .toList();

        if (!nonExistentNames.isEmpty()) {
            response.put("message", "Variant names do not exist for this tenant: " + String.join(", ", nonExistentNames));
            response.put("error", true);
            return response;
        }

        // Remove the variant names that exist for this tenant
        List<VariantValue> valuesToDelete = tenantValues.stream()
                .filter(v -> variantNamesToRemove.contains(v.getName()))
                .toList();

        variantValueRepository.deleteAll(valuesToDelete);

        response.put("message", "Variant names removed successfully for tenant.");
        response.put("error", false);
        return response;
    }

}

//    @Override
//    public Map<String, Object> addVariantNamesToExistingType(String variantType, List<String> variantNamesToAdd) {
//        Map<String, Object> response = new HashMap<>();
//
//        // Fetch the VariantName entity by variantType
//        VariantName existingVariantName = variantNameRepository.findByVariantType(variantType)
//                .orElseThrow(() -> new RuntimeException("VariantType not found"));
//
//        // Check for duplicate variant names (if they already exist in the database)
//        List<String> existingVariantNames = existingVariantName.getVariantNames();
//        List<String> duplicates = variantNamesToAdd.stream()
//                .filter(existingVariantNames::contains)
//                .collect(Collectors.toList());
//
//        if (!duplicates.isEmpty()) {
//            // Format the duplicates as 'name1', 'name2'
//            String formattedDuplicates = "'" + String.join("', '", duplicates) + "'";
//
//            response.put("message", "Some variant names already exist: " + formattedDuplicates);
//            response.put("error", true);
//            return response;
//        }
//
//        // Add the new variant names to the existing list
//        existingVariantNames.addAll(variantNamesToAdd);
//        existingVariantName.setVariantNames(existingVariantNames);
//        variantNameRepository.save(existingVariantName);
//
//        response.put("message", "Variant names added successfully.");
//        response.put("error", false);
//        return response;
//    }