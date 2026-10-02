package com.affycloud.TutorioBackend.services;

import com.affycloud.TutorioBackend.dtos.feature.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface FeatureService {
    Map<String, Object> createFeatures(FeatureAddDto featureAddDto);
    Map<String, Object> updateFeature(UUID id, FeatureUpdateDto featureUpdateDto);
    List<FeatureDetailDto> getAllFeatures();
    FeatureDetailDto getFeatureById(UUID id);
}