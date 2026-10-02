package com.affycloud.TutorioBackend.controllers;

import com.affycloud.TutorioBackend.enums.Role;
import com.affycloud.TutorioBackend.handler.InvalidOperationException;
import com.affycloud.TutorioBackend.models.User;
import com.affycloud.TutorioBackend.services.DashboardService;
import com.affycloud.TutorioBackend.utils.AuthUtilAdmin;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private AuthUtilAdmin authUtil;

    @GetMapping("/dashboard/count")
    public ResponseEntity<?> getTotalProductVariantsCount(
            HttpServletRequest httpRequest

    ) {
        User user = authUtil.getAuthenticatedUser(httpRequest);
        UUID tenantId = resolveTenantId(user);

        Map<String, Object> totalCounts =
                dashboardService.getTotalCountForDashboard(tenantId);

        return ResponseEntity.ok(Map.of(
                "message", "Total counts fetched successfully",
                "error", false,
                "meta", totalCounts
        ));
    }


    @GetMapping("/graph/count")
    public ResponseEntity<?> getTotalCountForGraph(
            HttpServletRequest httpRequest,
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        // Accepts either an explicit [startDate, endDate] window OR a day count
        // (today-anchored). The dashboard requests 2× the selected window for
        // period-over-period comparison, so the span ceiling here is 730.
        LocalDate[] window = resolveWindow(days, startDate, endDate, 3, 730);

        User user = authUtil.getAuthenticatedUser(httpRequest);
        UUID tenantId = resolveTenantId(user);
        Map<String, Object> totalCounts =
                dashboardService.getTotalCountForGraph(tenantId, window[0], window[1]);

        return ResponseEntity.ok(Map.of(
                "message", "Total counts fetched successfully",
                "error", false,
                "meta", totalCounts
        ));
    }


    @GetMapping("/product-mix")
    public ResponseEntity<?> getProductMix(
            HttpServletRequest httpRequest,
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LocalDate[] window = resolveWindow(days, startDate, endDate, 30, 365);

        User user = authUtil.getAuthenticatedUser(httpRequest);
        UUID tenantId = resolveTenantId(user);

        Map<String, Object> meta =
                dashboardService.getProductMixAndTopSellers(tenantId, window[0], window[1]);

        return ResponseEntity.ok(Map.of(
                "message", "Product mix fetched successfully",
                "error", false,
                "meta", meta
        ));
    }


    @GetMapping("/lms-sales")
    public ResponseEntity<?> getAllLmsSalesData(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String dateRange,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        if (page <= 0) {
            throw new InvalidOperationException("Page number must be greater than 0");
        }

        if (size <= 0 || size > 100) {
            throw new InvalidOperationException("Page size must be between 1 and 100");
        }

        // Resolve the optional window: explicit dates, or a day count (dateRange),
        // or neither — in which case all paid orders are returned.
        LocalDate start = null;
        LocalDate end = null;
        boolean hasDates = startDate != null && !startDate.isBlank() && endDate != null && !endDate.isBlank();
        if (hasDates || (dateRange != null && !dateRange.isBlank())) {
            Integer days = null;
            if (dateRange != null && !dateRange.isBlank()) {
                try {
                    days = Integer.parseInt(dateRange.trim());
                } catch (NumberFormatException e) {
                    throw new InvalidOperationException("dateRange must be a valid number of days");
                }
            }
            LocalDate[] window = resolveWindow(days, startDate, endDate, 30, 365);
            start = window[0];
            end = window[1];
        }

        User user = authUtil.getAuthenticatedUser(request);
        UUID tenantId = resolveTenantId(user); // Not used currently but can be used for tenant-specific data

        Map<String, Object> response =
                dashboardService.getAllLmsSalesData(
                         page - 1, size, start, end);

        return ResponseEntity.ok(Map.of(
                "message", "orders fetched successfully",
                "error", false,
                "meta", response
        ));
    }



    /**
     * Resolves an analytics window to a concrete [start, end] date pair.
     * <ul>
     *   <li>If {@code startDate} and {@code endDate} are both supplied, that exact
     *       (inclusive) window is used.</li>
     *   <li>Otherwise the window is the last {@code days} days ending today
     *       (falling back to {@code defaultDays} when {@code days} is null).</li>
     * </ul>
     * Span is capped at {@code maxSpanDays}; the end date may not be in the future.
     */
    private LocalDate[] resolveWindow(Integer days, String startDate, String endDate,
                                      int defaultDays, int maxSpanDays) {
        boolean hasDates = startDate != null && !startDate.isBlank()
                && endDate != null && !endDate.isBlank();

        if (hasDates) {
            LocalDate start;
            LocalDate end;
            try {
                start = LocalDate.parse(startDate.trim());
                end = LocalDate.parse(endDate.trim());
            } catch (DateTimeParseException e) {
                throw new InvalidOperationException("Dates must be in YYYY-MM-DD format");
            }
            if (start.isAfter(end)) {
                throw new InvalidOperationException("startDate must be on or before endDate");
            }
            if (end.isAfter(LocalDate.now())) {
                throw new InvalidOperationException("endDate cannot be in the future");
            }
            long span = ChronoUnit.DAYS.between(start, end) + 1;
            if (span > maxSpanDays) {
                throw new InvalidOperationException("Date range cannot exceed " + maxSpanDays + " days");
            }
            return new LocalDate[]{start, end};
        }

        int n = days != null ? days : defaultDays;
        if (n <= 0 || n > maxSpanDays) {
            throw new InvalidOperationException("Days must be between 1 and " + maxSpanDays);
        }
        LocalDate today = LocalDate.now();
        return new LocalDate[]{today.minusDays(n - 1L), today};
    }

    private UUID resolveTenantId(User user) {
        if (user.getRole() == Role.ADMIN) {
            return user.getId();
        }

        if (user.getRole() == Role.TEACHER) {
            if (user.getParent() == null) {
                throw new InvalidOperationException("Teacher is not linked to any tenant");
            }
            return user.getParent().getId();
        }

        throw new InvalidOperationException("You are not authorized to access dashboard");
    }
}
