package com.affycloud.TutorioBackend.dtos.variants;

import lombok.Data;

import java.util.List;


@Data
public class VariantNameDetailDto {

    private String variantType;
    private List<String> variantNames;

}
