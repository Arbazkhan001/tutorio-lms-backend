package com.affycloud.TutorioBackend.dtos.reponse;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaginatedResponse<T> {
    private int totalPages;
    private long totalItems;
    private int size;
    private int currentPage;
    private List<T> meta;
}
