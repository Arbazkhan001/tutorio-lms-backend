package com.affycloud.TutorioBackend.dtos.course.courseVariant;

import lombok.Data;

import java.util.List;

@Data
public class CategoryVariantDto {
    private String category;
    private List<CollectionVariantDto> collections;

    public CategoryVariantDto(String category, List<CollectionVariantDto> collections) {
        this.category = category;
        this.collections = collections;
    }
}
