package com.affycloud.TutorioBackend.services;



import com.affycloud.TutorioBackend.dtos.variants.VariantNameDetailDto;
import com.affycloud.TutorioBackend.dtos.variants.VariantNameDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface VariantNameService {
    Map<String, Object> createVariantName(VariantNameDto variantNameDto);
    List<VariantNameDetailDto> getAllVariantNames();
//    VariantNameDetailDto getVariantNameByType(String variantType);
    VariantNameDetailDto getVariantNameByType(String variantType, UUID tenantId);
//    Map<String, Object> addVariantNamesToExistingType(String variantType, List<String> newVariantNames);
    Map<String, Object> addVariantNamesToExistingType(String variantType, List<String> variantNamesToAdd, UUID tenantId);
    Map<String, Object> removeVariantNamesFromExistingType(String variantType, List<String> variantNamesToRemove,UUID tenantId);
}
