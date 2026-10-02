package com.affycloud.TutorioBackend.services;

import com.affycloud.TutorioBackend.dtos.feature.FeatureDetailDto;
import com.affycloud.TutorioBackend.dtos.lmsPackage.BuyPackageRequestDto;
import com.affycloud.TutorioBackend.dtos.lmsPackage.PackageAddDto;
import com.affycloud.TutorioBackend.dtos.lmsPackage.PackageDetailDto;
import com.affycloud.TutorioBackend.dtos.lmsPackage.PackageUpdateDto;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PackageService {
    Map<String, Object> createPackage(PackageAddDto request);
    Map<String, Object> updatePackage(UUID id, PackageUpdateDto request);
    PackageDetailDto getPackageById(UUID id);
    List<PackageDetailDto> getAllPackages();
    ApiResponse buyPackage(UUID userId, BuyPackageRequestDto request);
    ApiResponseWithMeta getMyPackage(UUID userId);
    ApiResponseWithMeta getMyPackageWithBilling(UUID userId);
    List<FeatureDetailDto> getAllFeaturesWithPackage(UUID packageId);
    String toggleFeatureInPackage(UUID packageId, UUID featureId);

}