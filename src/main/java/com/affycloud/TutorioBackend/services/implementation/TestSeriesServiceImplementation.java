//package com.affycloud.TutorioBackend.services.implementation;
//
//import com.affycloud.TutorioBackend.dtos.exam.ExamDTO;
//import com.affycloud.TutorioBackend.dtos.paymentType.PaymentType;
//import com.affycloud.TutorioBackend.dtos.question.QuestionResponseDto;
//import com.affycloud.TutorioBackend.dtos.test.TestResponseDto;
//import com.affycloud.TutorioBackend.dtos.testSeries.*;
//import com.affycloud.TutorioBackend.dtos.testSeries.directory.DirectoryDTO;
//import com.affycloud.TutorioBackend.dtos.testSeries.directory.DirectoryResponseDto;
//import com.affycloud.TutorioBackend.dtos.testSeries.directory.TestSeriesDirectoriesDto;
//import com.affycloud.TutorioBackend.dtos.testSeries.exams.TestSeriesResponseWithExamAndTestDto;
//import com.affycloud.TutorioBackend.dtos.testSeries.exams.TestSeriesUnifiedItemDto;
//import com.affycloud.TutorioBackend.dtos.testSeries.test.TestAddInTestSeriesDto;
//import com.affycloud.TutorioBackend.dtos.user.UserResponseDto;
//import com.affycloud.TutorioBackend.enums.ContentType;
//import com.affycloud.TutorioBackend.enums.ExamType;
//import com.affycloud.TutorioBackend.enums.Role;
//import com.affycloud.TutorioBackend.handler.InvalidOperationException;
//import com.affycloud.TutorioBackend.handler.ResourceNotFoundException;
//import com.affycloud.TutorioBackend.models.*;
//import com.affycloud.TutorioBackend.repositories.*;
//import com.affycloud.TutorioBackend.services.TestSeriesService;
//import lombok.RequiredArgsConstructor;
//import org.modelmapper.ModelMapper;
//import org.modelmapper.TypeToken;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.nio.file.StandardCopyOption;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class TestSeriesServiceImplementation  implements TestSeriesService {
//
//        @Autowired
//        private TestSeriesRepository testSeriesRepository;
//
//        @Autowired
//        private DirectoryRepository directoryRepository;
//
//         @Autowired
//        private R2ImageServiceImplementation r2ImageService;
//
//        @Autowired
//        private StudentTestResultRepository studentTestResultRepository;
//
//        @Autowired
//        private StudentRepository studentRepository;
//
//        @Autowired
//        private TestRepository testRepository;
//
//        @Autowired
//        private EnrollmentRepository enrollmentRepository;
//
//        @Autowired
//        private SubmissionRepository submissionRepository;
//
//        @Autowired
//        private ExamRepository examRepository;
//
//        @Autowired
//        private ModelMapper modelMapper;
//
//        @Autowired
//        private CartItemRepository cartItemRepository;
//
//
//    @Override
//    public Map<String, Object> createTestSeries(TestSeriesAddDto request,User user) throws IOException {
//
//        Map<String, Object> response = new HashMap<>();
//
//        // Validate and set paymentType
//        PaymentType paymentType = request.getPaymentType();
//        if (paymentType == null) {
//            paymentType = PaymentType.FREE; // Default to FREE if not provided
//            request.setPaymentType(PaymentType.FREE);
//        }
//        if (paymentType != PaymentType.FREE && paymentType != PaymentType.PAID) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid paymentType");
//        }
//
//
//        TestSeries testSeries = new TestSeries();
//        testSeries.setTitle(request.getTitle());
//        testSeries.setDescription(request.getDescription());
//
//        if(user.getRole() == Role.ADMIN){
////            testSeries.setCreatedById(user.getId());
//            testSeries.setTenantId(user.getId());
//        }
//        else if (user.getRole() == Role.TEACHER) {
////            testSeries.setCreatedById(user.getId());
//            testSeries.setTenantId(user.getParent().getId());
//        }
//
//        if (request.getPaymentType() == PaymentType.PAID) {
//            // For PAID courses, require all fields
//            if (
//                    request.getPrice() == null ||
//                    request.getDiscountType() == null ||
//                    request.getDiscount() == null) {
//                response.put("message", "Price, discountType, and discount are required for PAID courses.");
//                response.put("error", true);
//                return response;
//            }
//
//            Double price = request.getPrice();
//            String discountType = request.getDiscountType().toLowerCase();
//            Double discount = request.getDiscount();
//
//            // **Validation: Discount should not exceed price if type is "flat"**
//            if ("flat".equals(discountType) && discount > price - 1) {
//                response.put("message", "Discount amount cannot be greater than  price minus 1 when discount type is flat.");
//                response.put("error", true);
//                return response;
//            }
//
//            // **Validation: Discount should not exceed 98% if type is "percentage"**
//            if ("percentage".equals(discountType) && discount > 98) {
//                response.put("message", "Discount percentage cannot be more than 98%.");
//                response.put("error", true);
//                return response;
//            }
//
//            // Set values in testSeries
//            testSeries.setPrice(price);
//            testSeries.setDiscountType(discountType);
//            testSeries.setDiscount(discount);
//            testSeries.setFinalPrice(calculateFinalPrice(price, discount, discountType));
//
//        } else { // FREE course
//            // Allow missing values and set defaults
//            testSeries.setPrice(0.0);
//            testSeries.setDiscountType("none");
//            testSeries.setDiscount(0.0);
//            testSeries.setFinalPrice(0.0);
//        }
//        testSeries.setPaymentType(request.getPaymentType());
//        testSeries.setContentType(request.getContentType());  // Set the content type
//        testSeries.setCreatedBy(user);
//
//        // Handle thumbnail file upload if provided
//        if (request.getThumbnail() != null && !request.getThumbnail().isEmpty()) {
//            // Create the upload directory if it doesn't exist
//            Path uploadPath = Paths.get(uploadDir);
//            if (!Files.exists(uploadPath)) {
//                Files.createDirectories(uploadPath);
//            }
//            String fileName = UUID.randomUUID().toString() + "_" + request.getThumbnail().getOriginalFilename();
//            Path filePath = Paths.get(uploadDir, fileName);
//            Files.copy(request.getThumbnail().getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
//            // Construct the file URL using baseUrl and the saved file's name
//            String fileUrl = baseUrl + "/api/tests/images/view/" + fileName;
//            testSeries.setThumbnail(fileUrl);
//        }
//
//
//
//        try {
//            testSeriesRepository.save(testSeries);
//            response.put("error", false);
//            response.put("message", "Test series created successfully");
//        } catch (Exception e) {
//            response.put("message", "Failed to create Test series."+ e.getMessage());
//            response.put("error", true);
//        }
//        return response;
//    }
//
//
//    // Additional helper method for calculating final price (if needed)
//    private Double calculateFinalPrice(Double price, Double discount, String discountType) {
//        if ("percentage".equalsIgnoreCase(discountType)) {
//            return price - (price * discount / 100);
//        } else if ("flat".equalsIgnoreCase(discountType)) {
//            return price - discount;
//        } else if ("none".equalsIgnoreCase(discountType)) {
//            return price;
//        }
//        return price;
//    }
//
//    @Override
//    public Map<String, Object> addTestToTestSeries(UUID testSeriesId, TestAddInTestSeriesDto request) {
//        TestSeries testSeries = testSeriesRepository.findById(testSeriesId)
//                .orElseThrow(() -> new ResourceNotFoundException("Test series not found"));
//
//        if (testSeries.getContentType() != ContentType.SIMPLE) {
//            throw new InvalidOperationException("Adding tests is not supported in non-simple test series");
//        }
//
//
//        Test test = new Test();
//        test.setTitle(request.getTitle());
//        test.setDescription(request.getDescription());
//        test.setTestSeries(testSeries);
//        List<Question> questions = request.getQuestions().stream().map(qDto -> {
//            Question question = new Question();
//            question.setQuestionText(qDto.getQuestionText());
//            question.setOptions(qDto.getOptions());
//            question.setCorrectOptionIndex(qDto.getCorrectOptionIndex());
//            question.setTest(test);
//            return question;
//        }).collect(Collectors.toList());
//
//        test.setQuestions(questions);
//
//        testRepository.save(test);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("message", "Test added to test series successfully");
//        response.put("error", false);
//        return response;
//    }
//
//    @Override
//    public Map<String, Object> addExamToTestSeries(UUID testSeriesId, ExamDTO request) {
//        TestSeries testSeries = testSeriesRepository.findById(testSeriesId)
//                .orElseThrow(() -> new ResourceNotFoundException("Test series not found"));
//
//        if (testSeries.getContentType() != ContentType.SIMPLE) {
//            throw new InvalidOperationException("Adding direct-exam is not supported in non-simple test series");
//        }
//
//
//        Exam exam = modelMapper.map(request, Exam.class);
//
//        if (exam.getQuestions() != null) {
//            exam.getQuestions().forEach(q -> q.setExam(exam));
//        }
//
//
//        exam.setTestSeries(testSeries);
//        Exam savedExam = examRepository.save(exam);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("message", "Exam added to test series successfully");
//        response.put("error", false);
//        return response;
//    }
//
//    @Override
//    public Map<String, Object> addDirectoryInTestSeries(UUID testSeriesId, DirectoryDTO request) {
//        TestSeries testSeries = testSeriesRepository.findById(testSeriesId)
//                .orElseThrow(() -> new ResourceNotFoundException("Test series not found"));
//
//        if (testSeries.getContentType() == ContentType.SIMPLE) {
//            throw new InvalidOperationException("Adding direct-exams is not supported in non-simple test series");
//        }
//
//        Directory directory = new Directory();
//        directory.setName(request.getName());
//        directory.setTestSeries(testSeries);
//
//        directoryRepository.save(directory);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("message", "Directory added to test series successfully");
//        response.put("error", false);
//        return response;
//    }
//
//    @Override
//    public Map<String, Object> addTestInDirectory(UUID directoryId, TestAddInTestSeriesDto request) {
//        Directory directory = directoryRepository.findById(directoryId)
//                .orElseThrow(() -> new ResourceNotFoundException("Directory not found"));
//
//        Test test = new Test();
//        test.setTitle(request.getTitle());
//        test.setDescription(request.getDescription());
//        test.setDirectory(directory);
//
//        List<Question> questions = request.getQuestions().stream().map(qDto -> {
//            Question question = new Question();
//            question.setQuestionText(qDto.getQuestionText());
//            question.setOptions(qDto.getOptions());
//            question.setCorrectOptionIndex(qDto.getCorrectOptionIndex());
//            question.setTest(test);
//            return question;
//        }).collect(Collectors.toList());
//
//        test.setQuestions(questions);
//
//        testRepository.save(test);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("message", "Test added to directory successfully");
//        response.put("error", false);
//        return response;
//    }
//
//    @Override
//    public Map<String, Object> addExamInDirectory(UUID directoryId, ExamDTO request) {
//        Directory directory = directoryRepository.findById(directoryId)
//                .orElseThrow(() -> new ResourceNotFoundException("Directory not found"));
//
//        Exam exam = modelMapper.map(request, Exam.class);
//
//        if (exam.getQuestions() != null) {
//            exam.getQuestions().forEach(q -> q.setExam(exam));
//        }
//
//        exam.setDirectory(directory);
//        examRepository.save(exam);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("message", "Exam added to directory successfully");
//        response.put("error", false);
//        return response;
//    }
//
//
//
//    @Override
//    public TestSeriesResponseDto getTestSeriesById(UUID id,UUID tenantId) {
//        TestSeries testSeries = testSeriesRepository.findById(id)
//                .orElseThrow(() -> new NoSuchElementException("Test series not found with ID: " + id));
//
//        if(testSeries.getTenantId() != tenantId){
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "TestSeries not found.");
//        }
//        return modelMapper.map(testSeries,TestSeriesResponseDto.class);
//    }
//
//    @Override
//    public TestSeriesResponseDto getTestSeriesById(UUID id,UUID tenantId, UUID studentId) {
//        TestSeries testSeries = testSeriesRepository.findById(id)
//                .orElseThrow(() -> new NoSuchElementException("Test series not found with ID: " + id));
//
//        if(testSeries.getTenantId() != tenantId){
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "TestSeries not found.");
//        }
//
//        studentRepository.findById(studentId)
//                .orElseThrow(() -> new NoSuchElementException("Student not found with ID: " + studentId));
//
//        // Check if the TestSeries is in Cart
//        Set<UUID> testSeriesIdsInCart = cartItemRepository.findTestSeriesIdsByStudentId(studentId);
//
//        // Check if the TestSeries is in Enrollment
//        Set<UUID> enrolledTestSeriesIds = enrollmentRepository.findTestSeriesIdsByStudentId(studentId);
//
//        TestSeriesResponseDto dto = modelMapper.map(testSeries, TestSeriesResponseDto.class);
//
//        dto.setInCart(testSeriesIdsInCart.contains(id));
//        dto.setInMyLearning(enrolledTestSeriesIds.contains(id));
//
//        return dto;
//    }
//
//    @Override
//    public Page<Map<String, Object>> getAllTestSeriesByStudent(Pageable pageable, UUID studentId, UUID tenantId, String paymentType) {
//        PaymentType pt = parsePaymentType(paymentType);
//        Page<TestSeries> testSeriesPage = (pt != null)
//                ? testSeriesRepository.findAllByTenantIdAndPaymentType(tenantId,pt, pageable)
//                : testSeriesRepository.findAllByTenantId(pageable, tenantId);
//
//        Student student = studentRepository.findById(studentId)
//                .orElseThrow(() -> new RuntimeException("Student not found"));
//
//        // Fetch testSeries IDs in cart and enrolled
//        Set<UUID> testSeriesIdsInCart = cartItemRepository.findTestSeriesIdsByStudentId(studentId);
//        Set<UUID> enrolledTestSeriesIds = enrollmentRepository.findTestSeriesIdsByStudentId(studentId);
//
//        return testSeriesPage.map(testSeries -> {
//            TestSeriesResponseDto dto = modelMapper.map(testSeries, TestSeriesResponseDto.class);
//            Map<String, Object> testSeriesMap = new HashMap<>();
//            testSeriesMap.put("id", dto.getId());
//            testSeriesMap.put("title", dto.getTitle());
//            testSeriesMap.put("description", dto.getDescription());
//            testSeriesMap.put("paymentType", dto.getPaymentType());
//            testSeriesMap.put("itemType", dto.getItemType());
//            testSeriesMap.put("price", dto.getPrice());
//            testSeriesMap.put("discountType", dto.getDiscountType());
//            testSeriesMap.put("discount", dto.getDiscount());
//            testSeriesMap.put("finalPrice", dto.getFinalPrice());
//            testSeriesMap.put("thumbnail", dto.getThumbnail());
//            testSeriesMap.put("createdBy", dto.getCreatedBy());
//            testSeriesMap.put("createdAt", dto.getCreatedAt());
//            testSeriesMap.put("updatedAt", dto.getUpdatedAt());
//            testSeriesMap.put("tests", dto.getTests());
//            testSeriesMap.put("exams", dto.getExams());
//            testSeriesMap.put("inCart", testSeriesIdsInCart.contains(dto.getId()));
//            testSeriesMap.put("inMyLearning", enrolledTestSeriesIds.contains(dto.getId()));
//            return testSeriesMap;
//        });
//    }
//
//    @Override
//    public Page<TestSeriesResponseDto> getAllTestSeries(Pageable pageable, UUID tenantId, String paymentType) {
//        PaymentType pt = parsePaymentType(paymentType);
//        Page<TestSeries> testSeriesPage = (pt != null)
//                ? testSeriesRepository.findAllByTenantIdAndPaymentType( tenantId, pt, pageable)
//                : testSeriesRepository.findAllByTenantId(pageable,tenantId);
//        return testSeriesPage.map(ts -> modelMapper.map(ts, TestSeriesResponseDto.class));
//    }
//
//    private PaymentType parsePaymentType(String paymentType) {
//        if (paymentType != null && !paymentType.trim().isEmpty()) {
//            try {
//                return PaymentType.valueOf(paymentType.toUpperCase());
//            } catch (IllegalArgumentException e) {
//                throw new RuntimeException("Invalid paymentType provided: " + paymentType);
//            }
//        }
//        return null;
//    }
//
//
//
//
//
//    @Override
//    public TestSeriesResponseWithExamAndTestDto getTestSeriesByIdWithExamsAndTest(UUID id, UUID studentId) {
//        TestSeries testSeries = testSeriesRepository.findById(id)
//                .orElseThrow(() -> new NoSuchElementException("Test series not found with ID: " + id));
//
//        TestSeriesResponseWithExamAndTestDto dto = modelMapper.map(testSeries, TestSeriesResponseWithExamAndTestDto.class);
//        List<TestSeriesUnifiedItemDto> items = new ArrayList<>();
//        List<TestSeriesDirectoriesDto> directories = new ArrayList<>();
//        Map<UUID, StudentTestResult> testResults = new HashMap<>();
//
//        if (studentId != null) {
//            List<StudentTestResult> results = studentTestResultRepository.findByStudentId(studentId);
//            for (StudentTestResult result : results) {
//                testResults.put(result.getTest().getId(), result);
//            }
//        }
//
//        if (testSeries.getContentType() == ContentType.SIMPLE) {
//            // Process Tests
//            if (testSeries.getTests() != null) {
//                for (Test test : testSeries.getTests()) {
//                    TestSeriesUnifiedItemDto itemDto = mapTestToUnifiedItemDto(test, testResults, studentId);
//                    items.add(itemDto);
//                }
//            }
//
//            // Process Exams
//            if (testSeries.getExams() != null) {
//                for (Exam exam : testSeries.getExams()) {
//                    TestSeriesUnifiedItemDto itemDto = mapExamToUnifiedItemDto(exam, studentId);
//                    items.add(itemDto);
//                }
//            }
//        } else if (testSeries.getContentType() == ContentType.SUBJECT) {
//            // Process Directories
//            if (testSeries.getDirectories() != null) {
//                for (Directory directory : testSeries.getDirectories()) {
//                    TestSeriesDirectoriesDto dirDto = new TestSeriesDirectoriesDto();
//                    dirDto.setId(directory.getId());
//                    dirDto.setName(directory.getName());
//                    List<TestSeriesUnifiedItemDto> dirItems = new ArrayList<>();
//
//                    // Process Tests in Directory
//                    if (directory.getTests() != null) {
//                        for (Test test : directory.getTests()) {
//                            TestSeriesUnifiedItemDto itemDto = mapTestToUnifiedItemDto(test, testResults, studentId);
//                            dirItems.add(itemDto);
//                        }
//                    }
//
//                    // Process Exams in Directory
//                    if (directory.getExams() != null) {
//                        for (Exam exam : directory.getExams()) {
//                            TestSeriesUnifiedItemDto itemDto = mapExamToUnifiedItemDto(exam, studentId);
//                            dirItems.add(itemDto);
//                        }
//                    }
//
//                    dirDto.setItems(dirItems);
//                    directories.add(dirDto);
//                }
//            }
//        }
//
//        items.sort(Comparator.comparing(
//                TestSeriesUnifiedItemDto::getCreatedAt,
//                Comparator.nullsLast(Comparator.naturalOrder())
//        ));
//
//        dto.setItems(items);
//        dto.setDirectories(directories);
//        return dto;
//    }
//
//    private TestSeriesUnifiedItemDto mapTestToUnifiedItemDto(Test test, Map<UUID, StudentTestResult> testResults, UUID studentId) {
//        TestSeriesUnifiedItemDto itemDto = new TestSeriesUnifiedItemDto();
//        itemDto.setId(test.getId());
//        itemDto.setTitle(test.getTitle());
//        itemDto.setDescription(test.getDescription());
//        itemDto.setExamType(ExamType.TEST);
//        itemDto.setCreatedAt(test.getCreatedAt());
//        itemDto.setUpdatedAt(test.getUpdatedAt());
//        itemDto.setQuestions(modelMapper.map(test.getQuestions(), new TypeToken<List<Object>>(){}.getType()));
//
//        if (studentId != null && testResults.containsKey(test.getId())) {
//            StudentTestResult result = testResults.get(test.getId());
//            itemDto.setAttended(true);
//            itemDto.setScore(result.getScore());
//        } else {
//            itemDto.setAttended(false);
//            itemDto.setScore(null);
//        }
//
//        return itemDto;
//    }
//
//    private TestSeriesUnifiedItemDto mapExamToUnifiedItemDto(Exam exam, UUID studentId) {
//        TestSeriesUnifiedItemDto itemDto = new TestSeriesUnifiedItemDto();
//        itemDto.setId(exam.getId());
//        itemDto.setTitle(exam.getTitle());
//        itemDto.setDescription(exam.getDescription());
//        itemDto.setExamType(ExamType.EXAM);
//        itemDto.setCreatedAt(exam.getCreatedAt());
//        itemDto.setUpdatedAt(exam.getUpdatedAt());
//        itemDto.setQuestions(modelMapper.map(exam.getQuestions(), new TypeToken<List<Object>>(){}.getType()));
//
//        if (studentId != null && submissionRepository.existsByStudentIdAndExamId(studentId, exam.getId())) {
//            itemDto.setAttended(true);
//        } else {
//            itemDto.setAttended(false);
//        }
//
//        return itemDto;
//    }
//
//
//    @Override
//    public Page<TestSeriesResponseWithoutTestsDto> getAllTestSeriesWithTestCount(Pageable pageable,UUID tenantId) {
//        Page<TestSeries> testSeriesPage = testSeriesRepository.findAllByTenantId(pageable, tenantId);
//        return testSeriesPage.map(course -> {
//            TestSeriesResponseWithoutTestsDto dto = modelMapper.map(course, TestSeriesResponseWithoutTestsDto.class);
//            dto.setTestCount((long) course.getTests().size());
//            dto.setExamCount((long) course.getExams().size());
//            return dto;
//        });
//    }
//
//
//    @Override
//    public DirectoryResponseDto getDirectoryDetailsByDirectoryId(UUID directoryId) {
//        Directory directory = directoryRepository.findById(directoryId)
//                .orElseThrow(() -> new ResourceNotFoundException("Directory not found"));
//
//        return modelMapper.map(directory, DirectoryResponseDto.class);
//    }
//
//
//
//
//    @Override
//    public List<TestSeriesResponseDto> getTestSeriesByUserId(UUID userId) {
//        List<TestSeries> testSeriesList = testSeriesRepository.findByCreatedById(userId);
//
//        return testSeriesList.stream()
//                .map(this::mapToTestSeriesResponseDto)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public void deleteTestSeries(UUID id) {
//        TestSeries testSeries = testSeriesRepository.findById(id)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TestSeries not found with ID: " + id));
//
//        // Check if the TestSeries is linked in Enrollment
//        if (enrollmentRepository.existsByTestSeries(testSeries)) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TestSeries cannot be deleted as it is linked to an enrollment.");
//        }
//        // Check if the TestSeries exists in CartItem
//        List<CartItem> cartItems = cartItemRepository.findByTestSeries(testSeries);
//        if (!cartItems.isEmpty()) {
//            cartItemRepository.deleteAll(cartItems);
//        }
//
//        testSeriesRepository.deleteById(id);
//    }
//
//    @Override
//    public Map<String, Object> updateTestSeries(UUID id, TestSeriesUpdateDto updateDto, MultipartFile thumbnail) throws IOException {
//
//        Map<String, Object> response = new HashMap<>();
//
//        TestSeries testSeries = testSeriesRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("TestSeries not found"));
//
//        // Update fields only if they are provided in the update DTO.
//        if (updateDto.getTitle() != null) {
//            testSeries.setTitle(updateDto.getTitle());
//        }
//        if (updateDto.getDescription() != null) {
//            testSeries.setDescription(updateDto.getDescription());
//        }
//
//        if (updateDto.getPaymentType() != null) {
//            testSeries.setPaymentType(updateDto.getPaymentType());
//        }
//
//        // Handle thumbnail upload using R2ImageService
//        if (thumbnail != null && !thumbnail.isEmpty()) {
//            // delete old thumbnail
//            r2ImageService.deleteFileByUrl(testSeries.getThumbnail());
//            // upload new one
//            String imageUrl = r2ImageService.uploadImage(thumbnail);
//            testSeries.setThumbnail(imageUrl);
//        }
//
//        // Validation: Only perform these if any related field is updated.
//        Double effectivePrice = testSeries.getPrice();
//        Double effectiveDiscount = testSeries.getDiscount();
//        String effectiveDiscountType = testSeries.getDiscountType();
//
//        if (effectivePrice != null && effectiveDiscount != null && effectiveDiscountType != null) {
//            if ("flat".equals(effectiveDiscountType) && effectiveDiscount > effectivePrice - 1) {
//                response.put("message", "Discount amount cannot be greater than price minus 1 when discount type is flat.");
//                response.put("error", true);
//                return response;
//            }
//            if ("percentage".equals(effectiveDiscountType) && effectiveDiscount > 98) {
//                response.put("message", "Discount percentage cannot be more than 98%.");
//                response.put("error", true);
//                return response;
//            }
//
//            if (updateDto.getPrice() != null) {
//                testSeries.setPrice(updateDto.getPrice());
//            }
//            if (updateDto.getDiscountType() != null) {
//                testSeries.setDiscountType(updateDto.getDiscountType().toLowerCase());
//            }
//            if (updateDto.getDiscount() != null) {
//                testSeries.setDiscount(updateDto.getDiscount());
//            }
//
//            if (updateDto.getPrice() != null || updateDto.getDiscount() != null || updateDto.getDiscountType() != null) {
//                testSeries.setFinalPrice(calculateFinalPrice(testSeries.getPrice(), testSeries.getDiscount(), testSeries.getDiscountType()));
//            }
//        }
//
//
//
//
//
//        testSeriesRepository.save(testSeries);
//        response.put("error", false);
//        response.put("message", "Test series updated successfully");
//        return response;
//    }
//
//public TestSeriesResponseDto mapToTestSeriesResponseDto(TestSeries testSeries) {
//    TestSeriesResponseDto response = new TestSeriesResponseDto();
//    response.setId(testSeries.getId());
//    response.setTitle(testSeries.getTitle());
//    response.setDescription(testSeries.getDescription());
//    response.setPrice(testSeries.getPrice());
//    response.setPaymentType(testSeries.getPaymentType());
//    response.setCreatedAt(testSeries.getCreatedAt());
//    response.setUpdatedAt(testSeries.getUpdatedAt());
//
//    UserResponseDto userResponseDto = mapToUserResponseDto(testSeries.getCreatedBy());
//    response.setCreatedBy(userResponseDto);
//
//    List<TestResponseDto> testResponseDtos = testSeries.getTests().stream()
//            .map(this::mapToTestResponseDto)
//            .collect(Collectors.toList());
//    response.setTests(testResponseDtos);
//
//    return response;
//}
//
//public UserResponseDto mapToUserResponseDto(User user) {
//    UserResponseDto userResponseDto = new UserResponseDto();
//    userResponseDto.setId(user.getId());
//    userResponseDto.setName(user.getName());
//    userResponseDto.setEmail(user.getEmail());
//    userResponseDto.setPhoneNumber(user.getPhoneNumber());
//    userResponseDto.setUsername(user.getUsername());
//    userResponseDto.setRole(user.getRole());
//    userResponseDto.setCreatedAt(user.getCreatedAt());
//    userResponseDto.setUpdatedAt(user.getUpdatedAt());
//    return userResponseDto;
//}
//
//public TestResponseDto mapToTestResponseDto(Test test) {
//    TestResponseDto testResponseDto = new TestResponseDto();
//    testResponseDto.setId(test.getId());
//    testResponseDto.setTitle(test.getTitle());
//    testResponseDto.setDescription(test.getDescription());
//    testResponseDto.setCreatedAt(test.getCreatedAt());
//    testResponseDto.setUpdatedAt(test.getUpdatedAt());
//
//    List<QuestionResponseDto> questionResponseDtos = test.getQuestions().stream()
//            .map(this::mapToQuestionResponseDto)
//            .collect(Collectors.toList());
//    testResponseDto.setQuestions(questionResponseDtos);
//
//    return testResponseDto;
//}
//
//public QuestionResponseDto mapToQuestionResponseDto(Question question) {
//    QuestionResponseDto questionResponseDto = new QuestionResponseDto();
//    questionResponseDto.setId(question.getId());
//    questionResponseDto.setQuestionText(question.getQuestionText());
//    questionResponseDto.setOptions(question.getOptions());
//    questionResponseDto.setCorrectOptionIndex(question.getCorrectOptionIndex());
//    return questionResponseDto;
//}
//
//
//}