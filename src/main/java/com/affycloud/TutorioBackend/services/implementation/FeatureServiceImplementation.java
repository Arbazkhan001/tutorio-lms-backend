package com.affycloud.TutorioBackend.services.implementation;

import com.affycloud.TutorioBackend.dtos.feature.FeatureAddDto;
import com.affycloud.TutorioBackend.dtos.feature.FeatureDetailDto;
import com.affycloud.TutorioBackend.dtos.feature.FeatureUpdateDto;
import com.affycloud.TutorioBackend.models.Feature;
import com.affycloud.TutorioBackend.repositories.FeatureRepository;
import com.affycloud.TutorioBackend.services.FeatureService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.text.CaseUtils.toCamelCase;

@Service
public class FeatureServiceImplementation implements FeatureService {

    @Autowired
    private FeatureRepository repository;

    @Autowired
    private ModelMapper modelMapper;


    @Override
    public Map<String, Object> createFeatures(FeatureAddDto request)  {

        /* Map storing response. */
        Map<String, Object> response = new HashMap<>();
        if (repository.existsByNameIgnoreCase(request.getName())) {
            response.put("message", "Feature already exists.");
            response.put("error", true);
            return response;
        }

        // Generate featureKey (camelCase, no spaces)
        String featureKey = toCamelCase(request.getName());

        if (repository.existsByFeatureKeyIgnoreCase(featureKey)) {
            response.put("message", "Feature key already exists.");
            response.put("error", true);
            return response;
        }
        Feature feature = new Feature();
        feature.setName(request.getName());
        feature.setDescription(request.getDescription());
        feature.setFeatureKey(featureKey);
        repository.save(feature);

        response.put("message", "Feature created successfully.");
        response.put("error", false);
        return response;
    }

    @Override
    public Map<String, Object> updateFeature(UUID id, FeatureUpdateDto request) {
        Map<String, Object> response = new HashMap<>();


        Feature feature = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feature not found." ));

        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            feature.setDescription(request.getDescription());
        }

        try {
            repository.save(feature);
            response.put("message", "Feature updated successfully.");
            response.put("error", false);
            return response;
        } catch (Exception e) {
            response.put("message", "Failed to update Feature.");
            response.put("error", true);
            return response;
        }
    }

    private String toCamelCase(String input) {
        if (input == null || input.isEmpty()) return input;

        String[] parts = input.trim().toLowerCase().split("\\s+");
        StringBuilder camelCaseString = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            camelCaseString.append(parts[i].substring(0, 1).toUpperCase())
                    .append(parts[i].substring(1));
        }
        return camelCaseString.toString();
    }

    @Override
    public FeatureDetailDto getFeatureById(UUID id) {
        Feature feature = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feature not found." ));

        return modelMapper.map(feature, FeatureDetailDto.class);
    }

    @Override
    public List<FeatureDetailDto> getAllFeatures() {
        List<Feature> features = repository.findAll();
        if (features.isEmpty()) {
            return Collections.emptyList();
        }
        return features.stream()
                .map(feature -> {
                    return modelMapper.map(feature, FeatureDetailDto.class);
                })
                .collect(Collectors.toList());
    }

}


