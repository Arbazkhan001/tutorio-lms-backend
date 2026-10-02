package com.affycloud.TutorioBackend.dtos.course.courseVariant;

import lombok.Data;

import java.util.List;

@Data
public class CollectionVariantDto {
    private String collection;
    private List<String> subjects;

    public CollectionVariantDto(String collection, List<String> subjects) {
        this.collection = collection;
        this.subjects = subjects;
    }
}
