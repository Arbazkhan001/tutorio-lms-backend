package com.affycloud.TutorioBackend.dtos.variants;


import lombok.Data;

import java.util.List;

@Data
public class VariantNameDto {

    private String variantType; // Example: M, L, 22, 24, etc.
    private List<String> variantNames;

}
