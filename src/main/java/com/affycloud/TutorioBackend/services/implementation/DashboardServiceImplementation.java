package com.affycloud.TutorioBackend.services.implementation;

import com.affycloud.TutorioBackend.handler.InvalidOperationException;
import com.affycloud.TutorioBackend.models.Order;
import com.affycloud.TutorioBackend.models.OrderItem;
import com.affycloud.TutorioBackend.models.Student;
import com.affycloud.TutorioBackend.repositories.*;
import com.affycloud.TutorioBackend.services.DashboardService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImplementation implements DashboardService {

        @Autowired
        private CourseRepository courseRepository;

        @Autowired
        private CourseBundleRepository courseBundleRepository;

        @Autowired
        private EbookRepository ebookRepository;

        @Autowired
        private TestSeriesRepository testSeriesRepository;

        @Autowired
        private OrderRepository orderRepository;

        @Autowired
        private StudentRepository studentRepository;

        @Autowired
        private EnrollmentRepository enrollmentRepository;

        @Autowired
        private OrderItemRepository orderItemRepository;

        @Override
        public Map<String, Object> getTotalCountForDashboard(UUID tenantId) {

                Map<String, Object> response = new HashMap<>();

                long totalCourses = courseRepository.countByTenantId(tenantId);

                long totalBundles = courseBundleRepository.countByTenantId(tenantId);

                long totalEbooks = ebookRepository.countByTenantIdAndCourseTypeIdIsNull(tenantId);

                long totalTestSeries = testSeriesRepository.countByTenantId(tenantId);

                long totalStudents = studentRepository.countByTenantId(tenantId);

                // 🔥 FIXED
                Long totalOrders = orderRepository.countPaidOrdersByTenant(tenantId);

                Double totalSales = orderRepository.getTotalSalesByTenant(tenantId);

                // 🔥 TODAY calculation
                LocalDate today = LocalDate.now();

                Date startOfDay = Date.from(
                                today.atStartOfDay(ZoneId.systemDefault()).toInstant());

                Date endOfDay = Date.from(
                                today.atTime(23, 59, 59)
                                                .atZone(ZoneId.systemDefault())
                                                .toInstant());

                Long todayOrders = orderRepository.countPaidOrdersByTenantAndDateRange(
                                tenantId,
                                startOfDay,
                                endOfDay);

                Double todaySales = orderRepository.getSalesByTenantAndDateRange(
                                tenantId,
                                startOfDay,
                                endOfDay);

                response.put("totalCourseCount", totalCourses);
                response.put("totalCourseBundleCount", totalBundles);
                response.put("totalEbookCount", totalEbooks);
                response.put("totalTestSeriesCount", totalTestSeries);
                response.put("totalStudentCount", totalStudents);

                response.put("totalOrders", totalOrders != null ? totalOrders : 0);
                response.put("totalSales", totalSales != null ? totalSales : 0);

                response.put("todayTotalOrders", todayOrders != null ? todayOrders : 0);
                response.put("todayTotalSales", todaySales != null ? todaySales : 0);

                return response;
        }

        @Override
        public Map<String, Object> getTotalCountForGraph(UUID tenantId, LocalDate startDate, LocalDate endDate) {

                if (tenantId == null) {
                        throw new InvalidOperationException("Tenant not resolved");
                }

                List<Map<String, Object>> lastNDaysData = new ArrayList<>();

                // Walk the window newest → oldest so the most recent day is first.
                for (LocalDate targetDate = endDate;
                     !targetDate.isBefore(startDate);
                     targetDate = targetDate.minusDays(1)) {

                        // Convert LocalDate → Date (start of day)
                        Date startOfDay = Date.from(
                                        targetDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

                        Date endOfDay = Date.from(
                                        targetDate.atTime(23, 59, 59)
                                                        .atZone(ZoneId.systemDefault())
                                                        .toInstant());

                        // 🔥 Orders Count (Tenant + Paid + Date)
                        Long totalOrders = orderRepository.countPaidOrdersByTenantAndDateRange(
                                        tenantId,
                                        startOfDay,
                                        endOfDay);

                        // 🔥 Sales Sum
                        Double totalSales = orderRepository.getSalesByTenantAndDateRange(
                                        tenantId,
                                        startOfDay,
                                        endOfDay);

                        // 🔥 Students Joined (new registrations)
                        long studentsJoined = studentRepository.countByTenantIdAndCreatedAtBetween(
                                        tenantId,
                                        startOfDay,
                                        endOfDay);

                        // 🔥 Students Enrolled (new enrollments in any course/ebook/test-series)
                        long studentsEnrolled = enrollmentRepository.countByStudentTenantIdAndEnrolledAtBetween(
                                        tenantId,
                                        startOfDay,
                                        endOfDay);

                        Map<String, Object> dayData = new HashMap<>();
                        dayData.put("date", targetDate);
                        dayData.put("totalOrders", totalOrders != null ? totalOrders : 0);
                        dayData.put("totalSales", totalSales != null ? totalSales : 0);
                        dayData.put("studentsJoined", studentsJoined);
                        dayData.put("studentsEnrolled", studentsEnrolled);

                        lastNDaysData.add(dayData);
                }

                return Map.of("lastNDaysData", lastNDaysData);
        }

        @Override
        public Map<String, Object> getAllLmsSalesData(int page, int size, LocalDate startDate, LocalDate endDate) {
                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
                Page<Order> orderPage;
                Double overallTotalAmount = null;
                Long totalOrdersInDateRange;

                if (startDate != null && endDate != null) {
                        LocalDateTime start = startDate.atStartOfDay();
                        LocalDateTime end = endDate.atTime(23, 59, 59);

                        orderPage = orderRepository.findPaidOrdersInDateRange(start, end, pageable);
                        overallTotalAmount = orderRepository.getTotalAmountByDateRangeAndPaid(start, end);
                        totalOrdersInDateRange = orderRepository.countPaidOrdersInDateRange(start, end);
                } else {
                        orderPage = orderRepository.findByPaymentStatus("PAID", pageable);
                        totalOrdersInDateRange = orderRepository.countByPaymentStatus("PAID");
                }

                List<Map<String, Object>> orderData = new ArrayList<>();
                double grandTotal = 0.0;

                for (Order order : orderPage.getContent()) {
                        Map<String, Object> orderMap = new HashMap<>();
                        orderMap.put("orderId", order.getId());
                        orderMap.put("studentName", order.getStudent().getName());
                        orderMap.put("randomId", order.getRandomId());
                        orderMap.put("status", order.getStatus());
                        orderMap.put("paymentStatus", order.getPaymentStatus());
                        orderMap.put("totalAmount", order.getTotalAmount());
                        orderMap.put("saleDate", order.getCreatedAt());

                        List<Map<String, Object>> items = new ArrayList<>();
                        for (OrderItem item : order.getOrderItems()) {
                                Map<String, Object> itemMap = new HashMap<>();
                                itemMap.put("title", item.getTitle());
                                itemMap.put("itemType", item.getItemType());
                                itemMap.put("courseType", item.getCourseType());
                                itemMap.put("courseAttempt", item.getCourseAttempt());
                                itemMap.put("price", item.getPrice());
                                itemMap.put("discount", item.getDiscount());
                                itemMap.put("discountType", item.getDiscountType());
                                itemMap.put("finalPrice", item.getFinalPrice());
                                itemMap.put("quantity", item.getQuantity());
                                itemMap.put("totalPrice", item.getTotalPrice());
                                itemMap.put("saleDate", item.getCreatedAt());
                                items.add(itemMap);
                        }

                        orderMap.put("items", items);
                        orderData.add(orderMap);

                        grandTotal += order.getTotalAmount();
                }

                Map<String, Object> response = new HashMap<>();
                response.put("orders", orderData);
                response.put("grandTotal", grandTotal);
                response.put("currentPage", orderPage.getNumber() + 1);
                response.put("totalPages", orderPage.getTotalPages());
                response.put("totalOrdersInPage", orderPage.getNumberOfElements());
                response.put("totalOrdersInDateRange", totalOrdersInDateRange);

                if (overallTotalAmount != null) {
                        response.put("totalSalesInDateRange", overallTotalAmount);
                }

                return response;
        }

        @Override
        public Map<String, Object> getProductMixAndTopSellers(UUID tenantId, LocalDate startDate, LocalDate endDate) {
                Date start = Date.from(
                                startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                Date end = Date.from(
                                endDate.atTime(23, 59, 59)
                                                .atZone(ZoneId.systemDefault())
                                                .toInstant());

                // Revenue split by product type (COURSE / EBOOK / TEST_SERIES …)
                List<Map<String, Object>> productMix = new ArrayList<>();
                double totalRevenue = 0;
                for (Object[] r : orderItemRepository.revenueByItemType(tenantId, start, end)) {
                        double revenue = r[1] instanceof Number ? ((Number) r[1]).doubleValue() : 0;
                        long count = r[2] instanceof Number ? ((Number) r[2]).longValue() : 0;

                        Map<String, Object> entry = new LinkedHashMap<>();
                        entry.put("itemType", r[0] != null ? r[0].toString() : "UNKNOWN");
                        entry.put("revenue", revenue);
                        entry.put("count", count);
                        productMix.add(entry);
                        totalRevenue += revenue;
                }

                // Top 5 products by revenue
                List<Map<String, Object>> topProducts = new ArrayList<>();
                for (Object[] r : orderItemRepository.topSellingProducts(
                                tenantId, start, end, PageRequest.of(0, 5))) {
                        Map<String, Object> entry = new LinkedHashMap<>();
                        entry.put("itemId", r[0]);
                        entry.put("title", r[1]);
                        entry.put("itemType", r[2] != null ? r[2].toString() : "UNKNOWN");
                        entry.put("revenue", r[3] instanceof Number ? ((Number) r[3]).doubleValue() : 0);
                        entry.put("unitsSold", r[4] instanceof Number ? ((Number) r[4]).longValue() : 0);
                        topProducts.add(entry);
                }

                Map<String, Object> meta = new LinkedHashMap<>();
                meta.put("productMix", productMix);
                meta.put("topProducts", topProducts);
                meta.put("totalRevenue", totalRevenue);
                meta.put("startDate", startDate.toString());
                meta.put("endDate", endDate.toString());
                return meta;
        }

}

/*
 * @Override
 * public Map<String, Object> getTotalCountForDashboard(UUID tenantId) {
 * 
 * Map<String, Object> response = new HashMap<>();
 * 
 * long totalCourses =
 * courseRepository.countByTenantId(tenantId);
 * 
 * long totalBundles =
 * courseBundleRepository.countByTenantId(tenantId);
 * 
 * long totalEbooks =
 * ebookRepository.countByTenantId(tenantId);
 * 
 * long totalTestSeries =
 * testSeriesRepository.countByTenantId(tenantId);
 * 
 * long totalStudents =
 * studentRepository.countByTenantId(tenantId);
 * 
 * long totalOrders =
 * orderRepository.countByTenantIdAndPaymentStatus(
 * tenantId, "PAID"
 * );
 * 
 * Double totalSales =
 * orderRepository.getTotalSalesByTenant(tenantId);
 * 
 * response.put("totalCourseCount", totalCourses);
 * response.put("totalCourseBundleCount", totalBundles);
 * response.put("totalEbookCount", totalEbooks);
 * response.put("totalTestSeriesCount", totalTestSeries);
 * response.put("totalStudentCount", totalStudents);
 * response.put("totalOrders", totalOrders);
 * response.put("totalSales", totalSales);
 * 
 * return response;
 * }
 */

/*
 * @Override
 * public Map<String, Object> getTotalCountForGraph(UUID tenantId, int days) {
 * 
 * LocalDate today = LocalDate.now();
 * List<Map<String, Object>> lastNDaysData = new ArrayList<>();
 * 
 * for (int i = 0; i < days; i++) {
 * LocalDate targetDate = today.minusDays(i);
 * 
 * LocalDateTime startOfDay = targetDate.atStartOfDay();
 * LocalDateTime endOfDay = targetDate.atTime(23, 59, 59);
 * 
 * // long totalOrders =
 * // orderRepository.countPaidOrdersByTenantAndDate(
 * // tenantId, startOfDay, endOfDay);
 * //
 * // Double totalSales =
 * // orderRepository.sumPaidSalesByTenantAndDate(
 * // tenantId, startOfDay, endOfDay);
 * 
 * long studentsJoined =
 * studentRepository.countByTenantIdAndCreatedAtBetween(
 * tenantId, startOfDay, endOfDay);
 * 
 * Map<String, Object> dayData = new HashMap<>();
 * dayData.put("date", targetDate);
 * dayData.put("totalOrders", 0);
 * dayData.put("totalSales", 0);
 * dayData.put("studentsJoined", studentsJoined);
 * dayData.put("studentsEnrolled", studentsJoined);
 * 
 * lastNDaysData.add(dayData);
 * }
 * 
 * return Map.of("lastNDaysData", lastNDaysData);
 * }
 */