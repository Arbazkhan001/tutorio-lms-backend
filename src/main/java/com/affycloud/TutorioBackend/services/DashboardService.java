package com.affycloud.TutorioBackend.services;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public interface DashboardService {

    Map<String, Object> getTotalCountForDashboard(UUID tenantId);
    Map<String, Object> getTotalCountForGraph(UUID tenantId, LocalDate startDate, LocalDate endDate);
    Map<String, Object> getAllLmsSalesData(int page, int size, LocalDate startDate, LocalDate endDate);
    Map<String, Object> getProductMixAndTopSellers(UUID tenantId, LocalDate startDate, LocalDate endDate);
}
